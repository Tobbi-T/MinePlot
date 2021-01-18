package edu.ntnu.tobiasth.mineplot;

import org.bukkit.Location;

import java.util.Objects;

public class LocationSelection {
    private Location left;
    private Location right;

    public Location getLeft() {
        return new Location(left.getWorld(), left.getBlockX(), left.getBlockY(), left.getBlockZ());
    }

    public void setLeft(Location left) {
        this.left = left;
    }

    public Location getRight() {
        return Objects.isNull(right) ? null : new Location(right.getWorld(), right.getBlockX(), right.getBlockY(), right.getBlockZ());
    }

    public void setRight(Location right) {
        this.right = right;
    }
}
