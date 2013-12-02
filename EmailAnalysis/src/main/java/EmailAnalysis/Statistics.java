package EmailAnalysis;

public class Statistics {
    public int truePositive = 0;
    public int falsePositive = 0;
    public int trueNegative = 0;
    public int falseNegative = 0;

    public float getRecall() {
        return (float) truePositive / (float) (truePositive + falseNegative);
    }

    public float getPrecision() {
        return (float) truePositive / ((float) falsePositive + (float) truePositive);
    }
}