package wbs.buildwands.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import wbs.buildwands.shapes.WandShape;
import wbs.buildwands.wand.BuildWand;
import wbs.buildwands.wand.WandManager;
import wbs.utils.util.commands.WbsSubcommand;
import wbs.utils.util.plugin.WbsPlugin;

public class RadiusSubcommand extends WbsSubcommand {
    public RadiusSubcommand(@NotNull WbsPlugin plugin) {
        super(plugin, "radius");
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

        if (args.length <= start) {
            sendUsage("<number>", sender, label, args, start);
            return true;
        }

        String radiusString = args[start];

        WandShape shape = wand.getShape();
        int radius;
        try {
            radius = Integer.parseInt(radiusString);
        } catch (NumberFormatException e) {
            sendMessage("Invalid radius \"" + radiusString + "\". Use an integer.", sender);
            return true;
        }

        if (radius <= 0 && !checkPermission(sender, getPermission() + ".unlimited")) {
            sendMessage("Use a positive number.", sender);
            return true;
        }

        if (radius > wand.getType().getRadius(shape) && !checkPermission(sender, getPermission() + ".bypass")) {
            sendMessage("This wand can't exceed " + wand.getType().getRadius(shape) + " blocks.", sender);
            return true;
        }

        wand.setRadius(radius);
        wand.updateItem(item);

        sendMessage("Radius changed!", sender);

        return true;
    }
}
