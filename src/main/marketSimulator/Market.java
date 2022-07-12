package main.marketSimulator;

public class Market {
    private OrderBook orderBook;
    private int currMarketVol = 0;

    public void setOrderBook(OrderBook orderBook) {
        this.orderBook = orderBook;
    }

    public OrderBook getOrderBook() {
        return orderBook;
    }

    public void updateMarketVol(int amount) {
        currMarketVol = currMarketVol + amount;
    }

    public int getCurrMarketVol() {
        return currMarketVol;
    }
}
