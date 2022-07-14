package main.tradingEngine;

public class ChildOrder extends Order {
    public boolean isFilled;
    public double price;
    public actionType action;
    public int timestamp;
    public OrderKey key;

    public ChildOrder(int quantity, double price, actionType action, int timestamp) {
        super(quantity);

        this.price = price;
        this.action = action;
        this.isFilled = false;
        this.timestamp = timestamp;
        key = new OrderKey(action, price);
    }

    private String actionToString() {
        return action.equals(actionType.NEW)
                ? "N"
                : "C";
    }

    public void updateChildOrder(int quantity) {
        this.quantity = quantity;
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
