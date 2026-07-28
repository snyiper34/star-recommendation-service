package com.example.star.service;

import com.example.star.dto.RecommendationDto;
import com.example.star.service.rules.RecommendationRuleSet;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class RecommendationService {
    private final List<RecommendationRuleSet> ruleSets;

    public RecommendationService(List<RecommendationRuleSet> ruleSets) {
        this.ruleSets = ruleSets;
    }

    public List<RecommendationDto> getRecommendations(UUID userId) {
        List<RecommendationDto> recommendations = new ArrayList<>();

        for (RecommendationRuleSet ruleSet : ruleSets) {
            ruleSet.check(userId).ifPresent(recommendations::add);
        }

        return recommendations;
    }
}