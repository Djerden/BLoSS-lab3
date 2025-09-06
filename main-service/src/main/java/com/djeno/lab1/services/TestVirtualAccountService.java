package com.djeno.lab1.services;

import com.djeno.lab1.exceptions.CardAlreadyExistsException;
import com.djeno.lab1.exceptions.CardNotFoundException;
import com.djeno.lab1.exceptions.InsufficientBalanceException;
import com.djeno.lab1.exceptions.NegativeValueException;
import com.djeno.lab1.persistence.models.TestVirtualAccount;
import com.djeno.lab1.persistence.repositories.TestVirtualAccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Slf4j
@RequiredArgsConstructor
@Service
public class TestVirtualAccountService {

    private final TestVirtualAccountRepository repository;
    private final TransactionService transactionService;

    public BigDecimal getBalance(String cardNumber) {
        return repository.findById(cardNumber)
                .map(TestVirtualAccount::getBalance)
                .orElseThrow(() -> new CardNotFoundException("Виртуальный счет не найден"));
    }

    public void deposit(String cardNumber, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new NegativeValueException("Пополняемая сумма должна быть положительной");
        }
        TestVirtualAccount account = repository.findById(cardNumber)
                .orElseThrow(() -> new CardNotFoundException("Виртуальный счет не найден"));
        account.setBalance(account.getBalance().add(amount));
        repository.save(account);
    }

    public boolean withdraw(String cardNumber, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new NegativeValueException("Выводимая сумма должна быть положительной");
        }
        return transactionService.execute("processPaymentTx", 30, status -> {
            TestVirtualAccount account = repository.findByIdForUpdate(cardNumber)
                    .orElseThrow(() -> new CardNotFoundException("Виртуальный счет не найден"));
            if (account.getBalance().compareTo(amount) < 0) {
                throw new InsufficientBalanceException("Недостаточно средств на балансе");
            }
            account.setBalance(account.getBalance().subtract(amount));
            repository.save(account);
            log.info("Withdraw {} from account {}, new balance: {}", amount, account.getCardNumber(), account.getBalance());
            return true;
        });
    }

    public TestVirtualAccount createAccount(String cardNumber) {
        if (repository.existsById(cardNumber)) {
            throw new CardAlreadyExistsException("Виртуальный счет уже существует");
        }
        TestVirtualAccount account = new TestVirtualAccount();
        account.setCardNumber(cardNumber);
        account.setBalance(BigDecimal.ZERO);
        return repository.save(account);
    }

    public boolean existsAccount(String cardNumber) {
        return repository.existsById(cardNumber);
    }

    public TestVirtualAccount findByCardNumberForUpdate(String cardNumber) {
        return repository.findByIdForUpdate(cardNumber)
                .orElseThrow(() -> new CardNotFoundException("Виртуальный счет не найден"));
    }

    public void save(TestVirtualAccount account) {
        repository.save(account);
    }
}
