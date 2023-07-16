package com.bensonhoi.tradeenricher.controller;

import com.bensonhoi.tradeenricher.model.Trade;
import com.bensonhoi.tradeenricher.service.TradeService;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static java.util.stream.Collectors.joining;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.http.HttpStatus.OK;

public class TradeControllerTest {

    @Test
    public void testEnrichData() throws IOException {
        MockMultipartFile file = new MockMultipartFile("trade.csv", getRawData().getBytes());
        TradeController controller = new TradeController(new TradeService());
        ResponseEntity<InputStreamResource> response = controller.enrichTradeData(file);
        assertEquals(OK, response.getStatusCode());
        InputStream stream = Objects.requireNonNull(response.getBody()).getInputStream();

        String responseString = new BufferedReader(new InputStreamReader(stream))
                .lines().collect(joining("\n"));
        assertEquals(getExpectedResponse(), responseString);
    }

    private String getRawData() {
        return "date,product_id,currency,price\n20160101,1,EUR,10.0\n20160101,2,EUR,20.1\n20160101,3,EUR,30.34\n20160101,11,EUR,35.34";
    }

    private List<Trade> getExpectedTrades() {
        ArrayList<Trade> expectedTrades = new ArrayList<>();
        expectedTrades.add(new Trade("20160101", "Treasury Bills Domestic", "EUR", 10.0));
        expectedTrades.add(new Trade("20160101", "Corporate Bonds Domestic", "EUR", 20.1));
        expectedTrades.add(new Trade("20160101", "REPO Domestic", "EUR", 30.34));
        expectedTrades.add(new Trade("20160101", "Missing Product Name", "EUR", 35.34));
        return expectedTrades;
    }

    private String getExpectedResponse() {
        return "date,product_name,currency,price\n" +
                "20160101,Treasury Bills Domestic,EUR,10.0\n" +
                "20160101,Corporate Bonds Domestic,EUR,20.1\n" +
                "20160101,REPO Domestic,EUR,30.34\n" +
                "20160101,Missing Product Name,EUR,35.34";
    }
}
