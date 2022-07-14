package main.marketSimulator;

import main.logger.EquityLogger;
import main.tradingEngine.ChildOrder;
import main.tradingEngine.Order;
import main.tradingEngine.ParentOrder;

import java.util.HashMap;

/**
 * Keep track of orders and fill them when marketable.
 * Update parent order cumulative quantity.
 */
public class MarketSimulator {
    private final static int THREE_MIN_IN_MS = 180000;

    ParentOrder clientOrder;
    HashMap<Order.OrderKey, ChildOrder> orders; // queued orders would not have cancel orders
    Market market;
    EquityLogger logger;
    int bestBidTime;

    public MarketSimulator(Market market, ParentOrder clientOrder, EquityLogger logger) {
        this.market = market;
        this.clientOrder = clientOrder;
        this.logger = logger;
        this.orders = new HashMap<>();
        bestBidTime = 0;
    }

    public HashMap<Order.OrderKey, ChildOrder> getQueuedOrders() {
        return this.orders;
    }

    /**
     * Fill all marketable orders based on current quote.
     * Cancel orders respectively.
     * @param childOrders orders placed by Trade Engine based on current quote.
     * @return unfilled orders to Trade Engine so that Trade Engine can decide next strategy.
     */
    public HashMap<Order.OrderKey, ChildOrder> fillOrders(HashMap<Order.OrderKey, ChildOrder> childOrders, int currTime) {
        HashMap<Order.OrderKey, ChildOrder> mergedOrders = mergeOrders(childOrders);

        // check current best bid price. if queued orders contain best bid price && time difference = 3 mins, fill order
        double bestBidPrice = getBestBid();
        if ((currTime - bestBidTime) >= THREE_MIN_IN_MS) {
            Order.OrderKey key = new Order.OrderKey(Order.actionType.NEW, bestBidPrice);
            ChildOrder stagnant = mergedOrders.get(key);
            logger.log("STAGNANT: " + stagnant);

            stagnant.fillOrder();
            clientOrder.updateCumulativeQuantity(stagnant.quantity);
            market.updateMarketVol(stagnant.quantity);

            logger.logFills(stagnant, clientOrder.cumulativeQuantity);

            mergedOrders.remove(key);
        }

        for (Ask curr : market.getOrderBook().getAsks()) {
            // Check marketable orders, i.e. orders buying at ask price
            Order.OrderKey checkKey = new Order.OrderKey(Order.actionType.NEW, curr.askPrice);
            if (mergedOrders.containsKey(checkKey)) {
                ChildOrder childOrder = mergedOrders.get(checkKey);

                // order is marketable. Fill order.
                childOrder.fillOrder();
                clientOrder.updateCumulativeQuantity(childOrder.quantity);
                market.updateMarketVol(childOrder.quantity);
                // log filled orders
                logger.logFills(childOrder, clientOrder.cumulativeQuantity);
            }

            // remove filled order and cancelled order from queue.
            mergedOrders.remove(checkKey);
        }

        this.orders = mergedOrders;
        return orders;
    }

    /**
     * Merge recent orders and past orders if they have the same price.
     * @param recentOrder newly placed orders by trade engine
     * @return top ups of previous orders and rest of the orders, cancel corresponding orders.
     */
    private HashMap<Order.OrderKey, ChildOrder> mergeOrders(HashMap<Order.OrderKey, ChildOrder> recentOrder) {

        HashMap<Order.OrderKey, ChildOrder> immutable = new HashMap<>(orders);
        for (ChildOrder curr : recentOrder.values()) {
            ChildOrder queuedOrder = orders.get(curr.key);

            // queued order would not have cancelled orders
            if (queuedOrder != null && queuedOrder.action.equals(Order.actionType.NEW)) {
                // new orders with same key, merge quantity
                int total = queuedOrder.quantity + curr.quantity;
                logger.log("TOP UP TOTAL: " + total);
                curr.updateChildOrder(total);
            } else if (curr.action.equals(Order.actionType.CANCEL)) {
                immutable.remove(new Order.OrderKey(Order.actionType.NEW, curr.price));
                continue;
            }
            immutable.put(curr.key, curr);
        }

        return immutable;
    }

    private double getBestBid() {
        double highestBidPrice = 0;
        if (orders.isEmpty()) {
            bestBidTime = market.getOrderBook().getTimeStamp();
        }
        for (ChildOrder order: orders.values()) {
            double temp = Math.max(highestBidPrice, order.price);
            // new highest bid
            if (temp != highestBidPrice) {
                highestBidPrice = temp;
                // update best bid time
                bestBidTime = order.timestamp;
            }
        }
        return highestBidPrice;
    }
}
