package main.tradingEngine;

import main.marketSimulator.Ask;
import main.marketSimulator.Bid;
import main.marketSimulator.Market;

import java.util.HashMap;
import java.util.Iterator;

/**
 * Trade Engine to decide how to slice the parent order into
 * smaller child orders based on Market Volume.
 */
public class TradeEngine {
    Market market;
    ParentOrder clientOrder;
    public HashMap<Order.OrderKey, ChildOrder> queuedOrders; // orders that are queued in simulator but yet to be filled

    public TradeEngine(Market market, ParentOrder order) {
        this.market = market;
        this.clientOrder = order;
        queuedOrders = new HashMap<>();
    }

    public void updateQueuedOrders(HashMap<Order.OrderKey, ChildOrder> updated) {
        this.queuedOrders = updated;
    }

    /**
     * Decides how to slice parent order into smaller child orders based on order book.
     * @return child orders, when executed with queued orders to maintain PoV
     */
    public HashMap<Order.OrderKey, ChildOrder> sliceOrder() {
        // limits on total child orders' quantity
        // --> client's desired quantity
        // --> min/max/target ratio
        // --> greater than 0
        // --> bid/ask size
        HashMap<Order.OrderKey, ChildOrder> childOrders = new HashMap<>();
        int potentialCumulativeQuantity = clientOrder.cumulativeQuantity;

        if (market.getCurrMarketVol() == 0 || market.getCurrMarketVol() == clientOrder.quantity) {
            childOrders.putAll(passivePosting(potentialCumulativeQuantity));

        } else if (clientOrder.cumulativeQuantity < market.getCurrMarketVol() * clientOrder.minRatio) {
            childOrders.putAll(aggressivePosting(potentialCumulativeQuantity));
            childOrders.putAll(passivePosting(potentialCumulativeQuantity));

        } else if (clientOrder.cumulativeQuantity > market.getCurrMarketVol() * clientOrder.maxRatio
                || potentialCumulativeQuantity >= clientOrder.quantity) {
            // cumulative quantity exceeds current target percentage. Cancel all orders.
            childOrders.putAll(cancelOrders(queuedOrders));
        }

        // cancel queued orders that do not fulfill the new strategy
        for (ChildOrder queued : queuedOrders.values()) {
            if (!childOrders.containsKey(queued.key)) {
                ChildOrder cancelOrder = new ChildOrder(
                        queued.quantity, queued.price, Order.actionType.CANCEL, market.getOrderBook().getTimeStamp());
                childOrders.put(cancelOrder.key, cancelOrder);
            }
        }

        // compare to queued orders
        HashMap<Order.OrderKey, ChildOrder> immutable = new HashMap<>(childOrders);
        for (ChildOrder curr : childOrders.values()) {
            // match child order, if possible, with existing queue orders
            ChildOrder queueOrder = queuedOrders.get(curr.key);

            if (queueOrder != null && curr.action.equals(Order.actionType.NEW)) {
                // Top up the difference
                if (curr.quantity > queueOrder.quantity) {
                    int difference = curr.quantity - queueOrder.quantity;
                    curr.updateChildOrder(difference);
                    // New order is smaller than original, cancel previous order
                } else if (curr.quantity < queueOrder.quantity) {
                    ChildOrder cancelOrder = new ChildOrder(queueOrder.quantity, queueOrder.price, Order.actionType.CANCEL,
                            market.getOrderBook().getTimeStamp());
                    immutable.put(cancelOrder.key, cancelOrder);
                } else {
                    immutable.remove(curr.key);
                }
            }
        }
        return immutable;
    }

    /**
     * Method invoked when breaching max. Cancel all queued orders.
     * @param childOrders queued orders
     * @return cancel order for each queued order
     */
    private HashMap<Order.OrderKey, ChildOrder> cancelOrders(HashMap<Order.OrderKey, ChildOrder> childOrders) {
        HashMap<Order.OrderKey, ChildOrder> cancelledOrders = new HashMap<>();
        Iterator<ChildOrder> childOrderIterator = childOrders.values().iterator();

        while (childOrderIterator.hasNext()) {
            ChildOrder currOrder = childOrderIterator.next();
            ChildOrder cancelledOrder = new ChildOrder(currOrder.quantity, currOrder.price,
                    Order.actionType.CANCEL, market.getOrderBook().getTimeStamp());
            cancelledOrders.put(currOrder.key, cancelledOrder);
        }

        return cancelledOrders;
    }

    /**
     * Method for passive posting.
     * @param potentialCumulativeQuantity current cumulative quantity
     * @return all passive posting orders that have a total quantity less than client's desired quantity
     */
    private HashMap<Order.OrderKey, ChildOrder> passivePosting(int potentialCumulativeQuantity) {

        HashMap<Order.OrderKey, ChildOrder> childOrders = new HashMap<>();
        Iterator<Bid> bids = market.getOrderBook().getBids().iterator();

        while (bids.hasNext() && potentialCumulativeQuantity < clientOrder.quantity) {
            Bid currBid = bids.next();
            int quantity = Math.min(clientOrder.quantity - potentialCumulativeQuantity,
                    (int) (currBid.bidSize * clientOrder.targetPercentage));
            double price = currBid.bidPrice;

            if (potentialCumulativeQuantity + quantity > clientOrder.quantity || quantity == 0) {
                continue;
            }

            ChildOrder order = new ChildOrder(quantity, price, Order.actionType.NEW,
                    market.getOrderBook().getTimeStamp());
            childOrders.put(order.key, order);

            potentialCumulativeQuantity += quantity;
        }

        return childOrders;
    }

    /**
     * Aggressive posting based on best asks.
     * @param potentialCumulativeQuantity current cumulative quantity
     * @return all aggressive posting orders such that total quantity is minRatio
     */
    private HashMap<Order.OrderKey, ChildOrder> aggressivePosting(int potentialCumulativeQuantity) {

        HashMap<Order.OrderKey, ChildOrder> childOrders = new HashMap<>();
        Iterator<Ask> asks = market.getOrderBook().getAsks().iterator();

        while (potentialCumulativeQuantity < market.getCurrMarketVol() * clientOrder.minRatio
                && potentialCumulativeQuantity < clientOrder.quantity
                && asks.hasNext() ) {

            Ask currAsk = asks.next();

            // if shortfall is greater than currAsk size, quantity placed is limited to ask size
            int quantity = Math.min(
                    currAsk.askSize,
                    Math.min(clientOrder.quantity,
                            (int) (market.getCurrMarketVol() * clientOrder.minRatio)
                    ) - potentialCumulativeQuantity);
            double price = currAsk.askPrice;

            if (potentialCumulativeQuantity + quantity > clientOrder.quantity || quantity == 0) {
                continue;
            }

            ChildOrder order = new ChildOrder(quantity, price, Order.actionType.NEW,
                    market.getOrderBook().getTimeStamp());
            childOrders.put(order.key, order);

            potentialCumulativeQuantity += quantity;
        }

        return childOrders;
    }
}
