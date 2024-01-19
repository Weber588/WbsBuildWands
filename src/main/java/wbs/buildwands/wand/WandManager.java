package wbs.buildwands.wand;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public final class WandManager {
    private static final Map<String, WandType> PRESETS = new HashMap<>();
    public static List<String> getWandNames() {
        List<String> names = new ArrayList<>(PRESETS.keySet());
        names.sort(Comparator.naturalOrder());
        return names;
    }

    @Nullable
    public static WandType getWand(String wandName) {
        return PRESETS.get(stripSyntax(wandName));
    }

    @Nullable
    public static BuildWand getWand(ItemStack item) {
        return PRESETS.values().stream()
                .filter(preset -> preset.isWandItem(item))
                .map(wandPreset -> wandPreset.fromItem(item))
                .findAny()
                .orElse(null);
    }

    public static void registerWand(WandType wand) {
        PRESETS.put(stripSyntax(wand.getWandName()), wand);
    }

    public static String stripSyntax(String toStrip) {
        return toStrip.toLowerCase().replaceAll("$[^a-z0-9]+", "_");
    }
}
