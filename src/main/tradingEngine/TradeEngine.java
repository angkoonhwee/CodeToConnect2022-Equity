package main.tradingEngine;

import main.logger.EquityLogger;
import main.marketSimulator.Ask;
import main.marketSimulator.Bid;
import main.marketSimulator.Market;
import main.marketSimulator.OrderBook;

import java.util.HashMap;
import java.util.Iterator;

/**
 * Trade Engine to decide how to slice the parent order into
 * smaller child orders base on Market Volume.
 */
public class TradeEngine {
    Market market;
    ParentOrder clientOrder;
    EquityLogger logger;
    HashMap<Double, ChildOrder> queuedOrders;

    public TradeEngine(Market market, ParentOrder order, EquityLogger logger) {
        this.market = market;
        this.clientOrder = order;
        this.logger = logger;
        queuedOrders = new HashMap<>();
    }

    public void updateQueuedOrders(HashMap<Double, ChildOrder> updated) {
        this.queuedOrders = updated;
    }

    public HashMap<Double, ChildOrder> sliceOrder() {
        OrderBook orderBook = market.getOrderBook();
        Iterator<Bid> bids = orderBook.getBids().iterator();
        Iterator<Ask> asks = orderBook.getAsks().iterator();
        HashMap<Double, ChildOrder> childOrders = new HashMap();
        int potentialCumulativeQuantity = clientOrder.cumulativeQuantity;

        if (market.getCurrMarketVol() == 0
                || market.getCurrMarketVol() == clientOrder.quantity) {
            // market has just opened or order has been fulfilled, passive posting
            while (bids.hasNext()) {
                Bid currBid = bids.next();
                System.out.println(currBid);
                int quantity = (int) (currBid.bidSize * clientOrder.targetPercentage);
                double price = currBid.bidPrice;

                ChildOrder order = new ChildOrder(quantity, price, Order.actionType.NEW);
                childOrders.put(price, order);

                potentialCumulativeQuantity += quantity;
            }
        } else if (clientOrder.cumulativeQuantity < market.getCurrMarketVol() * clientOrder.minRatio) {
            // Cumulative order is lower than minimum ratio.
            // Aggressive buys till shortfall is covered.
            while (potentialCumulativeQuantity < market.getCurrMarketVol() * clientOrder.minRatio
                    && asks.hasNext()) {
                Ask currAsk = asks.next();
                System.out.println(currAsk);

                // if shortfall is greater than currAsk size, quantity placed is limited to ask size
                int shortfall = Math.min(
                        currAsk.askSize,
                        (int) (market.getCurrMarketVol() * clientOrder.minRatio) - potentialCumulativeQuantity);
                double price = currAsk.askPrice;

                ChildOrder order = new ChildOrder(shortfall, price, Order.actionType.NEW);
                childOrders.put(price, order);

                potentialCumulativeQuantity += shortfall;
            }

            // For passive posting.
            while (potentialCumulativeQuantity != clientOrder.quantity && bids.hasNext()) {
                Bid currBid = bids.next();
                System.out.println(currBid);
                int quantity = (int) (currBid.bidSize * clientOrder.targetPercentage);
                double price = currBid.bidPrice;

                ChildOrder order = new ChildOrder(quantity, price, Order.actionType.NEW);
                childOrders.put(price, order);

                potentialCumulativeQuantity += quantity;
            }
        } else if (clientOrder.cumulativeQuantity > market.getCurrMarketVol() * clientOrder.maxRatio) {
            // cumulative quantity exceeds current target percentage. Cancel all orders.
            childOrders = cancelOrders(queuedOrders);
        }
        logger.logOrders(childOrders);
        return childOrders;
    }

    private HashMap<Double, ChildOrder> cancelOrders(HashMap<Double, ChildOrder> childOrders) {
        HashMap<Double, ChildOrder> cancelledOrders = new HashMap<>();
        Iterator<ChildOrder> childOrderIterator = childOrders.values().iterator();
        while (childOrderIterator.hasNext()) {
            ChildOrder currOrder = childOrderIterator.next();
            ChildOrder cancelledOrder = new ChildOrder(currOrder.quantity, currOrder.price, Order.actionType.CANCEL);
            cancelledOrders.put(currOrder.price, cancelledOrder);
        }
        // inform the simulator to clear the queue of orders.

        return cancelledOrders;
    }
}
