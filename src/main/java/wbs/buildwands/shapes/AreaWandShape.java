package wbs.buildwands.shapes;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.stream.Collectors;

public abstract class AreaWandShape extends WandShape {
    private static final Vector X = new Vector(1, 0, 0);
    private static final Vector Y = new Vector(0, 1, 0);
    private static final Vector Z = new Vector(0, 0, 1);

    protected AreaWandShape(String shapeName) {
        super(shapeName);
    }

    @Override
    public @NotNull LinkedHashMap<Location, BlockData> select(Player player, Block clickedBlock, BlockFace face, int radius) {
        Vector normal = face.getDirection();

        Vector axis1;
        Vector axis2;

        if (normal.getY() != 0) {
            axis1 = X;
            axis2 = Z;
        } else if (normal.getX() != 0) {
            axis1 = Y;
            axis2 = Z;
        } else {
            axis1 = Y;
            axis2 = X;
        }

        return select(player, clickedBlock, radius, normal, axis1, axis2);
    }

    private LinkedHashMap<Location, BlockData> select(Player player, Block clickedBlock, int radius, Vector offset, Vector axis1, Vector axis2) {
        LinkedHashMap<Location, BlockData> selected = new LinkedHashMap<>();

        Vector againstDir = offset.clone().multiply(-1);

        Location centerLoc = clickedBlock.getLocation().add(offset);
        BlockData clickedData = clickedBlock.getBlockData();
        Material placementMaterial = clickedData.getPlacementMaterial();

        Queue<Location> toCheck = new LinkedList<>();
        toCheck.add(centerLoc);

        int maxForRadius = getMaxForRadius(radius);
        while (!toCheck.isEmpty() && selected.size() <= maxForRadius) {
            Location current = toCheck.poll();
            if (!canPlaceAt(current, player)) {
                continue;
            }

            Location againstLoc = current.clone().add(againstDir);

            BlockData againstData = againstLoc.getBlock().getBlockData();
            if (againstData.matches(clickedData) && againstData.getPlacementMaterial() == placementMaterial) {
                selected.put(current, againstData);

                List<Location> adjacent = getAdjacentInPlane(current, axis1, axis2);
                adjacent = adjacent.stream()
                        .filter(location ->
                                isValid(centerLoc, location, radius)
                        ).filter(location ->
                                !selected.containsKey(location)
                        ).collect(Collectors.toList());

                toCheck.addAll(adjacent);
            }
        }

        return selected;
    }

    protected List<Location> getAdjacentInPlane(Location current,
                                                Vector axis1,
                                                Vector axis2)
    {
        List<Location> adjacent = new LinkedList<>();

        adjacent.add(current.clone().add(axis1));
        adjacent.add(current.clone().add(axis2));

        adjacent.add(current.clone().subtract(axis1));
        adjacent.add(current.clone().subtract(axis2));

        if (allowDiagonals()) {
            adjacent.add(current.clone().add(axis1).add(axis2));
            adjacent.add(current.clone().subtract(axis1).subtract(axis2));
            adjacent.add(current.clone().add(axis1).subtract(axis2));
            adjacent.add(current.clone().subtract(axis1).add(axis2));
        }

        return adjacent;
    }

    protected abstract boolean isValid(Location central, Location location, int radius);

    protected abstract boolean allowDiagonals();
}
