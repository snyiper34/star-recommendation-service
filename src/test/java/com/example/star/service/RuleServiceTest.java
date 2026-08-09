package com.example.star.service;

import com.example.star.dto.RuleRequest;
import com.example.star.dto.RuleResponse;
import com.example.star.entity.RuleEntity;
import com.example.star.entity.RuleQueryEntity;
import com.example.star.repository.RuleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Тесты RuleService")
class RuleServiceTest {

    @Mock
    private RuleRepository ruleRepository;

    @InjectMocks
    private RuleService ruleService;

    private UUID ruleId;
    private RuleRequest ruleRequest;

    @BeforeEach
    void setUp() {
        ruleId = UUID.randomUUID();
        ruleRequest = new RuleRequest();
        ruleRequest.setProductName("Test Product");
        ruleRequest.setProductId(UUID.randomUUID());
        ruleRequest.setProductText("Test Description");
        RuleRequest.RuleQueryDto query = new RuleRequest.RuleQueryDto();
        query.setQuery("USER_OF");
        query.setArguments(List.of("DEBIT"));
        query.setNegate(false);
        ruleRequest.setRule(List.of(query));
    }

    @Test
    @DisplayName("Должен создать правило")
    void createRule_shouldSaveAndReturnRule() {
        // given
        RuleEntity savedEntity = new RuleEntity();
        savedEntity.setId(ruleId);
        savedEntity.setProductName("Test Product");
        savedEntity.setProductId(UUID.randomUUID());
        savedEntity.setProductText("Test Description");
        savedEntity.setQueries(List.of());

        when(ruleRepository.save(any(RuleEntity.class))).thenReturn(savedEntity);

        // when
        RuleResponse response = ruleService.createRule(ruleRequest);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(ruleId);
        assertThat(response.getProductName()).isEqualTo("Test Product");
        verify(ruleRepository, times(1)).save(any(RuleEntity.class));
    }

    @Test
    @DisplayName("Должен вернуть все правила")
    void getAllRules_shouldReturnListOfRules() {
        // given
        RuleEntity entity = new RuleEntity();
        entity.setId(ruleId);
        entity.setProductName("Test Product");
        entity.setProductId(UUID.randomUUID());
        entity.setProductText("Test Description");
        entity.setQueries(List.of());

        when(ruleRepository.findAll()).thenReturn(List.of(entity));

        // when
        List<RuleResponse> result = ruleService.getAllRules();

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getProductName()).isEqualTo("Test Product");
        verify(ruleRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Должен удалить правило")
    void deleteRule_shouldDeleteRule() {
        // given
        when(ruleRepository.existsById(ruleId)).thenReturn(true);
        doNothing().when(ruleRepository).deleteById(ruleId);

        // when
        ruleService.deleteRule(ruleId);

        // then
        verify(ruleRepository, times(1)).existsById(ruleId);
        verify(ruleRepository, times(1)).deleteById(ruleId);
    }

    @Test
    @DisplayName("Должен выбросить исключение при удалении несуществующего правила")
    void deleteRule_shouldThrowException_whenRuleNotFound() {
        // given
        when(ruleRepository.existsById(ruleId)).thenReturn(false);

        // then
        assertThatThrownBy(() -> ruleService.deleteRule(ruleId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Rule not found");
        verify(ruleRepository, never()).deleteById(any());
    }
}