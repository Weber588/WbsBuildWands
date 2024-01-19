package wbs.buildwands.listeners;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;
import wbs.buildwands.BlockUtils;
import wbs.buildwands.WbsBuildWands;
import wbs.buildwands.utils.HistoryEntry;
import wbs.buildwands.wand.BuildWand;
import wbs.buildwands.wand.WandManager;
import wbs.utils.util.WbsEnums;
import wbs.utils.util.WbsItems;
import wbs.utils.util.plugin.WbsMessenger;
import wbs.utils.util.plugin.WbsPlugin;

import java.util.*;

public class InteractionListeners extends WbsMessenger implements Listener {

    public InteractionListeners(@NotNull WbsPlugin plugin) {
        super(plugin);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }

        Block block = event.getClickedBlock();
        if (block == null) {
            return;
        }

        if (BlockUtils.isMultiBlock(block)) {
            return;
        }

        Player player = event.getPlayer();

        ItemStack item = player.getInventory().getItemInMainHand();

        BuildWand wand = WandManager.getWand(item);
        if (wand == null) {
            return;
        }

        boolean shouldCancel = false;
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            shouldCancel = wand.tryPlacing(player, item, block, event.getBlockFace());
            if (!block.getType().isInteractable()) {
                shouldCancel = true;
            }
        } else if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
            wand.preview(player, block, event.getBlockFace());
            shouldCancel = true;
        }

        if (shouldCancel) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent event) {
        ItemStack item = event.getItemDrop().getItemStack();

        BuildWand wand = WandManager.getWand(item);
        if (wand == null) {
            return;
        }

        event.setCancelled(true);

        Player player = event.getPlayer();
        boolean changed = false ;
        if (player.isSneaking() && player.hasPermission("buildwands.undo")) {
            changed = wand.undo(player);
        } else {
            if (wand.getType().getAllowedShapes().size() > 1) {
                changed = wand.cycleShape(player);
            }
        }

        if (changed) {
            wand.updateItem(item);
        }
    }
}
