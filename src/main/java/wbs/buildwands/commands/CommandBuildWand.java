package wbs.buildwands.commands;

import org.bukkit.command.PluginCommand;
import wbs.utils.util.commands.WbsCommand;
import wbs.utils.util.plugin.WbsPlugin;

public class CommandBuildWand extends WbsCommand {
    public CommandBuildWand(WbsPlugin plugin, PluginCommand command) {
        super(plugin, command);

        String permission = "buildwands.command";

        addSubcommand(new ReloadCommand(plugin), permission + ".reload");
        addSubcommand(new ErrorsCommand(plugin), permission + ".reload");
        addSubcommand(new GetSubcommand(plugin), permission + ".get");
        addSubcommand(new SettingsCommand(plugin), permission + ".settings");
        addSubcommand(new CycleSubcommand(plugin), permission + ".cycle");
    }
}
