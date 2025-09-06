package com.djeno.lab1.persistence.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Entity
@Table(name = "virtual_accounts")
public class TestVirtualAccount {

    @Id
    @Column(unique = true, nullable = false)
    private String cardNumber;

    @Min(value = 0, message = "Balance cannot be negative")
    @Column(nullable = false)
    private BigDecimal balance = BigDecimal.ZERO;

    @PrePersist
    public void prePersist() {
        if (balance == null) {
            balance = BigDecimal.ZERO;
        }
    }
}
