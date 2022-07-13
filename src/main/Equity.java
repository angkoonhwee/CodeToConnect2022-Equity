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
    final static String rootPath = "C:\\Users\\Ang Koon Hwee\\Downloads\\BankOfAmerica_Equity\\";

    public static void main(String[] args) {
        MarketDataParser mdp = new MarketDataParser();
        FIXParser fixParser = new FIXParser();
        EquityLogger logger = new EquityLogger();

        TradeEngine tradeEngine;
        MarketSimulator marketSimulator;
        Market market = new Market();

        File f = new File(rootPath + "market_data\\market_data.csv");

        try {
            logger.setupLogger("Equity", rootPath + "outputLog.txt");
            FileReader fr = new FileReader(f);
            BufferedReader br = new BufferedReader(fr);
            ParentOrder order = fixParser.parse("54=1; 40=1; 38=10000; 6404=10");

            tradeEngine = new TradeEngine(market, order, logger);
            marketSimulator = new MarketSimulator(market, order, logger);

            String csv;
            while ((csv = br.readLine()) != null) {
                OrderBook orderBook = mdp.parse(csv);
                market.setOrderBook(orderBook);
                market.updateMarketVol(100000);
//                order.updateCumulativeQuatity(13000);
//
//                HashMap<Double, ChildOrder> test = new HashMap<>();
//                ChildOrder childOrder1 = new ChildOrder(1000, 53, Order.actionType.NEW);
//                ChildOrder childOrder2 = new ChildOrder(1000, 53.1, Order.actionType.NEW);
//                ChildOrder childOrder3 = new ChildOrder(1000, 53.2, Order.actionType.NEW);
//                ChildOrder childOrder4 = new ChildOrder(1000, 53.3, Order.actionType.NEW);
//                test.put(childOrder1.price, childOrder1);
//                test.put(childOrder2.price, childOrder2);
//                test.put(childOrder3.price, childOrder3);
//                test.put(childOrder4.price, childOrder4);
//                tradeEngine.updateQueuedOrders(test);

                HashMap<Double, ChildOrder> recentOrder = tradeEngine.sliceOrder();
                tradeEngine.updateQueuedOrders(marketSimulator.fillOrders(recentOrder));
                break;
            }


        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
