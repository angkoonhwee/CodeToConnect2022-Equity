package marketSimulator;


import java.util.PriorityQueue;

public class OrderBook {
    // ordered by bid price (highest is top priority) for bid side of order book
    // max size = 3
    PriorityQueue<Bid> bids;
    // ordered by ask price (lowest is top priority) for ask side order book
    // max size = 3
    PriorityQueue<Ask> asks;
}
