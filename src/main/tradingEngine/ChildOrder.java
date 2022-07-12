package main.tradingEngine;

public class ChildOrder extends Order {
    public double price;
    public actionType action;

    public ChildOrder(int quantity, double price, actionType action) {
        super(quantity);

        this.price = price;
        this.action = action;
    }

    private String actionToString() {
        return action.equals(actionType.NEW)
                ? "N"
                : "C";
    }

    @Override
    public String toString() {
        return "[" + actionToString()
                + ":" + price
                + ":" + quantity
                + "]";
    }
}
