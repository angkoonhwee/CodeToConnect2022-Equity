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

    public static class OrderKey {
        actionType type;
        double price;

        public OrderKey(actionType type, double price) {
            this.type = type;
            this.price = price;
        }

        public OrderKey getOrderKey() {
            return this;
        }

        @Override
        public boolean equals(Object o) {
            if (o == this) {
                return true;
            }

            if (!(o instanceof OrderKey)) {
                return false;
            }

            OrderKey anotherKey = (OrderKey) o;
            return anotherKey.type.equals(this.type) && anotherKey.equals(this.price);
        }
    }
}
