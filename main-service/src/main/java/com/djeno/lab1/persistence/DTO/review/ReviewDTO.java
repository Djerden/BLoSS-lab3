package com.djeno.lab1.persistence.DTO.review;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReviewDTO {
    private Long id;
    private String userUsername;
    private int rating;
    private String comment;
    private LocalDateTime createdAt;
}
