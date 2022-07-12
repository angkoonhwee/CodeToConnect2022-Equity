package main;

import main.logger.EquityLogger;
import main.marketSimulator.Market;
import main.marketSimulator.OrderBook;
import main.parser.FIXParser;
import main.parser.MarketDataParser;
import main.tradingEngine.Order;
import main.tradingEngine.ParentOrder;
import main.tradingEngine.TradeEngine;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class Equity {
    final static String rootPath = "C:\\Users\\Ang Koon Hwee\\Downloads\\BankOfAmerica_Equity\\";

    public static void main(String[] args) {
        MarketDataParser mdp = new MarketDataParser();
        EquityLogger logger = new EquityLogger();
        TradeEngine tradeEngine;
        Market market = new Market();

        File f = new File(rootPath + "market_data\\market_data.csv");

        try {
            logger.setupLogger("Equity", rootPath + "outputLog.txt");
            FileReader fr = new FileReader(f);
            BufferedReader br = new BufferedReader(fr);
            String csv = br.readLine();

            OrderBook orderBook = mdp.parse(csv);
            FIXParser fixParser = new FIXParser();
            ParentOrder order = fixParser.parse("54=1; 40=1; 38=10000; 6404=10");
            market.setOrderBook(orderBook);

            tradeEngine = new TradeEngine(market, order, logger);
            tradeEngine.sliceOrder();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
