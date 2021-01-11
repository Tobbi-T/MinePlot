package edu.ntnu.tobiasth.mineplot.plot;

import edu.ntnu.tobiasth.mineplot.canvas.Canvas;
import org.bukkit.Material;

public class Point extends Plot {
    private final double x;
    private final double y;

    public Point(String name, Material material, double x, double y) {
        super(name, material);
        this.x = x;
        this.y = y;
    }

    @Override
    public void draw(Canvas canvas) throws IllegalArgumentException {
        canvas.drawValue(x, y, material);
    }

    @Override
    public void destroy(Canvas canvas) throws IllegalArgumentException {
        canvas.drawValue(x, y, canvas.getMaterial());
    }

    @Override
    public String toString() {
        return String.format("Point '%s' (%s) at (x: %s, y: %s)", name, material.toString().toLowerCase(), x, y);
    }
}