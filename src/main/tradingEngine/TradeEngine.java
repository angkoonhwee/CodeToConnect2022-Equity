package main.tradingEngine;

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
    HashMap<Order.OrderKey, ChildOrder> queuedOrders; // orders that are queued in simulator but yet to be filled

    public TradeEngine(Market market, ParentOrder order) {
        this.market = market;
        this.clientOrder = order;
        queuedOrders = new HashMap<>();
    }

    public void updateQueuedOrders(HashMap<Order.OrderKey, ChildOrder> updated) {
        this.queuedOrders = updated;
    }

    public HashMap<Order.OrderKey, ChildOrder> sliceOrder() {
        OrderBook orderBook = market.getOrderBook();
        Iterator<Bid> bids = orderBook.getBids().iterator();
        Iterator<Ask> asks = orderBook.getAsks().iterator();
        HashMap<Order.OrderKey, ChildOrder> childOrders = new HashMap<>();
        int potentialCumulativeQuantity = clientOrder.cumulativeQuantity;

        if (market.getCurrMarketVol() == 0
                || market.getCurrMarketVol() == clientOrder.quantity) {
            // market has just opened or order has been fulfilled, passive posting
            while (bids.hasNext() && clientOrder.quantity != potentialCumulativeQuantity) {
                Bid currBid = bids.next();
                System.out.println(currBid);
                int quantity = Math.min(clientOrder.quantity - potentialCumulativeQuantity,
                        (int) (currBid.bidSize * clientOrder.targetPercentage));
                double price = currBid.bidPrice;

                ChildOrder order = new ChildOrder(quantity, price, Order.actionType.NEW);
                childOrders.put(order.key, order);

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
                childOrders.put(order.key, order);

                potentialCumulativeQuantity += shortfall;
            }

            // For passive posting.
            while (potentialCumulativeQuantity != clientOrder.quantity && bids.hasNext()) {
                Bid currBid = bids.next();
                System.out.println(currBid);
                int quantity = (int) (currBid.bidSize * clientOrder.targetPercentage);
                double price = currBid.bidPrice;

                ChildOrder order = new ChildOrder(quantity, price, Order.actionType.NEW);
                childOrders.put(order.key, order);

                potentialCumulativeQuantity += quantity;
            }
        } else if (clientOrder.cumulativeQuantity > market.getCurrMarketVol() * clientOrder.maxRatio) {
            // cumulative quantity exceeds current target percentage. Cancel all orders.
            childOrders = cancelOrders(queuedOrders);
        }

        // compare to queued orders
        for (ChildOrder curr : childOrders.values()) {
            // match child order, if possible, with existing queue orders
            ChildOrder queueOrder = queuedOrders.get(curr.key);
            System.out.println("Before: " + childOrders.values());

            if (queueOrder != null) {
                // Top up the difference
                if (curr.action.equals(Order.actionType.NEW)
                        && curr.quantity > queueOrder.quantity) {
                    int difference = curr.quantity - queueOrder.quantity;
                    curr.updateChildOrder(difference);
                    System.out.println("After top up: " + childOrders.values());
                    // New order is smaller than original, cancel previous order
                } else if (curr.action.equals(Order.actionType.NEW)
                        && curr.quantity < queueOrder.quantity) {
                    ChildOrder cancelOrder = new ChildOrder(curr.quantity, curr.price, Order.actionType.CANCEL);
                    childOrders.put(cancelOrder.key, cancelOrder);
                }
                // should not have cancelled order case in queued order.
                // Cancelled order can be fulfilled immediately by removing corresponding buy order from queue.
            }
        }
        return childOrders;
    }

    private HashMap<Order.OrderKey, ChildOrder> cancelOrders(HashMap<Order.OrderKey, ChildOrder> childOrders) {
        HashMap<Order.OrderKey, ChildOrder> cancelledOrders = new HashMap<>();
        Iterator<ChildOrder> childOrderIterator = childOrders.values().iterator();
        while (childOrderIterator.hasNext()) {
            ChildOrder currOrder = childOrderIterator.next();
            ChildOrder cancelledOrder = new ChildOrder(currOrder.quantity, currOrder.price, Order.actionType.CANCEL);
            cancelledOrders.put(currOrder.key, cancelledOrder);
        }
        // inform the simulator to clear the queue of orders.

        return cancelledOrders;
    }
}
