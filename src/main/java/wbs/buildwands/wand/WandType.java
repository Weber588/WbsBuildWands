package wbs.buildwands.wand;

import com.google.common.collect.Iterables;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import wbs.buildwands.WbsBuildWands;
import wbs.buildwands.shapes.WandShape;
import wbs.utils.util.ShinyEnchantment;
import wbs.utils.util.string.WbsStrings;

import java.util.*;

public class WandType {
    public static final NamespacedKey KEY =  new NamespacedKey(WbsBuildWands.getInstance(), "wand");

    @NotNull
    private final String wandName;
    @NotNull
    private String displayName;

    @NotNull
    private LinkedHashMap<WandShape, Integer> allowedShapes;

    private String permission;

    private final Material material;
    @NotNull
    private List<String> lore = new LinkedList<>();
    private final boolean shiny = true;

    private boolean useDurability;
    private int durabilityPerBlock = 0;

    public WandType(@NotNull String wandName,
                    @NotNull LinkedHashMap<WandShape, Integer> allowedShapes,
                    Material material) {
        this.wandName = wandName;
        this.displayName = WbsStrings.capitalizeAll(wandName.replaceAll("_", " "));

        this.material = material;

        this.allowedShapes = allowedShapes;
    }

    public String getPermission() {
        return permission;
    }
    public WandType setPermission(String permission) {
        this.permission = permission;
        return this;
    }

    @NotNull
    public ItemStack buildItem() {
        return toWand().buildItem();
    }

    public boolean isWandItem(@NotNull ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;

        String wandName = meta.getPersistentDataContainer().get(KEY, PersistentDataType.STRING);
        return this.wandName.equalsIgnoreCase(wandName);
    }

    public @NotNull String getWandName() {
        return wandName;
    }

    public @NotNull String getDisplayName() {
        return displayName;
    }

    public WandType setDisplayName(String displayName) {
        this.displayName = displayName;
        return this;
    }

    public @NotNull WandShape getDefaultShape() {
        Optional<WandShape> first = allowedShapes.keySet().stream().findFirst();
        if (first.isEmpty()) {
            throw new IllegalStateException("No shapes available.");
        }
        return first.get();
    }

    public boolean useDurability() {
        return useDurability;
    }

    public WandType setUseDurability(boolean useDurability) {
        this.useDurability = useDurability;
        return this;
    }

    public void setLore(@NotNull List<String> lore) {
        this.lore = lore;
    }

    public WandType durabilityPerBlock(int durabilityPerBlock) {
        this.durabilityPerBlock = durabilityPerBlock;
        return this;
    }

    public int durabilityPerBlock() {
        return durabilityPerBlock;
    }

    public WandType setAllowedShapes(@NotNull LinkedHashMap<WandShape, Integer> allowedShapes) {
        this.allowedShapes = allowedShapes;
        return this;
    }

    public @NotNull LinkedHashMap<WandShape, Integer> getAllowedShapes() {
        return this.allowedShapes;
    }

    public BuildWand toWand() {
        return new BuildWand(this);
    }

    public BuildWand fromItem(ItemStack item) {
        return toWand().configure(item);
    }

    public Material getMaterial() {
        return material;
    }

    public List<String> getLore() {
        return Collections.unmodifiableList(lore);
    }

    public boolean isShiny() {
        return shiny;
    }

    @NotNull
    public WandShape getShape(int index) {
        if (allowedShapes.isEmpty()) {
            throw new IllegalStateException("No shapes defined.");
        }
        index %= allowedShapes.size();

        WandShape wandShape = Iterables.get(allowedShapes.keySet(), index);
        Objects.requireNonNull(wandShape);
        return wandShape;
    }

    public int getRadius(WandShape shape) {
        return allowedShapes.get(shape);
    }

    public int indexOf(WandShape shape) {
        return Iterables.indexOf(allowedShapes.keySet(), shape::equals);
    }
}
