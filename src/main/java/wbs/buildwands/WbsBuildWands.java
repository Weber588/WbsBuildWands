package wbs.buildwands;

import wbs.buildwands.commands.CommandBuildWand;
import wbs.buildwands.listeners.InteractionListeners;
import wbs.utils.util.plugin.WbsPlugin;

public class WbsBuildWands extends WbsPlugin {

    public BuildWandSettings settings;

    private static WbsBuildWands instance;
    public static WbsBuildWands getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;

        settings = new BuildWandSettings(this);
        settings.reload();

        new CommandBuildWand(this, getCommand("buildwand"));
        registerListener(new InteractionListeners(this));
    }
}
