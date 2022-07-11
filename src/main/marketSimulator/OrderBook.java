package main.marketSimulator;


import java.util.PriorityQueue;
import java.util.TreeSet;

public class OrderBook {
    int marketVolume;
    // ordered by bid price (highest is top priority) for bid side of order book
    // max size = 3
    TreeSet<Bid> bids;
    // ordered by ask price (lowest is top priority) for ask side order book
    // max size = 3
    TreeSet<Ask> asks;
    String timeStamp;

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    public void setBids(TreeSet<Bid> bids) {
        this.bids = bids;
    }

    public void setAsks(TreeSet<Ask> asks) {
        this.asks = asks;
    }

    public TreeSet<Bid> getBids() {
        return bids;
    }

    public TreeSet<Ask> getAsks() {
        return asks;
    }
}
