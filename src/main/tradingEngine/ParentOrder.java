package main.tradingEngine;

/**
 * Parent order derived from FIX parser.
 */
public class ParentOrder extends Order {
    public enum orderType {
        SELL,
        BUY
    }
    public orderType type;
    public double targetPercentage;
    public double minRatio;
    public double maxRatio;
    private int idealVol;

    public ParentOrder(orderType type, int quantity, double targetPercentage) {
        super(quantity);
        this.type = type;
        this.targetPercentage = targetPercentage/100;
        this.minRatio = (0.8 * targetPercentage)/100;
        this.maxRatio = (1.2 * targetPercentage)/100;
    }

    public void setIdealVol(int marketVol) {
        this.idealVol = (int) (marketVol * targetPercentage);
    }
}
