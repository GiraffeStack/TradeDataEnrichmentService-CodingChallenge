package com.bensonhoi.tradeenricher.service;

import com.bensonhoi.tradeenricher.model.Trade;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

import static java.lang.Integer.parseInt;
import static java.time.LocalDate.parse;
import static java.time.format.DateTimeFormatter.ofPattern;
import static java.util.Objects.requireNonNull;
import static org.apache.commons.csv.CSVFormat.DEFAULT;

@Service
public class TradeService implements ITradeService {

    public static final String MISSING_PRODUCT_NAME = "Missing Product Name";
    public static final String PRODUCT_CSV = "product.csv";
    public static final String PRODUCT_ID = "product_id";
    public static final String PRODUCT_NAME = "product_name";
    Logger logger = LoggerFactory.getLogger(TradeService.class);

    private static final DateTimeFormatter formatter = ofPattern("yyyyMMdd");

    void setLogger(Logger logger) {
        this.logger = logger;
    }

    public interface TradeCallback {
        void process(Trade trade);
    }

    public void enrichData(InputStream inputStream, TradeCallback callback) throws IOException {
        Map<Integer, String> productMap = loadProductsIntoMap();

        try (BufferedReader fileReader = new BufferedReader(new InputStreamReader(inputStream));
            CSVParser csvParser = new CSVParser(fileReader, DEFAULT.withFirstRecordAsHeader().withIgnoreHeaderCase().withTrim())) {

            for (CSVRecord csvRecord : csvParser) {
                Trade trade = getTrade(productMap, csvRecord);
                if (trade != null) {
                    callback.process(trade);
                }
            }
        }
    }

    private Trade getTrade(Map<Integer, String> productMap, CSVRecord csvRecord) {
        String date = csvRecord.get("date");
        try {
            requireNonNull(parse(date, formatter));
        } catch (DateTimeParseException e) {
            logger.error("Invalid date format: {}", date);
            return null;
        }

        int productId = parseInt(csvRecord.get("product_id"));

        String productName = productMap.getOrDefault(productId, MISSING_PRODUCT_NAME);
        if (MISSING_PRODUCT_NAME.equals(productName)) {
            logger.error("Missing product name for product id: {}", productId);
        }

        String currency = csvRecord.get("currency");
        String price = csvRecord.get("price");

        Trade trade = new Trade();
        trade.setDate(date);
        trade.setProductName(productName);
        trade.setCurrency(currency);
        trade.setPrice(Double.parseDouble(price));
        return trade;
    }

    private BufferedReader getBufferedReaderFromResourcesFolder(String filename) {
        return new BufferedReader(new InputStreamReader(requireNonNull(getClass().getClassLoader().getResourceAsStream(filename))));
    }

    private Map<Integer, String> loadProductsIntoMap() throws IOException {
        Map<Integer, String> productMap = new HashMap<>();

        try (BufferedReader fileReader = getBufferedReaderFromResourcesFolder(PRODUCT_CSV);
             CSVParser csvParser = new CSVParser(fileReader, DEFAULT.withFirstRecordAsHeader().withIgnoreHeaderCase().withTrim())) {

            for (CSVRecord csvRecord : csvParser) {
                int productId = parseInt(csvRecord.get(PRODUCT_ID));
                String productName = csvRecord.get(PRODUCT_NAME);

                productMap.put(productId, productName);
            }
        }

        return productMap;
    }
}