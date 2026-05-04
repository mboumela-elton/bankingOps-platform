package com.msel.app;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.msel.app.dto.CreateTransactionRequest;
import com.msel.app.dto.CreateUserRequest;
import com.msel.app.dto.UserDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class BankingOpsIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testHealthEndpoint() throws Exception {
        mockMvc.perform(get("/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    public void testReadyEndpoint() throws Exception {
        mockMvc.perform(get("/ready"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ready").value("true"));
    }

    @Test
    public void testCreateUserSuccess() throws Exception {
        CreateUserRequest request = CreateUserRequest.builder()
                .name("Jane Smith")
                .email("jane.smith@example.com")
                .build();

        mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.name").value("Jane Smith"))
                .andExpect(jsonPath("$.email").value("jane.smith@example.com"))
                .andExpect(jsonPath("$.created_at").isNotEmpty());
    }

    @Test
    public void testCreateUserValidationError() throws Exception {
        CreateUserRequest request = CreateUserRequest.builder()
                .name("")
                .email("invalid-email")
                .build();

        mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    public void testCompleteTransactionWorkflow() throws Exception {
        // 1. Create user
        CreateUserRequest userRequest = CreateUserRequest.builder()
                .name("Test User")
                .email("test@example.com")
                .build();

        MvcResult userResult = mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        UserDTO createdUser = objectMapper.readValue(
                userResult.getResponse().getContentAsString(),
                UserDTO.class);

        UUID userId = createdUser.getId();

        // 2. Get user
        mockMvc.perform(get("/users/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId.toString()));

        // 3. Create transaction
        CreateTransactionRequest transactionRequest = CreateTransactionRequest.builder()
                .userId(userId)
                .amount(new BigDecimal("150.75"))
                .currency("EUR")
                .build();

        MvcResult transactionResult = mockMvc.perform(post("/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(transactionRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andReturn();

        String transactionId = objectMapper.readTree(transactionResult.getResponse().getContentAsString())
                .get("id").asText();

        // 4. Get transaction
        mockMvc.perform(get("/transactions/{transactionId}", transactionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(transactionId));

        // 5. Get user transactions
        mockMvc.perform(get("/users/{userId}/transactions", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(transactionId));

        // 6. Validate transaction
        mockMvc.perform(post("/transactions/{transactionId}/validate", transactionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("VALIDATED"));

        // 7. Verify transaction is validated
        mockMvc.perform(get("/transactions/{transactionId}", transactionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("VALIDATED"));
    }

    @Test
    public void testFailTransaction() throws Exception {
        // Create user
        CreateUserRequest userRequest = CreateUserRequest.builder()
                .name("Fail Test User")
                .email("fail@example.com")
                .build();

        MvcResult userResult = mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        UserDTO createdUser = objectMapper.readValue(
                userResult.getResponse().getContentAsString(),
                UserDTO.class);

        // Create transaction
        CreateTransactionRequest transactionRequest = CreateTransactionRequest.builder()
                .userId(createdUser.getId())
                .amount(new BigDecimal("200.00"))
                .currency("GBP")
                .build();

        MvcResult transactionResult = mockMvc.perform(post("/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(transactionRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        String transactionId = objectMapper.readTree(transactionResult.getResponse().getContentAsString())
                .get("id").asText();

        // Fail transaction
        mockMvc.perform(post("/transactions/{transactionId}/fail", transactionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("FAILED"));

        // Verify transaction is failed
        mockMvc.perform(get("/transactions/{transactionId}", transactionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("FAILED"));
    }

    @Test
    public void testGetNonExistentUser() throws Exception {
        UUID nonExistentId = UUID.randomUUID();
        mockMvc.perform(get("/users/{userId}", nonExistentId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    public void testGetNonExistentTransaction() throws Exception {
        UUID nonExistentId = UUID.randomUUID();
        mockMvc.perform(get("/transactions/{transactionId}", nonExistentId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }
}
