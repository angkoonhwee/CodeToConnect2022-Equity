package main.tradingEngine;

public class Order {
    public enum actionType {
        NEW,
        CANCEL
    }
    public actionType action;
    public int price;
    public int quantity;
    public double targetPercentage;
    public double minRatio;
    public double maxRatio;
    private int idealVol;

    public enum orderType {
        BUY,
        SELL
    }

    public orderType type;

    public Order(orderType type, int quantity, double targetPercentage) {
        this.type = type;
        this.quantity = quantity;
        this.targetPercentage = targetPercentage/100;
        this.minRatio = (0.8 * targetPercentage)/100;
        this.maxRatio = (1.2 * targetPercentage)/100;
    }

    public void setIdealVol(int marketVol) {
        this.idealVol = (int) (marketVol * targetPercentage);
    }

    @Override
    public String toString() {
        return "[Order quantity: " + quantity
                + "; Target Percentage: " + targetPercentage
                + "]";
    }
}
