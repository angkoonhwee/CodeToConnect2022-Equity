package main.tradingEngine;

public class Order {
    public enum actionType {
        NEW,
        CANCEL
    }
    public actionType action;
    public int quantity;

    public Order(int quantity) {
        this.quantity = quantity;
    }
}
