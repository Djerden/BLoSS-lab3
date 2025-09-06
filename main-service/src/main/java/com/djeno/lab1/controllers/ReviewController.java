package com.djeno.lab1.controllers;

import com.djeno.lab1.persistence.DTO.review.CreateReviewRequest;
import com.djeno.lab1.persistence.DTO.review.ReviewDTO;
import com.djeno.lab1.services.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@Tag(name = "Отзывы")
@RequiredArgsConstructor
@RequestMapping("/reviews")
@RestController
public class ReviewController {

    private final ReviewService reviewService;

    @Operation(
            summary = "Создать новый отзыв",
            description = "Создает отзыв для указанного приложения с рейтингом и комментарием",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Отзыв успешно создан"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Приложение не найдено"
            )
    })
    @PreAuthorize("hasAuthority('CREATE_REVIEW')")
    @PostMapping
    public ResponseEntity<String> createReview(
            @Parameter(description = "Данные для создания отзыва", required = true)
            @RequestBody
            @Valid
            CreateReviewRequest request) {
        reviewService.createReview(request);
        return ResponseEntity.ok("Отзыв успешно создан");
    }
}
