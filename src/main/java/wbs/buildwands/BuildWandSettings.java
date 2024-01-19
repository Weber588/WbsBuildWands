package wbs.buildwands;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import wbs.buildwands.shapes.ShapeManager;
import wbs.buildwands.shapes.WandShape;
import wbs.buildwands.wand.WandType;
import wbs.buildwands.wand.WandManager;
import wbs.utils.exceptions.InvalidConfigurationException;
import wbs.utils.util.WbsEnums;
import wbs.utils.util.configuration.WbsConfigReader;
import wbs.utils.util.plugin.WbsSettings;

import java.util.*;

public class BuildWandSettings extends WbsSettings {
    protected BuildWandSettings(WbsBuildWands plugin) {
        super(plugin);
    }

    private YamlConfiguration config;

    @Override
    public void reload() {
        errors.clear();
        config = loadDefaultConfig("config.yml");

        buildWands();
    }

    private void buildWands() {
        ConfigurationSection wandSection = config.getConfigurationSection("wands");

        if (wandSection == null) {
            plugin.logger.severe("Wands section missing! Disabling plugin.");
            Bukkit.getPluginManager().disablePlugin(plugin);
            return;
        }

        int loaded = 0;
        int erroredWands = 0;
        for (String key : wandSection.getKeys(false)) {
            try {
                buildWand(Objects.requireNonNull(wandSection.getConfigurationSection(key)));
                loaded++;
            } catch (InvalidConfigurationException e) {
                erroredWands++;
            }
        }

        if (erroredWands > 0) {
            plugin.logger.warning(erroredWands + " wands failed to load.");
        }

        plugin.logger.info(loaded + " wands loaded.");

        if (loaded == 0) {
            plugin.logger.severe("No valid wands loaded.");
        }
    }

    private void buildWand(ConfigurationSection config) {
        String wandName = config.getName();
        String directory = "config.yml/wands/" + wandName;

        String shapeKey = "allowed-shapes";

        List<String> shapePairs = config.getStringList(shapeKey);
        LinkedHashMap<WandShape, Integer> wandShapes = new LinkedHashMap<>();

        for (String shapePair : shapePairs) {
            String[] shapeArgs = shapePair.split(":");
            if (shapeArgs.length != 2) {
                logError("Invalid shape pair " + shapePair +
                                ". Use Shape:Radius",
                        directory + "/" + shapeKey);
                throw new InvalidConfigurationException();
            }
            String shapeString = shapeArgs[0];
            String radiusString = shapeArgs[1];

            WandShape shape = ShapeManager.getShape(shapeString);
            if (shape == null) {
                logError("Invalid shape " + shapeString +
                                ". Valid shapes: " + String.join(", ", ShapeManager.getShapeNames()),
                        directory + "/" + shapeKey);
                throw new InvalidConfigurationException();
            }

            int radius;
            try {
                radius = Integer.parseInt(radiusString);
            } catch (NumberFormatException e) {
                logError("Invalid integer: " + radiusString,
                        directory + "/" + shapeKey);
                throw new InvalidConfigurationException();
            }
            wandShapes.put(shape, radius);
        }

        if (wandShapes.isEmpty()) {
            logError(shapeKey + " is a required field.",
                    directory + "/" + shapeKey);
            throw new InvalidConfigurationException();
        }

        WbsConfigReader.requireNotNull(config, "item", this, directory);
        String materialString = config.getString("item");
        Material material = WbsEnums.materialFromString(materialString);

        WandType wand = new WandType(wandName, wandShapes, material);

        String displayName = config.getString("display-name");
        if (displayName != null) {
            wand.setDisplayName(displayName);
        }

        boolean useDurability = config.getBoolean("use-durability", true);
        wand.setUseDurability(useDurability);

        int durabilityPerBlock = config.getInt("durability-per-block", 1);
        wand.durabilityPerBlock(durabilityPerBlock);

        List<String> lore = config.getStringList("lore");
        wand.setLore(lore);

        WandManager.registerWand(wand);
    }
}
