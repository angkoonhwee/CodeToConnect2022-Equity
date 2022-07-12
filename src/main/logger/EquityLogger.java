package main.logger;

import main.tradingEngine.ChildOrder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class EquityLogger {
    Logger logger;
    FileHandler fh;

    private class Formatter extends SimpleFormatter {
        @Override
        public String format(LogRecord record) {
            StringBuilder sb = new StringBuilder();
            sb.append(record.getMessage()).append('\n');
            return sb.toString();
        }
    }

    public void setupLogger(String name, String outputPath) {
        this.logger = Logger.getLogger(name);

        try {
            fh = new FileHandler(outputPath);
            logger.addHandler(fh);
            logger.setUseParentHandlers(false);
            Formatter formatter = new Formatter();
            fh.setFormatter(formatter);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void logOrders(ArrayList<ChildOrder> order) {
        System.out.println("ORDERS TO LOG: " + order);
        logger.log(Level.INFO, "Strategy out:" + order.toString());
    }

    public void logFills() {

    }
}
