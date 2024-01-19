package wbs.buildwands.shapes;

import org.bukkit.Location;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import wbs.utils.util.pluginhooks.WbsRegionUtils;
import wbs.utils.util.string.WbsStrings;

import java.util.LinkedHashMap;
import java.util.Objects;

public abstract class WandShape {
    private final String shapeName;

    protected WandShape(String shapeName) {
        this.shapeName = shapeName;
    }

    public String getName() {
        return shapeName;
    }

    public String getDisplayName() {
        return WbsStrings.capitalizeAll(shapeName.replaceAll("_", " "));
    }

    /**
     * @param player The player to select for
     * @param clickedBlock The block clicked to use the build wand
     * @param face The block face the player clicked
     * @param radius The maximum number of blocks to select, or <= 0 for no limit (within limit of given shape).
     * @return An ordered map of a location to place a block, to the block data of the block it's placed against
     */
    @NotNull
    public abstract LinkedHashMap<Location, BlockData> select(Player player, Block clickedBlock, BlockFace face, int radius);

    @Contract("null, _ -> false")
    protected boolean canPlaceAt(Location current, @NotNull Player player) {
        if (current == null) {
            return false;
        }

        if (!Tag.REPLACEABLE.isTagged(current.getBlock().getType())) {
            return false;
        }

        if (!WbsRegionUtils.canBuildAt(current, player)) {
            return false;
        }

        return true;
    }
    
    public abstract int getMaxForRadius(int radius);

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof WandShape wandShape)) return false;
        return shapeName.equals(wandShape.shapeName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(shapeName);
    }
}
