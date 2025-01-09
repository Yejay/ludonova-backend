package com.bht.ludonova.dto.review;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ReviewResponseDTO {
    private Long id;
    private Long gameId;
    private String gameTitle;
    private Long userId;
    private String username;
    private Integer rating;
    private String reviewText;
    private LocalDateTime createdAt;
}
