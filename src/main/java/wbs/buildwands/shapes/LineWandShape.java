package wbs.buildwands.shapes;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import wbs.utils.util.pluginhooks.WbsRegionUtils;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class LineWandShape extends WandShape {
    private static final Vector X = new Vector(1, 0, 0);
    private static final Vector Y = new Vector(0, 1, 0);
    private static final Vector Z = new Vector(0, 0, 1);
    
    public LineWandShape() {
        super("line");
    }

    @Override
    public @NotNull LinkedHashMap<Location, BlockData> select(Player player, Block clickedBlock, BlockFace face, int radius) {
        Vector offset = face.getDirection();

        Vector lineVector;

        if (offset.getBlockY() != 0) {
            double yaw = Math.abs(player.getLocation().getYaw());
            yaw = yaw % 180;

            if (yaw > 45.0 && yaw <= 135.0) {
                lineVector = Z;
            } else {
                lineVector = X;
            }
        } else if (offset.getBlockX() != 0) {
            lineVector = Z;
        } else {
            lineVector = X;
        }

        LinkedHashMap<Location, BlockData> selected = select(player, clickedBlock, radius, offset, lineVector);

        // If none were selected, try doing a vertical line (or x->z/vice versa if on the floor/ceiling)
        if (selected.size() <= 1) {
            if (offset.getBlockY() == 0) {
                lineVector = Y;
            } else if (lineVector == Z) {
                lineVector = X;
            } else {
                lineVector = Z;
            }

            selected = select(player, clickedBlock, radius, offset, lineVector);
        }

        return selected;
    }

    private LinkedHashMap<Location, BlockData> select(Player player, Block clickedBlock, int radius, Vector offset, Vector lineVector) {
        LinkedHashMap<Location, BlockData> selected = new LinkedHashMap<>();

        Vector againstDir = offset.clone().multiply(-1);

        Location centerBlock = clickedBlock.getLocation().add(offset);
        BlockData clickedData = clickedBlock.getBlockData();
        Material placementMaterial = clickedData.getPlacementMaterial();

        Queue<Location> toCheck = new LinkedList<>();
        toCheck.add(centerBlock);

        int maxForRadius = getMaxForRadius(radius);
        while (!toCheck.isEmpty() && (radius <= 0 || selected.size() < maxForRadius)) {
            Location current = toCheck.poll();
            if (!canPlaceAt(current, player)) {
                continue;
            }

            Location againstLoc = current.clone().add(againstDir);

            BlockData againstData = againstLoc.getBlock().getBlockData();
            if (againstData.matches(clickedData) && againstData.getPlacementMaterial() == placementMaterial) {
                selected.put(current, againstData);

                toCheck.addAll(getAdjacentOnLine(current, lineVector));
                toCheck.removeAll(selected.keySet());
            }
        }

        return selected;
    }

    private List<Location> getAdjacentOnLine(Location center, Vector direction) {
        direction = direction.clone();
        return List.of(center.clone().add(direction), center.clone().add(direction.multiply(-1)));
    }

    @Override
    public int getMaxForRadius(int radius) {
        return radius * 2 + 1;
    }
}
