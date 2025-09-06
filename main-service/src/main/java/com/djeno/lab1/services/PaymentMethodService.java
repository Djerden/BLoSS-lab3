package com.djeno.lab1.services;

import com.djeno.lab1.exceptions.*;
import com.djeno.lab1.persistence.DTO.payment.AddCardRequest;
import com.djeno.lab1.persistence.DTO.payment.PaymentCardDTO;
import com.djeno.lab1.persistence.models.PaymentMethod;
import com.djeno.lab1.persistence.models.TestVirtualAccount;
import com.djeno.lab1.persistence.models.User;
import com.djeno.lab1.persistence.repositories.PaymentMethodRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class PaymentMethodService {

    private final PaymentMethodRepository paymentMethodRepository;
    private final UserService userService;
    private final TransactionService transactionService;
    private final TestVirtualAccountService testVirtualAccountService;

    public PaymentMethod addCard(AddCardRequest request) {
        User user = userService.getCurrentUser();

        if (paymentMethodRepository.existsByUserAndCardNumber(user, request.getCardNumber())) {
            throw new CardAlreadyExistsException("Эта карта уже привязана к вашему аккаунту");
        }

        // Создаем виртуальный тестовый счет
        if (!testVirtualAccountService.existsAccount(request.getCardNumber())) {
            testVirtualAccountService.createAccount(request.getCardNumber());
        }

        PaymentMethod card = new PaymentMethod();
        card.setUser(user);
        card.setCardNumber(request.getCardNumber());
        card.setCardHolder(request.getCardHolder());
        card.setExpirationDate(request.getExpirationDate());
        card.setCvv(request.getCvv());
        card.setPrimary(user.getPaymentMethods().isEmpty()); // Первая карта становится основной

        return paymentMethodRepository.save(card);
    }

    public void setPrimaryCard(Long cardId) {
        User user = userService.getCurrentUser();
        PaymentMethod newPrimary = paymentMethodRepository.findByIdAndUser(cardId, user)
                .orElseThrow(() -> new CardNotFoundException("Карта не найдена"));

        user.getPaymentMethods().forEach(card -> {
            card.setPrimary(card.getId().equals(cardId));
            paymentMethodRepository.save(card);
        });
    }

    public List<PaymentCardDTO> getUserCards() {
        User user = userService.getCurrentUser();
        List<PaymentMethod> cards = paymentMethodRepository.findByUser(user);

        return cards.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteCard(Long cardId) {
        User currentUser = userService.getCurrentUser();
        PaymentMethod card = paymentMethodRepository.findByIdAndUser(cardId, currentUser)
                .orElseThrow(() -> new CardNotFoundException("Карта не найдена или не принадлежит пользователю"));

        // Нельзя удалить основную карту, если она последняя
        if (card.isPrimary() && paymentMethodRepository.countByUser(currentUser) == 1) {
            throw new LastPrimaryCardException("Нельзя удалить единственную основную карту");
        }

        paymentMethodRepository.delete(card);

        // Если удалили основную карту - назначаем новую основную
        if (card.isPrimary()) {
            paymentMethodRepository.findFirstByUser(currentUser)
                    .ifPresent(newPrimary -> {
                        newPrimary.setPrimary(true);
                        paymentMethodRepository.save(newPrimary);
                    });
        }
    }

    private PaymentCardDTO convertToDto(PaymentMethod card) {
        return PaymentCardDTO.builder()
                .id(card.getId())
                .maskedCardNumber(maskCardNumber(card.getCardNumber()))
                .isPrimary(card.isPrimary())
                .build();
    }

    private String maskCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.isEmpty()) {
            return "****";
        }
        String cleanNumber = cardNumber.replaceAll("[^0-9]", "");
        if (cleanNumber.length() < 8) {
            return "****" + (cleanNumber.isEmpty() ? "" : " " + cleanNumber);
        }
        String firstFour = cleanNumber.substring(0, 4);
        String lastFour = cleanNumber.substring(cleanNumber.length() - 4);
        int maskedDigits = cleanNumber.length() - 8;
        String maskedMiddle = String.join("", Collections.nCopies(maskedDigits, "*"));
        String formatted = firstFour + " " + maskedMiddle + " " + lastFour;

        return formatted.replaceAll(" {2,}", " ").trim();
    }

    public boolean processPayment(User user, BigDecimal amount) {
        // Бесплатное приложение
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            return true;
        }

        PaymentMethod primaryCard = paymentMethodRepository.findByUserAndIsPrimary(user, true)
                .orElseThrow(() -> new PrimaryCardNotFoundException("Основная карта не найдена"));

        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("processPaymentTx");
        def.setTimeout(30);
        // PROPAGATION_REQUIRED_NEW - новая независимая транзакция
        // PROPAGATION_REQUIRED - если есть активная транзакция, метод войдет в нее и станет ее частью
        // PROPAGATION_NESTED - вложенная в родительскую, rollback ограничивается savepoint (но commit всё равно будет только вместе с родительской)

        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

        return transactionService.execute(def, status -> {
            TestVirtualAccount account = testVirtualAccountService.findByCardNumberForUpdate(primaryCard.getCardNumber());
            if (account.getBalance().compareTo(amount) < 0) {
                throw new InsufficientBalanceException("Недостаточно средств на балансе");
            }
            account.setBalance(account.getBalance().subtract(amount));
            testVirtualAccountService.save(account);
            log.info("Withdraw {} from account {}, new balance: {}", amount, account.getCardNumber(), account.getBalance());
            return true;
        });
    }
}
