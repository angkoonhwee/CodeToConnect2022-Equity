package main;

import main.parser.FIXParser;
import main.parser.MarketDataParser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class Equity {
    public static void main(String[] args) {
        MarketDataParser mdp = new MarketDataParser();

        File f = new File("C:\\Users\\Ang Koon Hwee\\Downloads\\BankOfAmerica_Equity\\market_data\\market_data.csv");

        try {
            FileReader fr = new FileReader(f);
            BufferedReader br = new BufferedReader(fr);
            String csv = br.readLine();

            mdp.parse(csv);
            FIXParser fixParser = new FIXParser();
            fixParser.parse("54=1; 40=1; 38=10000; 6404=10");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
