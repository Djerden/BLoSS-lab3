package com.djeno.lab1.services;

import com.djeno.lab1.exceptions.AppAlreadyPurchasedException;
import com.djeno.lab1.exceptions.PurchaseAbsenceException;
import com.djeno.lab1.exceptions.PurchaseFreeAppException;
import com.djeno.lab1.jca.YookassaConnection;
import com.djeno.lab1.persistence.enums.PurchaseStatus;
import com.djeno.lab1.persistence.models.App;
import com.djeno.lab1.persistence.models.Purchase;
import com.djeno.lab1.persistence.models.User;
import com.djeno.lab1.persistence.repositories.PurchaseRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Slf4j
@RequiredArgsConstructor
@Service
public class PurchaseService {

    private final PurchaseRepository purchaseRepository;
    private final YookassaConnection yookassaConnection;

    public Page<Purchase> getPurchasesByUser(User user, Pageable pageable) {
        return purchaseRepository.findByUser(user, pageable);
    }

    public Page<Purchase> getPaidPurchasesByUser(User user, Pageable pageable) {
        return purchaseRepository.findByUserAndStatus(user, PurchaseStatus.PAID, pageable);
    }

    public boolean hasUserPurchasedApp(User user, App app) {
        return purchaseRepository.existsByUserAndApp(user, app);
    }

    public String purchaseApp(App app, User user) {

        // Проверяем, не куплено ли уже
        if (hasUserPurchasedApp(user, app)) {
            throw new AppAlreadyPurchasedException("Приложение уже приобретено");
        }

        // Бесплатное приложение
        if (app.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new PurchaseFreeAppException("Попытка оплатить бесплатное приложение");
        }

        Purchase purchase = new Purchase();
        purchase.setUser(user);
        purchase.setApp(app);
        purchase.setTotalPrice(app.getPrice());
        purchase.setStatus(PurchaseStatus.UNPAID);
        purchase.setPurchaseDate(LocalDateTime.now());

        Purchase savedPurchase = purchaseRepository.save(purchase);

        return yookassaConnection.createPayment(savedPurchase.getTotalPrice().longValue(), savedPurchase.getId());
    }

    public void confirmPayment(String yooKassaPaymentResponse){
        log.info("ConfirmPayment");
        log.info(yooKassaPaymentResponse);
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = null;
        try {
            root = mapper.readTree(yooKassaPaymentResponse);
        } catch (JsonProcessingException e) {
            log.info(e.getMessage());
            throw new RuntimeException(e);
        }

        String event = root.path("event").asText();
        log.info("event = {}", event);
        String status = root.path("object").path("status").asText();
        log.info("status = {}", status);
        String orderIdStr = root.path("object").path("metadata").path("order_id").asText();
        Long orderId = Long.parseLong(orderIdStr);
        log.info("order_id = {}", orderId);

        if ("payment.succeeded".equals(event) && "succeeded".equals(status)) {
            log.info("Payment succeeded");
            Purchase purchase = purchaseRepository.findById(orderId).orElseThrow(() -> new PurchaseAbsenceException("Purchase not found"));
            purchase.setStatus(PurchaseStatus.PAID);
            purchaseRepository.save(purchase);
        }
    }
}
