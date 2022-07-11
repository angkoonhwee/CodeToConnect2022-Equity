package parser;

import tradingEngine.Order;

public interface Parser {

    public Order parse(String message);
}
