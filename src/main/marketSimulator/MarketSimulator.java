package main.marketSimulator;

import main.logger.EquityLogger;
import main.tradingEngine.ChildOrder;
import main.tradingEngine.Order;
import main.tradingEngine.ParentOrder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Keep track of orders and fill them when marketable.
 * Update parent order cumulative quantity.
 */
public class MarketSimulator {
    ParentOrder clientOrder;
    HashMap<Double, ChildOrder> orders;
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
    public HashMap<Double, ChildOrder> fillOrders(HashMap<Double, ChildOrder> childOrders) {
        Iterator<Ask> asks = market.getOrderBook().getAsks().iterator();

        while (asks.hasNext()) {
            Ask curr = asks.next();

            // Check marketable orders, i.e. orders buying at ask price
            if (childOrders.containsKey(curr.askPrice)) {
                ChildOrder childOrder = childOrders.get(curr.askPrice);

                // if cancel order, remove from order queue.
                if (childOrder.action.equals(Order.actionType.CANCEL)) {
                    childOrders.remove(curr.askPrice);
                    continue;
                }
                // order is marketable. Fill order.
                childOrder.fillOrder();
                clientOrder.updateCumulativeQuatity(childOrder.quantity);
                logger.logFills(childOrder, clientOrder.cumulativeQuantity);

                // remove filled order from queue.
                childOrders.remove(curr.askPrice);
            }
        }

        this.orders = childOrders;
        return orders;
    }

    /**
     * Merge recent orders and past orders if they have the same price.
     * @param recentOrder
     * @return
     */
    private HashMap<Double, ChildOrder> mergeOrders(HashMap<Double, ChildOrder> recentOrder) {
        return null;
    }
}
