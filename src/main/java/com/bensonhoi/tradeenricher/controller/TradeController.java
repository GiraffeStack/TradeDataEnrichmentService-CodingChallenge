package com.bensonhoi.tradeenricher.controller;

import com.bensonhoi.tradeenricher.model.Trade;
import com.bensonhoi.tradeenricher.service.ITradeService;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PreDestroy;
import java.io.*;
import java.util.concurrent.ExecutorService;

import static java.util.concurrent.Executors.newFixedThreadPool;
import static org.springframework.http.MediaType.parseMediaType;
import static org.springframework.http.ResponseEntity.ok;

@Slf4j
@RestController
@RequestMapping("/api")
public class TradeController {

    public static final String HEADER = "date,product_name,currency,price\n";
    public static final MediaType MEDIA_TYPE = parseMediaType("text/csv");
    private final ITradeService tradeService;
    private final ExecutorService executorService;

    public TradeController(ITradeService tradeService) {
        this.tradeService = tradeService;
        this.executorService = newFixedThreadPool(108); // not getRuntime().availableProcessors() as the API is more IO bound
    }

    @PreDestroy
    public void cleanup() {
        if (executorService != null) {
            executorService.shutdown();
        }
    }

    /*
    Performant when there is a single user of the API
     */
    @PostMapping(value = "/v2/enrich", produces = "text/csv")
    public ResponseEntity<InputStreamResource> enrichTradeDataV2(@RequestParam("file") MultipartFile file) throws IOException {
        PipedInputStream inPipe = new PipedInputStream();
        PipedOutputStream outPipe = new PipedOutputStream(inPipe);
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outPipe));

        try {
            writer.write(HEADER);
            tradeService.enrichData(file.getInputStream(), trade -> {
                try {
                    writer.write(formatTrade(trade));
                } catch (IOException e) {
                    log.error("Error writing trade to output stream: {}", trade);
                }
            });
            writer.close();
        } catch (IOException e) {
            log.error("Error writing header to output stream", e);
        }

        return ok().contentType(MEDIA_TYPE).body(new InputStreamResource(inPipe));
    }

    /*
    Performant even when there are many users.
     */
    @PostMapping(value = "/v1/enrich", produces = "text/csv")
    public ResponseEntity<InputStreamResource> enrichTradeData(@RequestParam("file") MultipartFile file) throws IOException {
        PipedInputStream inPipe = new PipedInputStream();
        PipedOutputStream outPipe = new PipedOutputStream(inPipe);
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outPipe));

        executorService.submit(() -> {
            try {
                writer.write(HEADER);
                tradeService.enrichData(file.getInputStream(), trade -> {
                    try {
                        writer.write(formatTrade(trade));
                    } catch (IOException e) {
                        log.error("Error writing trade to output stream: {}", trade);
                    }
                });
                writer.close();
            } catch (IOException e) {
                log.error("Error writing header to output stream", e);
            }
        });

        return ok().contentType(MEDIA_TYPE).body(new InputStreamResource(inPipe));
    }

    private static String formatTrade(Trade trade) {
        return trade.getDate() + "," + trade.getProductName() + "," + trade.getCurrency() + "," + trade.getPrice() + "\n";
    }
}
