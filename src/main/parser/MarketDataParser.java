package main.parser;

import main.marketSimulator.Ask;
import main.marketSimulator.Bid;
import main.marketSimulator.Market;
import main.marketSimulator.OrderBook;

import java.util.StringTokenizer;
import java.util.TreeSet;

public class MarketDataParser {
    private final String QUOTE = "Q";
    private final String TRADE = "T";

    public void parseAndUpdateMarket(String quote, Market market) throws IllegalArgumentException {
        StringTokenizer splitQuote = new StringTokenizer(quote, ",");

        switch (splitQuote.nextToken()) {
            case QUOTE:
                OrderBook ob = new OrderBook();
                ob.setTimeStamp(splitQuote.nextToken());
                ob.setBids(parseBids(splitQuote.nextToken()));
                ob.setAsks(parseAsks(splitQuote.nextToken()));
                market.setOrderBook(ob);
                break;
            case TRADE:
                splitQuote.nextToken();
                splitQuote.nextToken();
                market.updateMarketVol(Integer.parseInt(splitQuote.nextToken()));
                break;
        }
       return;
    }

    private TreeSet<Bid> parseBids(String bids) {
        StringTokenizer splitBids = new StringTokenizer(bids, " ");

        if (splitBids.countTokens() % 2 != 0) {
            throw new IllegalArgumentException(
                    "Please check bids. Number of bid prices does not match number of bid sizes.");
        }

        TreeSet<Bid> treeSet = new TreeSet<>();

        while (splitBids.countTokens() > 0) {
            double bidPrice = Double.parseDouble(splitBids.nextToken());
            int bidSize = Integer.parseInt(splitBids.nextToken());
            Bid bid = new Bid(bidSize, bidPrice);

            treeSet.add(bid);
        }

        return treeSet;
    }

    private TreeSet<Ask> parseAsks(String asks) {
        StringTokenizer splitAsks = new StringTokenizer(asks, " ");

        if (splitAsks.countTokens() % 2 != 0) {
            throw new IllegalArgumentException(
                    "Please check asks. Number of ask prices does not match number of ask sizes.");
        }

        TreeSet<Ask> treeSet = new TreeSet<>();

        while (splitAsks.countTokens() > 0) {
            double askPrice = Double.parseDouble(splitAsks.nextToken());
            int askSize = Integer.parseInt(splitAsks.nextToken());
            Ask ask = new Ask(askSize, askPrice);

            treeSet.add(ask);
        }

        return treeSet;
    }
}
