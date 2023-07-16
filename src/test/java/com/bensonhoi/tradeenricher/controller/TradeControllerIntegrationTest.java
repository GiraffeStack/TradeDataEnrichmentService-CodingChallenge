package com.bensonhoi.tradeenricher.controller;

import org.junit.Ignore;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import static java.lang.System.currentTimeMillis;
import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class TradeControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void test_valid_trades_file_uploaded()
            throws Exception {
        mockMvc.perform(multipart("/api/v1/enrich").file(new MockMultipartFile(
                        "file",
                        "trade.csv",
                        "text/csv",
                        new ClassPathResource("trade.csv").getInputStream()
                )))
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/csv"))
                .andExpect(content().string("date,product_name,currency,price\n"
                        + "20160101,Treasury Bills Domestic,EUR,10.0\n"
                        + "20160101,Corporate Bonds Domestic,EUR,20.1\n"
                        + "20160101,REPO Domestic,EUR,30.34\n"
                        + "20160101,Missing Product Name,EUR,35.34\n"));
    }

    // @Test
    @Ignore("This test takes a long time to run and can be elegantly configured to run as part of a CI/CD pipeline")
    public void test_trades_file_with_one_million_rows_uploaded()
            throws Exception {
        long requestStartTime = currentTimeMillis();

        mockMvc.perform(multipart("/api/v1/enrich").file(new MockMultipartFile(
                        "file",
                        "trade.csv",
                        "text/csv",
                        generateRandomCsvDataRows(5000000)
                )))
                .andExpect(status().isOk());

        long requestExecutionTime = currentTimeMillis() - requestStartTime;

        assertTrue("requestExecutionTime was " + requestExecutionTime, requestExecutionTime < 50000);
    }

    private InputStream generateRandomCsvDataRows(int numberOfRows) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        outputStream.write("date,product_id,currency,price\n".getBytes());
        for(int i = 0; i< numberOfRows; i++) {
            outputStream.write("20160101,1,EUR,10.0\n".getBytes());
        }

        return new ByteArrayInputStream(outputStream.toByteArray());
    }

}
