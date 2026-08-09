package com.example.star.controller;

import com.example.star.dto.RuleRequest;
import com.example.star.dto.RuleResponse;
import com.example.star.service.RuleService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RuleController.class)
@DisplayName("Тесты RuleController")
class RuleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RuleService ruleService;

    @Autowired
    private ObjectMapper objectMapper;

    private RuleRequest ruleRequest;
    private RuleResponse ruleResponse;

    @BeforeEach
    void setUp() {
        ruleRequest = new RuleRequest();
        ruleRequest.setProductName("Test Product");
        ruleRequest.setProductId(UUID.randomUUID());
        ruleRequest.setProductText("Test Description");

        RuleRequest.RuleQueryDto query = new RuleRequest.RuleQueryDto();
        query.setQuery("USER_OF");
        query.setArguments(List.of("DEBIT"));
        query.setNegate(false);
        ruleRequest.setRule(List.of(query));

        ruleResponse = new RuleResponse(
                UUID.randomUUID(),
                "Test Product",
                UUID.randomUUID(),
                "Test Description",
                List.of()
        );
    }

    @Test
    @DisplayName("POST /rule — должен создать правило")
    void createRule_shouldReturnCreatedRule() throws Exception {
        when(ruleService.createRule(any(RuleRequest.class))).thenReturn(ruleResponse);

        mockMvc.perform(post("/rule")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ruleRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.product_name").value("Test Product"));

        verify(ruleService, times(1)).createRule(any(RuleRequest.class));
    }

    @Test
    @DisplayName("GET /rule — должен вернуть список правил")
    void getAllRules_shouldReturnListOfRules() throws Exception {
        when(ruleService.getAllRules()).thenReturn(List.of(ruleResponse));

        mockMvc.perform(get("/rule"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(1));

        verify(ruleService, times(1)).getAllRules();
    }

    @Test
    @DisplayName("DELETE /rule/{id} — должен удалить правило")
    void deleteRule_shouldReturnNoContent() throws Exception {
        doNothing().when(ruleService).deleteRule(any(UUID.class));

        mockMvc.perform(delete("/rule/" + UUID.randomUUID()))
                .andExpect(status().isNoContent());

        verify(ruleService, times(1)).deleteRule(any(UUID.class));
    }
}