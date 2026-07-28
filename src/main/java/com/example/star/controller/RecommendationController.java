package com.example.star.controller;

import com.example.star.dto.RecommendationDto;
import com.example.star.dto.RecommendationResponse;
import com.example.star.service.RecommendationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/recommendation")
public class RecommendationController {
    private final RecommendationService recommendationService;

    public RecommendationController(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    @GetMapping("/{user_id}")
    public ResponseEntity<RecommendationResponse> getRecommendations(@PathVariable("user_id") UUID userId) {
        List<RecommendationDto> recommendations = recommendationService.getRecommendations(userId);
        RecommendationResponse response = new RecommendationResponse(userId, recommendations);
        return ResponseEntity.ok(response);

    }
}