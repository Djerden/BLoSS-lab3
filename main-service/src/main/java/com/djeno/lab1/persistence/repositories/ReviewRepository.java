package com.djeno.lab1.persistence.repositories;

import com.djeno.lab1.persistence.models.App;
import com.djeno.lab1.persistence.models.Review;
import com.djeno.lab1.persistence.models.User;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.awt.print.Pageable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    boolean existsByUserAndApp(User user, App app);

    // Статистика
    long countByCreatedAtAfter(LocalDateTime dateTime);

    @Query("SELECT AVG(r.rating) FROM Review r")
    Optional<Double> getAverageRating();

    @Query(value = "SELECT a.name, COUNT(r.id) " +
            "FROM reviews r " +
            "JOIN apps a ON r.app_id = a.id " +
            "GROUP BY a.name " +
            "ORDER BY COUNT(r.id) DESC " +
            "LIMIT :limit", nativeQuery = true)
    List<Object[]> findTopAppsByReviewCount(@Param("limit") int limit);
}
