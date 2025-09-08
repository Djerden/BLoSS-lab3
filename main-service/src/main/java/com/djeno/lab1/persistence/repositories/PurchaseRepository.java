package com.djeno.lab1.persistence.repositories;

import com.djeno.lab1.persistence.enums.PurchaseStatus;
import com.djeno.lab1.persistence.models.App;
import com.djeno.lab1.persistence.models.Purchase;
import com.djeno.lab1.persistence.models.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PurchaseRepository extends JpaRepository<Purchase, Long> {

    boolean existsByUserAndApp(User user, App app);

    Page<Purchase> findByUser(User user, Pageable pageable);

    Page<Purchase> findByUserAndStatus(User user, PurchaseStatus status, Pageable pageable);


    // Статистика
    long countByStatus(PurchaseStatus status);
    long countByStatusAndPurchaseDateAfter(PurchaseStatus status, LocalDateTime dateTime);

    @Query("SELECT SUM(p.totalPrice) FROM Purchase p WHERE p.status = :status")
    Optional<BigDecimal> sumTotalPriceByStatus(@Param("status") PurchaseStatus status);

    @Query("SELECT p.app.name, SUM(p.totalPrice) FROM Purchase p WHERE p.status = 'PAID' GROUP BY p.app.name ORDER BY SUM(p.totalPrice) DESC")
    List<Object[]> findTopAppsByRevenue(Pageable pageable);

    @Query("SELECT p.user.username, COUNT(p) FROM Purchase p WHERE p.status = 'PAID' GROUP BY p.user.username ORDER BY COUNT(p) DESC")
    List<Object[]> findTopUsersByPurchaseCount(Pageable pageable);

}
