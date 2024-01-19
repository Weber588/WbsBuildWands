package wbs.buildwands;

import org.bukkit.block.Block;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.*;

public class BlockUtils {
    public static boolean isMultiBlock(Block block) {
        BlockData data = block.getBlockData();

        if (data instanceof Bed) {
            return true;
        }

        if (data instanceof Chest chest) {
            return chest.getType() != Chest.Type.SINGLE;
        }

        if (data instanceof Bisected && !(data instanceof Stairs) && !(data instanceof TrapDoor)) {
            return true;
        }

        return false;
    }
}
