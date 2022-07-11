package parser;

import tradingEngine.Order;

public class FIXParser implements Parser {
    private final static String SIDE_TAG = "54";
    private final static String SIDE_BUY = "1";
    private final static String SIDE_SELL = "2";
    private final static String ORDER_TYPE_TAG = "40=1";
    private final static String ORDER_QUANTITY_TAG = "38";
    private final static String TARGET_PERCENTAGE_TAG = "6064";

    public Order parse(String fixMessage) {

        Order.orderType type = null;
        int quantity = 0;
        double targetPercentage = 0.0;

        String[] tags = fixMessage.split(";");

        for (String tag : tags) {
            String[] processedTag = tag.split("=");

            switch (processedTag[0]) {
                case SIDE_TAG:
                    type = processedTag[1] == SIDE_BUY ? Order.orderType.BUY : Order.orderType.SELL;
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

        return new Order(type, quantity, targetPercentage);
    }
}
