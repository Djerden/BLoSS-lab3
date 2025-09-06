package com.djeno.lab1.services;

import com.djeno.lab1.persistence.DTO.review.CreateReviewRequest;
import com.djeno.lab1.persistence.models.App;
import com.djeno.lab1.persistence.models.Review;
import com.djeno.lab1.persistence.models.User;
import com.djeno.lab1.persistence.repositories.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final AppService appService;
    private final UserService userService;


    @Transactional
    public void createReview(CreateReviewRequest request) {
        User currentUser = userService.getCurrentUser();
        App app = appService.getAppById(request.getAppId());

        Review review = new Review();
        review.setUser(currentUser);
        review.setApp(app);
        review.setRating(request.getRating());
        review.setComment(request.getComment());

        reviewRepository.save(review);
    }
}
