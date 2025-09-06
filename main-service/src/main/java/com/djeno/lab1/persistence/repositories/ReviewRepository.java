package com.djeno.lab1.persistence.repositories;

import com.djeno.lab1.persistence.models.App;
import com.djeno.lab1.persistence.models.Review;
import com.djeno.lab1.persistence.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    boolean existsByUserAndApp(User user, App app);
}
