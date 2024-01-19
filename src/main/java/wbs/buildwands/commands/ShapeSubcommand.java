package wbs.buildwands.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import wbs.buildwands.shapes.ShapeManager;
import wbs.buildwands.shapes.WandShape;
import wbs.buildwands.wand.BuildWand;
import wbs.buildwands.wand.WandManager;
import wbs.utils.util.commands.WbsSubcommand;
import wbs.utils.util.plugin.WbsPlugin;

import java.util.stream.Collectors;

public class ShapeSubcommand extends WbsSubcommand {
    public ShapeSubcommand(@NotNull WbsPlugin plugin) {
        super(plugin, "shape");
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
            sendUsage("<shape>", sender, label, args, start);
            return true;
        }

        String shapeString = args[start];
        WandShape shape = ShapeManager.getShape(shapeString);

        if (shape == null) {
            sendMessage("Invalid shape: \"" + shapeString + "\". Please choose from the following: " +
                    wand.getType().getAllowedShapes().keySet().stream()
                            .map(WandShape::getName)
                            .collect(Collectors.joining(", ")),
                    sender
            );
            return true;
        }

        wand.setShape(shape);
        wand.updateItem(item);

        sendMessage("Shape changed!", sender);

        return true;
    }
}
