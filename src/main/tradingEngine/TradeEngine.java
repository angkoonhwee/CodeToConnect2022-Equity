package main.tradingEngine;

import main.marketSimulator.Bid;
import main.marketSimulator.Market;
import main.marketSimulator.OrderBook;

import java.util.Iterator;

/**
 * Trade Engine to decide how to slice the parent order into
 * smaller child orders base on Market Volume.
 */
public class TradeEngine {
    Market market;
    Order clientOrder;
    OrderBook orderBook;

    public TradeEngine(Market market, Order order) {
        this.market = market;
        this.clientOrder = order;

        orderBook = market.getOrderBook();
    }

    public void sliceOrder() {
        // market has just opened or order has been fulfilled, should use passive posting
        if (market.getCurrMarketVol() == 0 || market.getCurrMarketVol() == clientOrder.quantity) {
            Iterator<Bid> bids = orderBook.getBids().iterator();

            while (bids.hasNext()) {
                int quantity = (int) (bids.next().bidSize * clientOrder.targetPercentage);

//                Order order = new Order(quantity)
            }
        }
    }
}
