package com.djeno.lab1.controllers;

import com.djeno.lab1.services.PurchaseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/yookassa/notifications")
public class YookassaController {

    private final PurchaseService purchaseService;

    @PostMapping
    public ResponseEntity<String> handlePayment(@RequestBody String payload) {
        purchaseService.confirmPayment(payload);
        return ResponseEntity.ok().body("OK");

    }
}
