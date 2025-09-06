package com.djeno.lab1.persistence.repositories;

import com.djeno.lab1.persistence.models.TestVirtualAccount;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TestVirtualAccountRepository extends JpaRepository<TestVirtualAccount, String> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM TestVirtualAccount a WHERE a.cardNumber = :cardNumber")
    Optional<TestVirtualAccount> findByIdForUpdate(@Param("cardNumber") String cardNumber);
}
