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
    ParentOrder clientOrder;
    HashMap<Order.OrderKey, ChildOrder> orders; // queued orders would not have cancel orders
    Market market;
    EquityLogger logger;
    ChildOrder bestBid;
    int bestBidTimer;

    public MarketSimulator(Market market, ParentOrder clientOrder, EquityLogger logger) {
        this.market = market;
        this.clientOrder = clientOrder;
        this.logger = logger;
        this.orders = new HashMap<>();
    }

    public void updateQueuedOrders(HashMap<Order.OrderKey, ChildOrder> updated) {
        this.orders.putAll(updated);
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
    public HashMap<Order.OrderKey, ChildOrder> fillOrders(HashMap<Order.OrderKey, ChildOrder> childOrders) {
        HashMap<Order.OrderKey, ChildOrder> mergedOrders = mergeOrders(childOrders);

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
                curr.updateChildOrder(queuedOrder.quantity + curr.quantity);
            } else if (curr.action.equals(Order.actionType.CANCEL)) {
                immutable.remove(new Order.OrderKey(Order.actionType.NEW, curr.price));
                continue;
            }
            immutable.put(curr.key, curr);
        }

        return immutable;
    }
}
