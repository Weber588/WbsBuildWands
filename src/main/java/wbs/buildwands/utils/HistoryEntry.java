package wbs.buildwands.utils;

import org.bukkit.Location;

import java.util.Objects;

public final class HistoryEntry {
    private final Location location;
    private final int dataHash;

    public HistoryEntry(Location location, int dataHash) {
        this.location = location;
        this.dataHash = dataHash;
    }

    public HistoryEntry(Location location) {
        this(location, location.getBlock().getBlockData().hashCode());
    }

    public Location location() {
        return location;
    }

    public int dataHash() {
        return dataHash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (HistoryEntry) obj;
        return Objects.equals(this.location, that.location) &&
                this.dataHash == that.dataHash;
    }

    @Override
    public int hashCode() {
        return Objects.hash(location, dataHash);
    }

    @Override
    public String toString() {
        return "HistoryEntry[" +
                "location=" + location + ", " +
                "dataHash=" + dataHash + ']';
    }

}
