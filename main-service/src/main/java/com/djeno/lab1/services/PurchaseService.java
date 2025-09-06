package com.djeno.lab1.services;

import com.djeno.lab1.exceptions.AppAlreadyPurchasedException;
import com.djeno.lab1.persistence.models.App;
import com.djeno.lab1.persistence.models.Purchase;
import com.djeno.lab1.persistence.models.User;
import com.djeno.lab1.persistence.repositories.PurchaseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.time.LocalDateTime;

@RequiredArgsConstructor
@Service
public class PurchaseService {

    private final PurchaseRepository purchaseRepository;
    private final PaymentMethodService paymentMethodService;
    private final TransactionService transactionService;

    public Page<Purchase> getPurchasesByUser(User user, Pageable pageable) {
        return purchaseRepository.findByUser(user, pageable);
    }

    public boolean hasUserPurchasedApp(User user, App app) {
        return purchaseRepository.existsByUserAndApp(user, app);
    }

    // все выполняется в рамках одной транзакции (оплата и добавление приложения в список оплаченных у пользователя)
    public Purchase purchaseApp(App app, User user) {

        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("purchaseAppTx");
        def.setTimeout(300);
        def.setPropagationBehavior(DefaultTransactionDefinition.PROPAGATION_REQUIRED);
        def.setIsolationLevel(DefaultTransactionDefinition.ISOLATION_SERIALIZABLE);

        return transactionService.execute(def, status -> {

            // Проверяем, не куплено ли уже
            if (hasUserPurchasedApp(user, app)) {
                throw new AppAlreadyPurchasedException("Приложение уже приобретено");
            }

            // Пытаемся провести оплату
            paymentMethodService.processPayment(user, app.getPrice());

            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Interrupted while simulating delay", e);
            }

            Purchase purchase = new Purchase();
            purchase.setUser(user);
            purchase.setApp(app);
            purchase.setPurchaseDate(LocalDateTime.now());

            return purchaseRepository.save(purchase);
        });
    }
}
