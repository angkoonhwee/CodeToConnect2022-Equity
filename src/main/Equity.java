package main;

import main.logger.EquityLogger;
import main.marketSimulator.Market;
import main.marketSimulator.MarketSimulator;
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

        File fMarket = new File(args[0]);
        File fFIX = new File(args[1]);

        try {
            logger.setupLogger("Equity", "outputLog.txt");

            FileReader frMarket = new FileReader(fMarket);
            BufferedReader brMarket = new BufferedReader(frMarket);
            FileReader frFIX = new FileReader(fFIX);

            BufferedReader brFIX = new BufferedReader(frFIX);
            ParentOrder order = fixParser.parse(brFIX.readLine());
            System.out.println(order);

            tradeEngine = new TradeEngine(market, order);
            marketSimulator = new MarketSimulator(market, order, logger);

            int i = 0;
            String csv;
            while ((csv = brMarket.readLine()) != null) {
                System.out.println(csv);
                mdp.parseAndUpdateMarket(csv, market);

                // Trade Engine places all orders, including partial orders e.g. already queued [N:10:100]
                // Top up order to 200@10 by placing another order [N:10:100]
                HashMap<Order.OrderKey, ChildOrder> recentOrder = tradeEngine.sliceOrder();
                logger.logOrders(recentOrder);

                // Market Simulator will merge the orders so that when order is filled,
                // "Filled: 200@10" will be shown.
                // Merge order also includes removing cancelled orders.
                HashMap<Order.OrderKey, ChildOrder> updateOrders
                        = marketSimulator.fillOrders(recentOrder, market.getOrderBook().getTimeStamp());
                tradeEngine.updateQueuedOrders(updateOrders);
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
