package wbs.buildwands.wand;

import com.google.common.base.Strings;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import wbs.buildwands.WbsBuildWands;
import wbs.buildwands.shapes.ShapeManager;
import wbs.buildwands.shapes.WandShape;
import wbs.buildwands.utils.HistoryEntry;
import wbs.buildwands.utils.HistoryEntryDataType;
import wbs.utils.util.ShinyEnchantment;
import wbs.utils.util.WbsEnums;
import wbs.utils.util.WbsItems;
import wbs.utils.util.particles.CuboidParticleEffect;
import wbs.utils.util.plugin.WbsMessenger;
import wbs.utils.util.string.WbsStrings;

import java.util.*;

import static wbs.buildwands.wand.WandType.KEY;

public class BuildWand extends WbsMessenger {
    // For some reason the interact event fires again for the click against the placed blocks,
    // so we need to have a blocker so this can only happen once per tick.

    private static final Set<UUID> USED_WAND_THIS_TICK = new HashSet<>();

    private static int taskId = -1;

    private static void setUsed(UUID uuid) {
        USED_WAND_THIS_TICK.add(uuid);

        if (taskId == -1) {
            taskId = WbsBuildWands.getInstance().runSync(() -> {
                USED_WAND_THIS_TICK.clear();
                taskId = -1;
            });
        }
    }

    public static final NamespacedKey RADIUS_KEY = new NamespacedKey(WbsBuildWands.getInstance(), "radius");
    public static final NamespacedKey SHAPE_KEY = new NamespacedKey(WbsBuildWands.getInstance(), "shape");
    public static final NamespacedKey LOCATIONS_KEY = new NamespacedKey(WbsBuildWands.getInstance(), "locations");

    @NotNull
    private WandShape shape;
    protected int radius;

    private final boolean useDurability;
    private final int durabilityPerBlock;

    private final WandType type;

    @Nullable
    private List<String> previousLore = new LinkedList<>();

    @Nullable
    private List<HistoryEntry> history;

    public BuildWand(WandType type) {
        super(WbsBuildWands.getInstance());
        this.type = type;

        this.shape = type.getDefaultShape();
        this.radius = type.getRadius(shape);
        this.useDurability = type.useDurability();
        this.durabilityPerBlock = type.durabilityPerBlock();
    }

    @NotNull
    public LinkedHashMap<Location, BlockData> select(Player player, Block block, BlockFace face, int radius) {
        return shape.select(player, block, face, radius);
    }

    private String fillPlaceholders(String string) {
        String radiusString = radius <= 0 ? "∞" : String.valueOf(radius);
        return string.replaceAll("%radius%", radiusString)
                .replaceAll("%shape%", shape.getDisplayName())
                + getInvisibleKey();
    }

    public @NotNull WandShape getShape() {
        return shape;
    }

    public void setShape(@NotNull WandShape shape) {
        this.shape = shape;
    }

    public int getRadius() {
        return radius;
    }

    public BuildWand setRadius(int radius) {
        this.radius = radius;
        return this;
    }

    private void updatePreviousLore() {
        previousLore = new LinkedList<>();
        type.getLore().forEach(line -> previousLore.add(plugin.dynamicColourise(fillPlaceholders(line))));
    }

    public boolean useDurability() {
        return useDurability;
    }

    public int getDurabilityPerBlock() {
        return durabilityPerBlock;
    }

    public BuildWand configure(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            throw new IllegalArgumentException("Wand must be a metadata holding item (i.e. not air).");
        }

        updatePreviousLore();

        PersistentDataContainer container = meta.getPersistentDataContainer();

        String shapeName = container.get(SHAPE_KEY, PersistentDataType.STRING);
        if (shapeName == null) {
            shapeName = type.getDefaultShape().getName();
        }

        WandShape shape = ShapeManager.getShape(shapeName);
        if (shape != null) {
            setShape(shape);
        }

        Integer radius = container.get(RADIUS_KEY, PersistentDataType.INTEGER);
        if (radius != null) {
            setRadius(radius);
        } else {
            setRadius(type.getRadius(shape));
        }

        PersistentDataContainer locationsContainer = container.get(LOCATIONS_KEY, PersistentDataType.TAG_CONTAINER);

        if (locationsContainer != null && !locationsContainer.isEmpty()) {
            history = new LinkedList<>();
            for (NamespacedKey key : locationsContainer.getKeys()) {
                HistoryEntry entry = locationsContainer.get(key, HistoryEntryDataType.INSTANCE);
                history.add(entry);
            }
        }

