package wbs.buildwands.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import wbs.buildwands.wand.BuildWand;
import wbs.buildwands.wand.WandManager;
import wbs.utils.util.commands.WbsCommandNode;
import wbs.utils.util.plugin.WbsMessageBuilder;
import wbs.utils.util.plugin.WbsPlugin;

public class SettingsCommand extends WbsCommandNode {
    private final static String LINE_BREAK = "&m                           ";

    public SettingsCommand(@NotNull WbsPlugin plugin) {
        super(plugin, "settings");
        addAlias("info");
        addAlias("set");

        addChild(new ShapeSubcommand(plugin), getPermission() + ".shape");
        addChild(new RadiusSubcommand(plugin), getPermission() + ".radius");
    }

    @Override
    protected boolean onCommandNoArgs(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args, int start) {
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

        sendSettingsMessage(player, wand);

        return true;
    }

    protected void sendSettingsMessage(Player player, BuildWand wand) {
        WbsMessageBuilder builder = plugin.buildMessage(LINE_BREAK);

        String baseCommand = "/buildwand " + getLabel();

        builder.append("\nShape: &h" + wand.getShape().getDisplayName() + " &6[Change]")
                    .addClickCommandSuggestion(baseCommand + " shape ")
                    .addHoverText("&h"  + baseCommand + " shape ")
                .append(" &b[Cycle]")
                    .addClickCommand("/buildwand cycle")
                    .addHoverText("&h/buildwand cycle")
                .append("\nRadius: &h" + wand.getRadius())
                    .addClickCommandSuggestion(baseCommand + " radius ")
                    .addHoverText("&h"  + baseCommand + " max-radius ");

        builder.append("\n" + LINE_BREAK);

        builder.send(player);
    }
}
