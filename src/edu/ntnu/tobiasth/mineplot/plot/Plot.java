package edu.ntnu.tobiasth.mineplot.plot;

import edu.ntnu.tobiasth.mineplot.canvas.Canvas;
import org.bukkit.Material;

public abstract class Plot {
    protected final String name;
    protected final Material material;

    protected Plot(String name, Material material) {
        this.name = name;
        this.material = material;
    }

    public abstract void draw(Canvas canvas) throws IllegalArgumentException;

    public abstract void destroy(Canvas canvas);

    public abstract String toString();

    public String getName() {
        return name;
    }
}