        return this;
    }

    public void updateItem(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            throw new IllegalArgumentException("Wand must be a metadata holding item (i.e. not air).");
        }

        PersistentDataContainer container = meta.getPersistentDataContainer();

        if (radius != type.getRadius(shape)) {
            container.set(RADIUS_KEY, PersistentDataType.INTEGER, radius);
        } else {
            container.remove(RADIUS_KEY);
        }

        if (shape != type.getDefaultShape()) {
            container.set(SHAPE_KEY, PersistentDataType.STRING, shape.getName());
        } else {
            container.remove(SHAPE_KEY);
        }

        if (history != null && !history.isEmpty()) {
            PersistentDataAdapterContext context = container.getAdapterContext();
            PersistentDataContainer locationsContainer = context.newPersistentDataContainer();

            int i = 0;
            for (HistoryEntry entry : history) {
                locationsContainer.set(new NamespacedKey(plugin, String.valueOf(i)),
                        HistoryEntryDataType.INSTANCE,
                        entry);
                i++;
            }

            container.set(LOCATIONS_KEY, PersistentDataType.TAG_CONTAINER, locationsContainer);
        } else {
            // Clear any old undone history
            container.remove(LOCATIONS_KEY);
        }

        meta.setDisplayName(plugin.dynamicColourise(getDisplayName()));

        if (type.isShiny()) {
            meta.addEnchant(ShinyEnchantment.SHINY, 0, true);
        } else {
            meta.removeEnchant(ShinyEnchantment.SHINY);
        }

        updateLore(meta);

        item.setItemMeta(meta);
    }

    private void updateLore(ItemMeta meta) {
        List<String> updatedLore = new LinkedList<>();

        List<String> lore = meta.getLore();
        if (lore != null) {
            lore.forEach(line -> {
                if (!line.contains(getInvisibleKey())) {
                    updatedLore.add(line);
                }
            });
        }

        type.getLore().forEach(line -> updatedLore.add(0, fillPlaceholders(line)));

        meta.setLore(plugin.colouriseAll(updatedLore));
    }

    public String getDisplayName() {
        return fillPlaceholders(type.getDisplayName());
    }

    public boolean cycleShape(@Nullable Player player) {
        LinkedHashMap<WandShape, Integer> shapes = type.getAllowedShapes();

        if (shapes.size() <= 1) {
            if (player != null) {
                sendActionBar("&wNo other shapes available.", player);
            }
            return false;
        }

        int index = type.indexOf(shape);

        if (index != -1) {
            updatePreviousLore();
            setShape(type.getShape(index + 1));
            setRadius(type.getRadius(shape));
            if (player != null) {
                sendActionBar("Shape changed: &h" + shape.getDisplayName(), player);
            }
            return true;
        } else {
            throw new IllegalStateException("Current shape does not exist in allowed shapes map.");
        }
    }

    public WandType getType() {
        return type;
    }

    public BuildWand setHistory(List<HistoryEntry> history) {
        this.history = history;
        return this;
    }

    public boolean undo(Player player) {
        if (history == null || history.isEmpty()) {
            sendActionBar("&wNothing to undo!", player);
            return false;
        }

        List<ItemStack> toReturn = new LinkedList<>();

        int undone = 0;
        for (HistoryEntry entry : history) {
            Location loc = entry.location();
            int dataHash = entry.dataHash();

            BlockData data = loc.getBlock().getBlockData();
            if (data.hashCode() == dataHash) {
                Material placementMaterial = data.getPlacementMaterial();

                toReturn.add(new ItemStack(placementMaterial));
                loc.getBlock().setType(Material.AIR);
                undone++;
            }
        }
        // Return next tick to avoid replacing held wand in hotbar
        plugin.runSync(() -> {
            HashMap<Integer, ItemStack> failed = player.getInventory()
                    .addItem(toReturn.toArray(new ItemStack[0]));

            for (ItemStack item : failed.values()) {
                player.getWorld().dropItemNaturally(player.getLocation(), item);
            }
        });

        if (undone > 0) {
            history.clear();
            sendActionBar("Undid " + undone + " block(s).", player);
            return true;
        } else {
            sendActionBar("&wNothing to undo!", player);
            return false;
        }
    }

    public ItemStack buildItem() {
        ItemStack item = new ItemStack(type.getMaterial());

        ItemMeta meta = Bukkit.getItemFactory().getItemMeta(type.getMaterial());
        if (meta == null) {
            throw new IllegalArgumentException("Material must have meta defined");
        }

        meta.getPersistentDataContainer().set(KEY, PersistentDataType.STRING, type.getWandName());

        item.setItemMeta(meta);
        
        updateItem(item);

        return item;
    }

    public boolean tryPlacing(Player player, ItemStack item, Block block, BlockFace face) {
        if (USED_WAND_THIS_TICK.contains(player.getUniqueId())) {
            return false;
        }

        Material placedMaterial = block.getType();
        Material materialToPlace = block.getBlockData().getPlacementMaterial();

        int totalPlaceable = getTotalPlaceable(player, materialToPlace);

        if (totalPlaceable == 0) {
            sendActionBar("&wNo " + WbsEnums.toPrettyString(placedMaterial) + " in inventory!", player);
            return false;
        }

        LinkedHashMap<Location, BlockData> finalLocations = limitedSelect(player, block, face, totalPlaceable);

        if (finalLocations.isEmpty()) {
            sendActionBar("&wNo valid locations!", player);
            return false;
        }

        List<HistoryEntry> history = new LinkedList<>();
        ItemStack materialInstance = new ItemStack(materialToPlace);

        int blocksPlaced = 0;
        for (Location location : finalLocations.keySet()) {
            // TODO: Add player place block event and check for cancelled.

            if (!player.getInventory().removeItem(materialInstance).isEmpty()) {
                break;
            }

            Block targetBlock = location.getBlock();
            targetBlock.setType(placedMaterial);
            targetBlock.setBlockData(finalLocations.get(location));
            blocksPlaced++;
            history.add(new HistoryEntry(location));

            if (item instanceof Damageable damageable) {
                if (this.useDurability()) {
                    if (damageable.getDamage() + this.getDurabilityPerBlock() >= item.getType().getMaxDurability()) {
                        WbsItems.damageItem(player, item, this.getDurabilityPerBlock());
                    } else {
                        // Don't keep going if wand will break; just stop early.
                        break;
                    }
                }
            }
        }

        this.setHistory(history)
                .updateItem(item);

        setUsed(player.getUniqueId());
        sendActionBar("Placed " + blocksPlaced + " blocks", player);
        return true;
    }

    @NotNull
    private LinkedHashMap<Location, BlockData> limitedSelect(Player player, Block block, BlockFace face, int totalPlaceable) {
        LinkedHashMap<Location, BlockData> locations = this.select(player, block, face, radius);

        return getLimitedLocations(totalPlaceable, locations);
    }

    @NotNull
    private LinkedHashMap<Location, BlockData> getLimitedLocations(int totalPlaceable,
                                                                   LinkedHashMap<Location, BlockData> locations) {
        LinkedHashMap<Location, BlockData> finalLocations = new LinkedHashMap<>();

        int blocksToPlace = Math.min(totalPlaceable, locations.size());
        int i = 0;
        for (Location location : locations.keySet()) {
            finalLocations.put(location, locations.get(location));
            i++;
            if (i >= blocksToPlace) {
                break;
            }
        }

        return finalLocations;
    }

    private int getTotalPlaceable(Player player, Material materialToPlace) {
        HashMap<Integer, ? extends ItemStack> placeableStacks = player.getInventory().all(materialToPlace);

        // TODO: Add settings to block items with lore, or another way of identifying it?

        int totalPlaceable = 0;
        for (ItemStack placeable : placeableStacks.values()) {
            totalPlaceable += placeable.getAmount();
        }

        return totalPlaceable;
    }

    public void preview(Player player, Block block, BlockFace face) {
        if (USED_WAND_THIS_TICK.contains(player.getUniqueId())) {
            return;
        }

        Material placementMaterial = block.getBlockData().getPlacementMaterial();
        int totalPlaceable = getTotalPlaceable(player, placementMaterial);

        if (totalPlaceable == 0) {
            sendActionBar("&wNo " + WbsEnums.toPrettyString(placementMaterial) + " in inventory!", player);
            return;
        }

        LinkedHashMap<Location, BlockData> locations = limitedSelect(player, block, face, totalPlaceable);

        if (locations.isEmpty()) {
            sendActionBar("&wNo valid locations!", player);
            return;
        }

        CuboidParticleEffect effect = new CuboidParticleEffect();
        effect.setScaleAmount(true);
        effect.setAmount(5);
        effect.setOptions(new Particle.DustOptions(Color.RED, 0.8f));

        Particle particle = Particle.REDSTONE;
        for (Location loc : locations.keySet()) {
            effect.configureBlockOutline(loc, loc);
            effect.play(particle, loc.clone().add(0.5, 0.5, 0.5), player);
        }

        setUsed(player.getUniqueId());
        sendActionBar("&hPreviewing " + Math.min(locations.size(), totalPlaceable) + " blocks.", player);
    }

    private String getInvisibleKey() {
        return plugin.dynamicColourise("&0 " + Strings.repeat("&e ", 5));
    }
}
