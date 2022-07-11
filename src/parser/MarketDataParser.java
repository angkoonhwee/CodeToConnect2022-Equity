package parser;

import marketSimulator.Ask;
import marketSimulator.Bid;
import marketSimulator.OrderBook;

import java.util.PriorityQueue;
import java.util.StringTokenizer;

public class MarketDataParser {
    private final String QUOTE = "Q";
    private final String TRADE = "T";

    public OrderBook parse(String quote) throws IllegalArgumentException {
        System.out.println(quote);
        StringTokenizer splitQuote = new StringTokenizer(quote, ",");

        switch (splitQuote.nextToken()) {
            case QUOTE:
                OrderBook ob = new OrderBook();
                ob.setTimeStamp(splitQuote.nextToken());
                ob.setBids(parseBids(splitQuote.nextToken()));
                ob.setAsks(parseAsks(splitQuote.nextToken()));
            case TRADE:

        }
       return null;
    }

    private PriorityQueue<Bid> parseBids(String bids) {
        StringTokenizer splitBids = new StringTokenizer(bids, " ");

        if (splitBids.countTokens() % 2 != 0) {
            throw new IllegalArgumentException(
                    "Please check bids. Number of bid prices does not match number of bid sizes.");
        }

        PriorityQueue<Bid> pq = new PriorityQueue<>();

        while (splitBids.countTokens() > 0) {
            double bidPrice = Double.parseDouble(splitBids.nextToken());
            int bidSize = Integer.parseInt(splitBids.nextToken());
            Bid bid = new Bid(bidSize, bidPrice);

            pq.add(bid);
        }

        System.out.println(pq);
        return pq;
    }

    private PriorityQueue<Ask> parseAsks(String asks) {
        StringTokenizer splitAsks = new StringTokenizer(asks, " ");

        if (splitAsks.countTokens() % 2 != 0) {
            throw new IllegalArgumentException(
                    "Please check asks. Number of ask prices does not match number of ask sizes.");
        }

        PriorityQueue<Ask> pq = new PriorityQueue<>();

        while (splitAsks.countTokens() > 0) {
            double askPrice = Double.parseDouble(splitAsks.nextToken());
            int askSize = Integer.parseInt(splitAsks.nextToken());
            Ask ask = new Ask(askSize, askPrice);

            pq.add(ask);
        }

        System.out.println(pq);
        return pq;
    }
}
