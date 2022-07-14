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

        File f = new File("market_data\\market_data.csv");

        try {
            logger.setupLogger("Equity", "outputLog.txt");
            FileReader fr = new FileReader(f);
            BufferedReader br = new BufferedReader(fr);
            ParentOrder order = fixParser.parse("54=1; 40=1; 38=100000; 6404=10");
            System.out.println(order);


            tradeEngine = new TradeEngine(market, order);
            marketSimulator = new MarketSimulator(market, order, logger);

            int i = 0;
            String csv;
            while ((csv = br.readLine()) != null) {
                System.out.println(csv);
                mdp.parseAndUpdateMarket(csv, market);
                logger.log(csv);
                logger.log("Queued orders: " + marketSimulator.getQueuedOrders().values());
                System.out.println("Before order filled: " + market.getCurrMarketVol());

                // Trade Engine places all orders, including partial orders e.g. already queued [N:10:100]
                // Top up order to 200@10 by placing another order [N:10:100]
                HashMap<Order.OrderKey, ChildOrder> recentOrder = tradeEngine.sliceOrder();
                logger.logOrders(recentOrder);

                // Market Simulator will merge the orders so that when order is filled,
                // "Filled: 200@10" will be shown.
                // Merge order also includes removing cancelled orders.
                HashMap<Order.OrderKey, ChildOrder> updateOrders = marketSimulator.fillOrders(recentOrder);
                tradeEngine.updateQueuedOrders(updateOrders);
                System.out.println("After order filled: " + market.getCurrMarketVol());
                System.out.println();
                i++;
                if (i > 3500) {
                    break;
                }
            }


        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
