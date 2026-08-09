package com.example.star.service;

import com.example.star.dto.RecommendationDto;
import com.example.star.repository.RecommendationRepository;
import com.example.star.service.rules.RecommendationRuleSet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Тесты RecommendationService")
class RecommendationServiceTest {

    @Mock
    private RecommendationRepository repository;

    @Mock
    private RuleService ruleService;

    @Mock
    private List<RecommendationRuleSet> ruleSets;

    @InjectMocks
    private RecommendationService recommendationService;

    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
    }

    @Test
    @DisplayName("Должен вернуть пустой список, если нет правил")
    void getRecommendations_shouldReturnEmptyList_whenNoRules() {
        // given
        when(ruleService.getAllRulesRaw()).thenReturn(List.of());

        // when
        List<RecommendationDto> result = recommendationService.getRecommendations(userId);

        // then
        assertThat(result).isEmpty();
        verify(ruleService, times(1)).getAllRulesRaw();
    }

    @Test
    @DisplayName("Должен вернуть рекомендации для динамического правила")
    void getRecommendations_shouldReturnDynamicRecommendation_whenRuleMatches() {
        // given
        Map<String, Object> rule = Map.of(
                "productName", "Test Product",
                "productId", UUID.randomUUID().toString(),
                "productText", "Test Description",
                "rule", List.of(Map.of(
                        "query", "USER_OF",
                        "arguments", List.of("DEBIT"),
                        "negate", false
                ))
        );
        when(ruleService.getAllRulesRaw()).thenReturn(List.of(rule));

        // when
        List<RecommendationDto> result = recommendationService.getRecommendations(userId);

        // then
        assertThat(result).isNotEmpty();
        assertThat(result.get(0).getName()).isEqualTo("Test Product");
    }
}