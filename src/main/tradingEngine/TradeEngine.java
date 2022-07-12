package main.tradingEngine;

import main.logger.EquityLogger;
import main.marketSimulator.Bid;
import main.marketSimulator.Market;
import main.marketSimulator.OrderBook;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Trade Engine to decide how to slice the parent order into
 * smaller child orders base on Market Volume.
 */
public class TradeEngine {
    Market market;
    ParentOrder clientOrder;
    OrderBook orderBook;
    EquityLogger logger;

    public TradeEngine(Market market, ParentOrder order, EquityLogger logger) {
        this.market = market;
        this.clientOrder = order;
        this.logger = logger;

        orderBook = market.getOrderBook();
    }

    public void sliceOrder() {
        ArrayList<ChildOrder> childOrders = new ArrayList<>();
        // market has just opened or order has been fulfilled, should use passive posting
        if (market.getCurrMarketVol() == 0 || market.getCurrMarketVol() == clientOrder.quantity) {
            Iterator<Bid> bids = orderBook.getBids().iterator();

            while (bids.hasNext()) {
                Bid currBid = bids.next();
                int quantity = (int) (currBid.bidSize * clientOrder.targetPercentage);
                double price = currBid.bidPrice;

                ChildOrder order = new ChildOrder(quantity, price, Order.actionType.NEW);
                childOrders.add(order);
            }

            logger.logOrders(childOrders);
        }
    }
}
