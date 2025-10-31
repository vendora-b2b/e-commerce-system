package com.example.ecommerce.marketplace.web.quotation;

import com.example.ecommerce.marketplace.application.quotation.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.mockito.Mockito;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(QuotationController.class)
class QuotationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CreateQuotationRequestUseCase createQuotationRequestUseCase;

    @MockBean
    private SubmitQuotationOfferUseCase submitQuotationOfferUseCase;

    @Test
    void shouldCreateQuotationRequest() throws Exception {
        // given
        CreateQuotationRequestDTO requestDTO = new CreateQuotationRequestDTO();
        requestDTO.setRetailerId(1L);
        requestDTO.setSupplierId(2L);
        requestDTO.setValidUntil(LocalDateTime.now().plusDays(7));
        requestDTO.setNotes("Test notes");

        CreateQuotationRequestDTO.RequestItemDTO itemDTO = new CreateQuotationRequestDTO.RequestItemDTO();
        itemDTO.setProductId(1L);
        itemDTO.setQuantity(10);
        itemDTO.setSpecifications("Test specs");
        requestDTO.setItems(Collections.singletonList(itemDTO));

        CreateQuotationRequestResult result = new CreateQuotationRequestResult("QR-12345", 1L);
        when(createQuotationRequestUseCase.execute(any())).thenReturn(result);

        // when/then
        mockMvc.perform(post("/api/v1/quotations/requests")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.requestNumber").value("QR-12345"))
                .andExpect(jsonPath("$.requestId").value(1L));
    }

    @Test
    void shouldSubmitQuotationOffer() throws Exception {
        // given
        SubmitQuotationOfferDTO offerDTO = new SubmitQuotationOfferDTO();
        offerDTO.setQuotationRequestId(1L);
        offerDTO.setSupplierId(2L);
        offerDTO.setValidUntil(LocalDateTime.now().plusDays(7));
        offerDTO.setNotes("Test notes");
        offerDTO.setTermsAndConditions("Standard terms");

        SubmitQuotationOfferDTO.OfferItemDTO itemDTO = new SubmitQuotationOfferDTO.OfferItemDTO();
        itemDTO.setProductId(1L);
        itemDTO.setQuantity(10);
        itemDTO.setQuotedPrice(100.0);
        itemDTO.setSpecifications("Test specs");
        offerDTO.setItems(Collections.singletonList(itemDTO));

        SubmitQuotationOfferResult result = new SubmitQuotationOfferResult("QO-12345", 1L, 1000.0);
        when(submitQuotationOfferUseCase.execute(any())).thenReturn(result);

        // when/then
        mockMvc.perform(post("/api/v1/quotations/offers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(offerDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.offerNumber").value("QO-12345"))
                .andExpect(jsonPath("$.offerId").value(1L))
                .andExpect(jsonPath("$.totalAmount").value(1000.0));
    }

    @Test
    void shouldRejectInvalidQuotationRequest() throws Exception {
        // given
        CreateQuotationRequestDTO requestDTO = new CreateQuotationRequestDTO();
        // Missing required fields

        // when/then
        mockMvc.perform(post("/api/v1/quotations/requests")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectInvalidQuotationOffer() throws Exception {
        // given
        SubmitQuotationOfferDTO offerDTO = new SubmitQuotationOfferDTO();
        // Missing required fields

        // when/then
        mockMvc.perform(post("/api/v1/quotations/offers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(offerDTO)))
                .andExpect(status().isBadRequest());
    }
}