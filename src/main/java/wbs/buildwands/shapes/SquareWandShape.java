package wbs.buildwands.shapes;

import org.bukkit.Location;
import wbs.buildwands.WbsBuildWands;

public class SquareWandShape extends AreaWandShape {

    protected SquareWandShape() {
        super("square");
    }

    @Override
    protected boolean isValid(Location central, Location location, int radius) {
        int xDiff = Math.abs(location.getBlockX() - central.getBlockX());
        int yDiff = Math.abs(location.getBlockY() - central.getBlockY());
        int zDiff = Math.abs(location.getBlockZ() - central.getBlockZ());

        return xDiff <= radius && yDiff <= radius && zDiff <= radius;
    }

    @Override
    protected boolean allowDiagonals() {
        return false;
    }

    @Override
    public int getMaxForRadius(int radius) {
        int diameter = radius * 2 + 1;
        // TODO: Change this to be a circular area, making sure it works for cuboid
        return diameter * diameter;
    }
}
