package edu.ntnu.tobiasth.mineplot.canvas;

public class ValueRange {
    private final double minX;
    private final double maxX;
    private final double minY;
    private final double maxY;

    public ValueRange(double minX, double maxX, double minY, double maxY) {
        this.minX = minX;
        this.maxX = maxX;
        this.minY = minY;
        this.maxY = maxY;
    }

    public double getMinX() {
        return minX;
    }

    public double getMaxX() {
        return maxX;
    }

    public double getMinY() {
        return minY;
    }

    public double getMaxY() {
        return maxY;
    }
}
