package com.example.star.controller;

import com.example.star.dto.RecommendationDto;
import com.example.star.dto.RecommendationResponse;
import com.example.star.service.RecommendationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RecommendationController.class)
@DisplayName("Тесты RecommendationController")
class RecommendationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RecommendationService recommendationService;

    private UUID userId;
    private List<RecommendationDto> recommendations;

    @BeforeEach
    void setUp() {
        userId = UUID.fromString("cd515076-5d8a-44be-930e-8d4fcb79f42d");
        recommendations = List.of(
                new RecommendationDto("Invest 500", UUID.randomUUID(), "Test description")
        );
    }

    @Test
    @DisplayName("GET /recommendation/{user_id} — должен вернуть рекомендации")
    void getRecommendations_shouldReturnRecommendations() throws Exception {
        when(recommendationService.getRecommendations(any(UUID.class))).thenReturn(recommendations);

        mockMvc.perform(get("/recommendation/{user_id}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user_id").value(userId.toString()))
                .andExpect(jsonPath("$.recommendations").isArray())
                .andExpect(jsonPath("$.recommendations.length()").value(1))
                .andExpect(jsonPath("$.recommendations[0].name").value("Invest 500"));
    }

    @Test
    @DisplayName("GET /recommendation/{user_id} — должен вернуть пустой список, если рекомендаций нет")
    void getRecommendations_shouldReturnEmptyList_whenNoRecommendations() throws Exception {
        when(recommendationService.getRecommendations(any(UUID.class))).thenReturn(List.of());

        mockMvc.perform(get("/recommendation/{user_id}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user_id").value(userId.toString()))
                .andExpect(jsonPath("$.recommendations").isArray())
                .andExpect(jsonPath("$.recommendations.length()").value(0));
    }
}