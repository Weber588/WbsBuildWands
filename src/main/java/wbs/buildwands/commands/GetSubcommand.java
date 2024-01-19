package wbs.buildwands.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import wbs.buildwands.wand.WandType;
import wbs.buildwands.wand.WandManager;
import wbs.utils.util.commands.WbsSubcommand;
import wbs.utils.util.plugin.WbsPlugin;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class GetSubcommand extends WbsSubcommand {
    public GetSubcommand(@NotNull WbsPlugin plugin) {
        super(plugin, "get");
    }

    @Override
    protected boolean onCommand(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args, int start) {
        if (!(sender instanceof Player player)) {
            sendMessage("This command is only usable by players.", sender);
            return true;
        }

        if (args.length <= start) {
            sendUsage("<wand name>", sender, label, args, start);
            return true;
        }

        String wandName = args[start];

        WandType wand = WandManager.getWand(wandName);
        if (wand == null) {
            sendMessage("&wInvalid wand: \"&x" + wandName + "&w\"", sender);
            sendMessage("&wPlease choose from the following: &x" +
                    String.join(", ", WandManager.getWandNames()),
                    sender
            );
            return true;
        }

        if (!checkPermission(player, wand.getPermission())) {
            return true;
        }

        HashMap<Integer, ItemStack> failedToAdd = player.getInventory().addItem(wand.buildItem());
        ItemStack item = failedToAdd.get(0);

        if (item != null) {
            player.getWorld().dropItemNaturally(player.getLocation(), item);
        }

        sendMessage("Received wand \"" + wandName + "\"", sender);

        return true;
    }

    @Override
    protected List<String> getTabCompletions(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args, int start) {
        List<String> completions = new LinkedList<>();

        if (args.length == start) {
            return WandManager.getWandNames();
        }

        return completions;
    }
}
