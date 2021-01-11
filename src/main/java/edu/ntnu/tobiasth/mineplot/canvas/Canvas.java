package edu.ntnu.tobiasth.mineplot.canvas;

import edu.ntnu.tobiasth.mineplot.Message;
import edu.ntnu.tobiasth.mineplot.plot.Plot;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.Arrays;

public class Canvas {
    private final String name;
    private final ValueRange valueRange;
    private final Location startLocation;
    private final Location endLocation;
    private final Material material;
    private final ArrayList<Plot> plots = new ArrayList<>();

    public Canvas(String name, ValueRange valueRange, Location a, Location b, Material material) throws IllegalArgumentException {
        //If the given coordinates are not a plane.
        if((a.getBlockX() - b.getBlockX() != 0) && (a.getBlockZ() - b.getBlockZ() != 0))
            throw new IllegalArgumentException(Message.INVALID_CANVAS_DIMENSIONS);

        //If the value range is not valid.
        if(valueRange.getMaxX() < valueRange.getMinX() || valueRange.getMaxY() < valueRange.getMinY())
            throw new IllegalArgumentException(Message.INVALID_VALUE_RANGE);

        this.name = name;
        this.valueRange = valueRange;
        this.material = material;

        //The location with the highest Y coordinate is the endLocation.
        boolean aIsStartLocation = a.getBlockY() < b.getBlockY();
        this.startLocation = aIsStartLocation ? a : b;
        this.endLocation = aIsStartLocation ? b : a;

        //Build the new canvas
        this.build();
    }

    /**
     * Builds the canvas.
     */
    public void build() {
        fill(material);
    }

    /**
     * Clears the canvas.
     */
    public void clear() {
        plots.clear();
        fill(material);
    }

    /**
     * Destroys the canvas.
     */
    public void destroy() {
        fill(Material.AIR);
    }

    /**
     * Replace all canvas blocks with the given material.
     * @param material Material to replace blocks with.
     */
    private void fill(Material material) {
        int[] xCoords = { startLocation.getBlockX(), endLocation.getBlockX() };
        int[] yCoords = { startLocation.getBlockY(), endLocation.getBlockY() };
        int[] zCoords = { startLocation.getBlockZ(), endLocation.getBlockZ() };
        Arrays.sort(xCoords);
        Arrays.sort(yCoords);
        Arrays.sort(zCoords);

        World world = startLocation.getWorld();

        for(int x = xCoords[0]; x <= xCoords[1]; x++) {
            for(int y = yCoords[0]; y <= yCoords[1]; y++) {
                for(int z = zCoords[0]; z <= zCoords[1]; z++) {
                    Location current = new Location(world, x, y, z);
                    current.getBlock().setType(material);
                }
            }
        }
    }

    /**
     * Draw a point on the canvas.
     * The point is given as (x, y) and placed based on the defined value range.
     * @param x The value on the x-axis.
     * @param y The value on the y-axis.
     * @param material The material to replace block with.
     * @throws IllegalArgumentException If the given point is outside the canvas.
     */
    public void drawValue(double x, double y, Material material) throws IllegalArgumentException {
        //Calculate how many percent from the left the point is.
        double relativeX = (x - valueRange.getMinX()) / (valueRange.getMaxX() - valueRange.getMinX());
        double relativeY = (y - valueRange.getMinY()) / (valueRange.getMaxY() - valueRange.getMinY());

        //If the percent is under 0 or over 1, the block is not on the canvas.
        if(relativeX > 1 || relativeY > 1 || relativeX < 0 || relativeY < 0)
            throw new IllegalArgumentException(Message.POINT_OUTSIDE_CANVAS);

        int blocksFromStartX = (int) Math.round(relativeX * getBlocksX());
        int blocksFromStartY = (int) Math.round(relativeY * getBlocksY());

        drawBlock(blocksFromStartX, blocksFromStartY, material);
    }

