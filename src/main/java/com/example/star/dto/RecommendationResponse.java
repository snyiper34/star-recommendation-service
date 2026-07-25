package com.example.star.dto;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class RecommendationResponse {
    private final UUID user_id;
    private final List<RecommendationDto> recommendations;

    public RecommendationResponse(UUID user_id, List<RecommendationDto> recommendations) {
        this.user_id = user_id;
        this.recommendations = recommendations != null ? List.copyOf(recommendations) : List.of();
    }

    public UUID getUser_id() {
        return user_id;
    }

    public List<RecommendationDto> getRecommendations() {
        return recommendations;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RecommendationResponse that = (RecommendationResponse) o;
        return Objects.equals(user_id, that.user_id) &&
                Objects.equals(recommendations, that.recommendations);
    }

    @Override
    public int hashCode() {
        return Objects.hash(user_id, recommendations);
    }
}