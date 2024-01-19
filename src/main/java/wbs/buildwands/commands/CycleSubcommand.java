package wbs.buildwands.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import wbs.buildwands.wand.BuildWand;
import wbs.buildwands.wand.WandManager;
import wbs.utils.util.commands.WbsSubcommand;
import wbs.utils.util.plugin.WbsPlugin;

public class CycleSubcommand extends WbsSubcommand {
    public CycleSubcommand(@NotNull WbsPlugin plugin) {
        super(plugin, "cycle");
    }

    @Override
    protected boolean onCommand(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args, int start) {
        if (!(sender instanceof Player player)) {
            sendMessage("This command is only usable by players.", sender);
            return true;
        }

        ItemStack item = player.getInventory().getItemInMainHand();

        BuildWand wand = WandManager.getWand(item);

        if (wand == null) {
            sendMessage("Hold a build wand!", sender);
            return true;
        }

        if (wand.cycleShape(player)) {
            wand.updateItem(item);
        }

        return true;
    }
}
