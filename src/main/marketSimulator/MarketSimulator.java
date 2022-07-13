package main.marketSimulator;

import main.tradingEngine.ChildOrder;

import java.util.Queue;

/**
 * Keep track of orders and fill them when marketable.
 * Update parent order cumulative quantity.
 */
public class MarketSimulator {
    // TODO: Merge market into OrderBook.
    Queue<ChildOrder> orders;
    OrderBook orderBook;
}
