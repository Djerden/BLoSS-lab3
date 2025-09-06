package com.djeno.lab1.persistence.repositories;

import com.djeno.lab1.persistence.models.App;
import com.djeno.lab1.persistence.models.Purchase;
import com.djeno.lab1.persistence.models.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PurchaseRepository extends JpaRepository<Purchase, Long> {

    boolean existsByUserAndApp(User user, App app);

    Page<Purchase> findByUser(User user, Pageable pageable);
}
