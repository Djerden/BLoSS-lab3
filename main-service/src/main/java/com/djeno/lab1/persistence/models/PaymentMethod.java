package com.djeno.lab1.persistence.models;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Data
@Entity
@Table(name = "payment_methods")
public class PaymentMethod {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String cardNumber;

    @Column(nullable = false)
    private String cardHolder;

    @Column(nullable = false)
    private String expirationDate;

    @Column(nullable = false)
    private String cvv;

    @Column(nullable = false)
    private boolean isPrimary;

    @PrePersist
    @PreUpdate
    private void ensureSinglePrimaryCard() {
        if (isPrimary) {
            // При выборе новой основной карты сбрасываем флаг isPrimary у других карт пользователя
            List<PaymentMethod> userCards = user.getPaymentMethods();
            if (userCards != null) {
                for (PaymentMethod card : userCards) {
                    if (!card.getId().equals(this.id)) {
                        card.setPrimary(false);
                    }
                }
            }
        }
    }
}
