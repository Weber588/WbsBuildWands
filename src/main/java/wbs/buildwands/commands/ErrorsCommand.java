package wbs.buildwands.commands;

import wbs.buildwands.WbsBuildWands;
import wbs.utils.util.commands.WbsErrorsSubcommand;
import wbs.utils.util.plugin.WbsPlugin;
import wbs.utils.util.plugin.WbsSettings;

public class ErrorsCommand extends WbsErrorsSubcommand {
    public ErrorsCommand(WbsPlugin plugin) {
        super(plugin);
    }

    @Override
    protected WbsSettings getSettings() {
        return WbsBuildWands.getInstance().settings;
    }
}
