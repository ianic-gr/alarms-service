package gr.ianic.utils;

public class AggregationResult {
    private double sum;
    private int count;

    public void addValue(double value) {
        sum += value;
        count++;
    }

    public double getAverage() {
        return sum / count;
    }

    public double getSum() {
        return sum;
    }

    public int getCount() {
        return count;
    }
}