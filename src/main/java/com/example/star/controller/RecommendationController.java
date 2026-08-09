package com.example.star.controller;

import com.example.star.dto.RecommendationDto;
import com.example.star.dto.RecommendationResponse;
import com.example.star.service.RecommendationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Контроллер для получения рекомендаций.
 */
@RestController
@RequestMapping("/recommendation")
public class RecommendationController {

    private static final Logger logger = LoggerFactory.getLogger(RecommendationController.class);

    private final RecommendationService recommendationService;

    public RecommendationController(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    /**
     * Получить рекомендации для пользователя.
     *
     * @param userId идентификатор пользователя
     * @return рекомендации в формате JSON
     */
    @GetMapping("/{user_id}")
    public ResponseEntity<RecommendationResponse> getRecommendations(@PathVariable("user_id") UUID userId) {
        logger.info("Received GET /recommendation/{} request", userId);
        List<RecommendationDto> recommendations = recommendationService.getRecommendations(userId);
        RecommendationResponse response = new RecommendationResponse(userId, recommendations);
        logger.debug("Returning {} recommendations for user {}", recommendations.size(), userId);
        return ResponseEntity.ok(response);
    }
}