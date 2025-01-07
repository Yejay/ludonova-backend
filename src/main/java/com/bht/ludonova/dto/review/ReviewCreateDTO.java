package com.bht.ludonova.dto.review;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReviewCreateDTO {
    @NotNull
    private Long gameId;

    @NotNull
    @Min(1)
    @Max(5)
    private Integer rating;

    private String reviewText;
}
