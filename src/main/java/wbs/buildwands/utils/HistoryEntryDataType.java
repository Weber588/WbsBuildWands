package wbs.buildwands.utils;

import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import wbs.buildwands.WbsBuildWands;
import wbs.utils.util.persistent.WbsPersistentDataType;

public class HistoryEntryDataType implements PersistentDataType<PersistentDataContainer, HistoryEntry> {
    public static final HistoryEntryDataType INSTANCE = new HistoryEntryDataType();

    private static final NamespacedKey LOCATION_KEY = new NamespacedKey(WbsBuildWands.getInstance(), "location");
    private static final NamespacedKey DATA_KEY = new NamespacedKey(WbsBuildWands.getInstance(), "data-hash");

    @NotNull
    @Override
    public Class<PersistentDataContainer> getPrimitiveType() {
        return PersistentDataContainer.class;
    }

    @NotNull
    @Override
    public Class<HistoryEntry> getComplexType() {
        return HistoryEntry.class;
    }

    @NotNull
    @Override
    public PersistentDataContainer toPrimitive(@NotNull HistoryEntry historyEntry, @NotNull PersistentDataAdapterContext persistentDataAdapterContext) {
        PersistentDataContainer container = persistentDataAdapterContext.newPersistentDataContainer();

        container.set(LOCATION_KEY, WbsPersistentDataType.LOCATION, historyEntry.location());
        container.set(DATA_KEY, PersistentDataType.INTEGER, historyEntry.dataHash());

        return container;
    }

    @SuppressWarnings("ConstantConditions")
    @NotNull
    @Override
    public HistoryEntry fromPrimitive(@NotNull PersistentDataContainer persistentDataContainer, @NotNull PersistentDataAdapterContext persistentDataAdapterContext) {
        Location location = persistentDataContainer.get(LOCATION_KEY, WbsPersistentDataType.LOCATION);
        int dataHash = persistentDataContainer.get(DATA_KEY, PersistentDataType.INTEGER);

        return new HistoryEntry(location, dataHash);
    }
}
