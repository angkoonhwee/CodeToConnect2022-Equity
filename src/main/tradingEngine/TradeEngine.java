package main.tradingEngine;

import main.logger.EquityLogger;
import main.marketSimulator.Ask;
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
    EquityLogger logger;

    public TradeEngine(Market market, ParentOrder order, EquityLogger logger) {
        this.market = market;
        this.clientOrder = order;
        this.logger = logger;
    }

    public void sliceOrder() {
        OrderBook orderBook = market.getOrderBook();
        Iterator<Bid> bids = orderBook.getBids().iterator();
        Iterator<Ask> asks = orderBook.getAsks().iterator();
        ArrayList<ChildOrder> childOrders = new ArrayList<>();
        int potentialCumulativeQuantity = clientOrder.cumulativeQuantity;

        if (market.getCurrMarketVol() == 0
                || market.getCurrMarketVol() == clientOrder.quantity) {
            // market has just opened or order has been fulfilled, passive posting
            while (bids.hasNext()) {
                Bid currBid = bids.next();
                int quantity = (int) (currBid.bidSize * clientOrder.targetPercentage);
                double price = currBid.bidPrice;

                ChildOrder order = new ChildOrder(quantity, price, Order.actionType.NEW);
                childOrders.add(order);

                potentialCumulativeQuantity += quantity;
            }
        } else if (clientOrder.cumulativeQuantity < market.getCurrMarketVol() * clientOrder.minRatio) {
            // Cumulative order is lower than minimum ratio.
            // Aggressive buys till shortfall is covered.
            while (potentialCumulativeQuantity < market.getCurrMarketVol() * clientOrder.minRatio
                    && asks.hasNext()) {
                Ask currAsk = asks.next();

                // if shortfall is greater than currAsk size, quantity placed is limited to ask size
                int shortfall = Math.max(
                        currAsk.askSize,
                        (int) (market.getCurrMarketVol() * clientOrder.minRatio) - potentialCumulativeQuantity);
                
                double price = currAsk.askPrice;

                ChildOrder order = new ChildOrder(shortfall, price, Order.actionType.NEW);
                childOrders.add(order);

                potentialCumulativeQuantity += shortfall;
            }

            // For passive posting.
            while (potentialCumulativeQuantity == clientOrder.quantity || bids.hasNext()) {
                Bid currBid = bids.next();
                int quantity = (int) (currBid.bidSize * clientOrder.targetPercentage);
                double price = currBid.bidPrice;

                ChildOrder order = new ChildOrder(quantity, price, Order.actionType.NEW);
                childOrders.add(order);

                potentialCumulativeQuantity += quantity;
            }
        } // breach max case
        logger.logOrders(childOrders);
    }
}
