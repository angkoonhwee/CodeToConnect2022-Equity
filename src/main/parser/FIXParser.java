package main.parser;

import main.tradingEngine.Order;
import main.tradingEngine.ParentOrder;

public class FIXParser {
    private final static String SIDE_TAG = "54";
    private final static String SIDE_BUY = "1";
    private final static String SIDE_SELL = "2";
    private final static String ORDER_TYPE_TAG = "40=1";
    private final static String ORDER_QUANTITY_TAG = "38";
    private final static String TARGET_PERCENTAGE_TAG = "6404";

    public Order parse(String fixMessage) {

        ParentOrder.orderType type = null;
        int quantity = 0;
        double targetPercentage = 0.0;

        String[] tags = fixMessage.replaceAll("\\s", "").split(";");

        for (String tag : tags) {
            String[] processedTag = tag.split("=");

            switch (processedTag[0]) {
                case SIDE_TAG:
                    type = processedTag[1] == SIDE_BUY ?
                            ParentOrder.orderType.BUY
                            : ParentOrder.orderType.SELL;
                    break;
                case ORDER_QUANTITY_TAG:
                    quantity = Integer.parseInt(processedTag[1]);
                    break;
                case TARGET_PERCENTAGE_TAG:
                    targetPercentage = Double.parseDouble(processedTag[1]);
                    break;
                default:
                    break;
            }
        }

        ParentOrder order = new ParentOrder(type, quantity, targetPercentage);
        System.out.println(order);
        return order;
    }
}
