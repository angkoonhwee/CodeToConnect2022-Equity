package tradingEngine;

public class Order {
    public enum actionType {
        NEW,
        CANCEL
    }
    public actionType action;
    public int price;
    public int quantity;
    public double targetPercentage;
    private int idealVol;

    public enum orderType {
        BUY,
        SELL
    }

    public orderType type;

    public Order(orderType type, int quantity, double targetPercentage) {
        this.type = type;
        this.quantity = quantity;
        this.targetPercentage = targetPercentage;
    }

    @Override
    public String toString() {
        return "[Order quantity: " + quantity
                + "; Target Percentage: " + targetPercentage
                + "]";
    }
}
