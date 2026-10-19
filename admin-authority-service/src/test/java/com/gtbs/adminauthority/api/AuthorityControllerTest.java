package com.gtbs.adminauthority.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.gtbs.adminauthority.api.dto.AppliedInstructionResponse;
import com.gtbs.adminauthority.api.dto.BookingVerificationResponse;
import com.gtbs.adminauthority.api.dto.ClosureRequest;
import com.gtbs.adminauthority.service.AuthorityInstructionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(
        controllers = AuthorityController.class,
        excludeAutoConfiguration = {SecurityAutoConfiguration.class, OAuth2ResourceServerAutoConfiguration.class})
class AuthorityControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthorityInstructionService authorityInstructionService;

    @Test
    void postClosureReturnsCreated() throws Exception {
        var req = new ClosureRequest(
                "corr-1",
                "jur-1",
                List.of("seg-1"),
                Instant.parse("2026-04-01T12:00:00Z"),
                null,
                "construction",
                null);
        when(authorityInstructionService.submitClosure(any())).thenReturn(
                new AppliedInstructionResponse(42L, "corr-1", "pol-9"));

        mockMvc.perform(post("/authority/closures")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.mapVersion").value(42))
                .andExpect(jsonPath("$.correlationId").value("corr-1"));
    }

    @Test
    void verifyBookingProxies() throws Exception {
        when(authorityInstructionService.verifyBooking(eq("bk-1")))
                .thenReturn(new BookingVerificationResponse(
                        "bk-1",
                        "CONFIRMED",
                        7L,
                        Instant.parse("2026-04-01T08:00:00Z"),
                        Instant.parse("2026-04-01T09:00:00Z"),
                        List.of("seg-a"),
                        "proof-token"));

        mockMvc.perform(get("/authority/bookings/bk-1/verify"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bookingId").value("bk-1"))
                .andExpect(jsonPath("$.status").value("CONFIRMED"));
    }
}
