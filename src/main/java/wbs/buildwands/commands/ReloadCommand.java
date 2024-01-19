package wbs.buildwands.commands;

import wbs.buildwands.WbsBuildWands;
import wbs.utils.util.commands.WbsReloadSubcommand;
import wbs.utils.util.plugin.WbsPlugin;
import wbs.utils.util.plugin.WbsSettings;

public class ReloadCommand extends WbsReloadSubcommand {
    public ReloadCommand(WbsPlugin plugin) {
        super(plugin);
    }

    @Override
    protected WbsSettings getSettings() {
        return WbsBuildWands.getInstance().settings;
    }
}
