package marketSimulator;

public class Bid implements Comparable<Bid> {
    public int bidSize;
    public double bidPrice;

    public Bid(int bidSize, double bidPrice) {
        this.bidSize = bidSize;
        this.bidPrice = bidPrice;
    }

    public void decreaseBidSizeBy(int decreaseBy) {
        this.bidSize = this.bidSize - decreaseBy;
    }

    @Override
    public int compareTo(Bid anotherBid) throws IllegalArgumentException {
        return Double.compare(this.bidPrice, anotherBid.bidPrice);
    }

    @Override
    public String toString() {
        return "[Bid Size: " + bidSize + "; Bid Price: " + bidPrice + "]";
    }
}