    /**
     * Draw a block on the canvas.
     * The block is given as (x, y) and placed relative to the lower left corner.
     * @param x Number of blocks right from the origin.
     * @param y Number of blocks up from the origin.
     * @param material The material to replace block with.
     * @throws IllegalArgumentException If the given point is outside the canvas.
     */
    public void drawBlock(int x, int y, Material material) throws IllegalArgumentException {
        //If the block is outside the canvas.
        if(y > getBlocksY() || x > getBlocksX() || y < 0 || x < 0)
            throw new IllegalArgumentException(Message.POINT_OUTSIDE_CANVAS);

        Location location = new Location(startLocation.getWorld(), startLocation.getBlockX(), startLocation.getBlockY(), startLocation.getBlockZ());

        //Invert the x value if positive on the canvas is negative in-game.
        if(isInvertedX())
            x *= -1;

        if(isParallelToX())
            location.add(x, y, 0);
        else
            location.add(0, y, x);

        location.getBlock().setType(material);
    }

    /**
     * Draws all the plots on the canvas.
     * Useful if you remove a plot and it is overlapping another.
     */
    public void drawPlots() {
        for(Plot plot : plots) {
            plot.draw(this);
        }
    }

    /**
     * Returns if the canvas is parallel to the in-game x-axis.
     * @return True if parallel to x-axis, false if parallel to z-axis.
     */
    public boolean isParallelToX() {
        return startLocation.getBlockZ() == endLocation.getBlockZ();
    }

    /**
     * Whether or not the in-game axis is inverted compared to the canvas axis.
     * @return True if the axis are inverted, false if not.
     */
    public boolean isInvertedX() {
        if(isParallelToX())
            return (endLocation.getBlockX() - startLocation.getBlockX()) < 0;
        else
            return (endLocation.getBlockZ() - startLocation.getBlockZ()) < 0;
    }

    /**
     * Adds a plot to a canvas plot list.
     * @param plot Plot to add.
     */
    public void addPlot(Plot plot) {
        plots.add(plot);
    }

    /**
     * Removes a plot from a canvas plot list.
     * @param plot Plot to remove.
     */
    public void removePlot(Plot plot) {
        plots.remove(plot);
    }

    /**
     * Get the number of blocks in the x direction on the canvas.
     * @return Number of blocks.
     */
    public int getBlocksX() {
        if(isParallelToX())
            return Math.abs(endLocation.getBlockX() - startLocation.getBlockX());
        else
            return Math.abs(endLocation.getBlockZ() - startLocation.getBlockZ());
    }

    /**
     * Get the number of blocks in the y direction on the canvas.
     * @return Number of blocks.
     */
    public int getBlocksY() {
        return Math.abs(endLocation.getBlockY() - startLocation.getBlockY());
    }

    /**
     * Get the name of the canvas.
     * @return Canvas name.
     */
    public String getName() {
        return name;
    }

    /**
     * Get the value range of the canvas.
     * @return Canvas value range.
     */
    public ValueRange getValueRange() {
        return valueRange;
    }

    /**
     * Get canvas plots.
     * @return Canvas plots.
     */
    public Plot[] getPlots() {
        return plots.toArray(new Plot[0]);
    }

    /**
     * Get canvas material.
     * @return Canvas material.
     */
    public Material getMaterial() {
        return material;
    }

    /**
     * Get string with information about the canvas.
     * @return Info string.
     */
    @Override
    public String toString() {
        return String.format("'%s' (%s) from (%s, %s, %s)=(x: %s, y: %s) to (%s, %s, %s)=(x: %s, y: %s)",
                name, material.toString().toLowerCase(),
                startLocation.getBlockX(), startLocation.getBlockY(), startLocation.getBlockZ(), valueRange.getMinX(), valueRange.getMinY(),
                endLocation.getBlockX(), endLocation.getBlockY(), endLocation.getBlockZ(), valueRange.getMaxX(), valueRange.getMaxY());
    }

}