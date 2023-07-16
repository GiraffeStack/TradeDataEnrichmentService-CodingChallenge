package com.bensonhoi.tradeenricher.service;

import com.bensonhoi.tradeenricher.model.Trade;
import org.slf4j.Logger;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.mock.web.MockMultipartFile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.io.FileInputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class TradeServiceTest {

    private TradeService tradeService;
    private List<Trade> results;
    private MultipartFile file;

    private Logger logger;

    @BeforeEach
    public void setup() throws IOException {
        tradeService = new TradeService();
        logger = mock(Logger.class);
        tradeService.setLogger(logger);

        results = new ArrayList<>();
        FileInputStream inputFile = new FileInputStream("src/test/resources/trade.csv");
        file = new MockMultipartFile("file", "trade.csv", "text/csv", inputFile);
    }

    @Test
    public void testEnrichData_ValidTrades() throws IOException {
        TradeService.TradeCallback callback = trade -> results.add(trade);
        tradeService.enrichData(file.getInputStream(), callback);
        assertEquals(4, results.size());

        Trade firstTrade = results.get(0);
        assertEquals("20160101", firstTrade.getDate());
        assertEquals("Treasury Bills Domestic", firstTrade.getProductName());
        assertEquals("EUR", firstTrade.getCurrency());
        assertEquals(10.0, firstTrade.getPrice());
    }

    @Test
    public void testEnrichData_InvalidTrades() throws IOException {
        TradeService.TradeCallback callback = trade -> {};
        tradeService.enrichData(file.getInputStream(), callback);

        verify(logger).error(eq("Invalid date format: {}"), eq("X0160101"));
        verify(logger).error(eq("Missing product name for product id: {}"), eq(11));
    }
}
