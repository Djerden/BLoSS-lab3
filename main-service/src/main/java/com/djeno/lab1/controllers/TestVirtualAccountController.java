package com.djeno.lab1.controllers;

import com.djeno.lab1.services.TestVirtualAccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RequiredArgsConstructor
@RequestMapping("/test/accounts")
@RestController
public class TestVirtualAccountController {

    private final TestVirtualAccountService testVirtualAccountService;

    // Проверить баланс
    @GetMapping("/{cardNumber}/balance")
    public ResponseEntity<String> getBalance(@PathVariable String cardNumber) {
        BigDecimal balance = testVirtualAccountService.getBalance(cardNumber);
        return ResponseEntity.ok(balance.toString());
    }

    // Пополнить счет
    @PostMapping("/{cardNumber}/deposit")
    public ResponseEntity<String> deposit(@PathVariable String cardNumber, @RequestParam BigDecimal amount) {
        testVirtualAccountService.deposit(cardNumber, amount);
        return ResponseEntity.ok("Deposited " + amount + " to account " + cardNumber);
    }

    // Снять со счета
    @PostMapping("/{cardNumber}/withdraw")
    public ResponseEntity<String> withdraw(@PathVariable String cardNumber, @RequestParam BigDecimal amount) {
        testVirtualAccountService.withdraw(cardNumber, amount);
        return ResponseEntity.ok("Withdrew " + amount + " from account " + cardNumber);
    }

}
