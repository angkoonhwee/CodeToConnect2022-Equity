package main.tradingEngine;

public class ChildOrder extends Order {
    public boolean isFilled;
    public double price;
    public actionType action;
    public int timestamp;

    public ChildOrder(int quantity, double price, actionType action) {
        super(quantity);

        this.price = price;
        this.action = action;
        this.isFilled = false;
    }

    private String actionToString() {
        return action.equals(actionType.NEW)
                ? "N"
                : "C";
    }

    public void fillOrder() {
        this.isFilled = true;
    }

    @Override
    public String toString() {
        String result = isFilled
                ? quantity + "@" + price
                : "[" + actionToString()
                + ":" + price
                + ":" + quantity
                + "]";
        return result;
    }
}
