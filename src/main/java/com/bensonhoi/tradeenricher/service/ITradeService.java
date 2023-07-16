package com.bensonhoi.tradeenricher.service;

import java.io.IOException;
import java.io.InputStream;

public interface ITradeService {
    void enrichData(InputStream inputStream, TradeService.TradeCallback callback) throws IOException;
}
