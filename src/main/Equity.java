package main;

import main.logger.EquityLogger;
import main.marketSimulator.Market;
import main.marketSimulator.MarketSimulator;
import main.marketSimulator.OrderBook;
import main.parser.FIXParser;
import main.parser.MarketDataParser;
import main.tradingEngine.ChildOrder;
import main.tradingEngine.Order;
import main.tradingEngine.ParentOrder;
import main.tradingEngine.TradeEngine;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

public class Equity {

    public static void main(String[] args) {
        MarketDataParser mdp = new MarketDataParser();
        FIXParser fixParser = new FIXParser();
        EquityLogger logger = new EquityLogger();

        TradeEngine tradeEngine;
        MarketSimulator marketSimulator;
        Market market = new Market();

        File f = new File("market_data\\market_data.csv");

        try {
            logger.setupLogger("Equity", "outputLog.txt");
            FileReader fr = new FileReader(f);
            BufferedReader br = new BufferedReader(fr);
            ParentOrder order = fixParser.parse("54=1; 40=1; 38=10000; 6404=10");

            tradeEngine = new TradeEngine(market, order);
            marketSimulator = new MarketSimulator(market, order, logger);

            String csv;
            while ((csv = br.readLine()) != null) {
                OrderBook orderBook = mdp.parse(csv);
                market.setOrderBook(orderBook);
                market.updateMarketVol(100000);
//                order.updateCumulativeQuatity(13000);

                HashMap<Order.OrderKey, ChildOrder> test = new HashMap<>();
                ChildOrder childOrder1 = new ChildOrder(1000, 53, Order.actionType.NEW);
                ChildOrder childOrder2 = new ChildOrder(1000, 53.1, Order.actionType.NEW);
                ChildOrder childOrder3 = new ChildOrder(1000, 53.2, Order.actionType.NEW);
                ChildOrder childOrder4 = new ChildOrder(1000, 53.3, Order.actionType.NEW);
                test.put(childOrder1.key, childOrder1);
                test.put(childOrder2.key, childOrder2);
                test.put(childOrder3.key, childOrder3);
                test.put(childOrder4.key, childOrder4);
                tradeEngine.updateQueuedOrders(test);

                // Trade Engine places all orders, including partial orders e.g. already queued [N:10:100]
                // Top up order to 200@10 by placing another order [N:10:100]
                HashMap<Order.OrderKey, ChildOrder> recentOrder = tradeEngine.sliceOrder();
                logger.logOrders(recentOrder);

                // Market Simulator will merge the orders so that when order is filled,
                // "Filled: 200@10" will be shown.
                // Merge order also includes removing cancelled orders.
                HashMap<Order.OrderKey, ChildOrder> updateOrders = marketSimulator.fillOrders(recentOrder);
                tradeEngine.updateQueuedOrders(updateOrders);
                break;
            }


        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
