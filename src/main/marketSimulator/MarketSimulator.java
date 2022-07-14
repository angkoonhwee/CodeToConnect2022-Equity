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
    HashMap<Order.OrderKey, ChildOrder> orders;
    Market market;
    EquityLogger logger;

    public MarketSimulator(Market market, ParentOrder clientOrder, EquityLogger logger) {
        this.market = market;
        this.clientOrder = clientOrder;
        this.logger = logger;
    }

    /**
     * Fill all marketable orders based on current quote.
     * Cancel orders respectively.
     * @param childOrders orders placed by Trade Engine based on current quote.
     * @return unfilled orders to Trade Engine so that Trade Engine can decide next strategy.
     */
    public HashMap<Order.OrderKey, ChildOrder> fillOrders(HashMap<Order.OrderKey, ChildOrder> childOrders) {

        for (Ask curr : market.getOrderBook().getAsks()) {
            // Check marketable orders, i.e. orders buying at ask price
            Order.OrderKey checkKey = new Order.OrderKey(Order.actionType.NEW, curr.askPrice);
            if (childOrders.containsKey(checkKey)) {
                ChildOrder childOrder = childOrders.get(checkKey);

                // order is marketable. Fill order.
                childOrder.fillOrder();
                clientOrder.updateCumulativeQuatity(childOrder.quantity);
                // log filled orders
                logger.logFills(childOrder, clientOrder.cumulativeQuantity);
            }

            // remove filled order and cancelled order from queue.
            childOrders.remove(checkKey);
        }

        this.orders = childOrders;
        return orders;
    }

    /**
     * Merge recent orders and past orders if they have the same price.
     * @param recentOrder newly placed orders by trade engine
     * @return top ups of previous orders and rest of the orders, cancel corresponding orders.
     */
    private HashMap<Order.OrderKey, ChildOrder> mergeOrders(HashMap<Order.OrderKey, ChildOrder> recentOrder) {

        for (ChildOrder curr : recentOrder.values()) {
            ChildOrder queuedOrder = orders.get(curr.key);

            if (queuedOrder != null) {
                if (queuedOrder.action.equals(Order.actionType.NEW)) {

                }
            }
        }
        return null;
    }
}
