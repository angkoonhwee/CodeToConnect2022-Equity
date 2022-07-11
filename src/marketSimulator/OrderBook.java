package marketSimulator;


import java.util.PriorityQueue;

public class OrderBook {
    int marketVolume;
    // ordered by bid price (highest is top priority) for bid side of order book
    // max size = 3
    PriorityQueue<Bid> bids;
    // ordered by ask price (lowest is top priority) for ask side order book
    // max size = 3
    PriorityQueue<Ask> asks;
    String timeStamp;

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    public void setBids(PriorityQueue<Bid> bids) {
        this.bids = bids;
    }

    public void setAsks(PriorityQueue<Ask> asks) {
        this.asks = asks;
    }
}
