package marketSimulator;

public class Ask implements Comparable<Ask> {
    public int askSize;
    public double askPrice;

    public Ask(int askSize, double askPrice) {
        this.askSize = askSize;
        this.askPrice = askPrice;
    }

    @Override
    public int compareTo(Ask anotherAsk) {
        return Double.compare(anotherAsk.askPrice, this.askPrice);
    }
}
