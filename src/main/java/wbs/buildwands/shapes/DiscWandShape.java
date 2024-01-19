package wbs.buildwands.shapes;

import org.bukkit.Location;

public class DiscWandShape extends AreaWandShape {
    protected DiscWandShape() {
        super("disc");
    }

    @Override
    protected boolean isValid(Location central, Location location, int radius) {
        double distanceSq = central.distanceSquared(location);

        return distanceSq <= radius * radius;
    }

    @Override
    protected boolean allowDiagonals() {
        return true;
    }

    @Override
    public int getMaxForRadius(int radius) {
        int diameter = radius * 2 + 1;
        // TODO: Change this to be a circular area, making sure it works for cuboid
        return diameter * diameter;
    }
}
