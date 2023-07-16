package com.bensonhoi.tradeenricher.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Trade {
    private String date;
    private String productName;
    private String currency;
    private double price;
}