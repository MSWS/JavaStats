package xyz.msws.server;

import lombok.Getter;
import xyz.msws.data.DataSnapshot;

import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

public abstract class ServerData implements Comparable<ServerData> {
    public static final long CACHE_TIME = TimeUnit.HOURS.toMillis(8);

    protected Map<Long, DataSnapshot> snapshots = new TreeMap<>();
    @Getter
    protected final ServerConfig config;

    public ServerData(ServerConfig config) {
        this.config = config;
    }

    /**
     * Gets the closest snapshot to the given time
     * If no snapshot is found, returns the latest snapshot
     * If no snapshots exist, returns null
     *
     * @param time The time to get the snapshot for
     * @return The closest snapshot to the given time or null if no snapshots exist
     */
    public Optional<DataSnapshot> getDataAt(long time) {
        long bigDiff = Long.MAX_VALUE;
        DataSnapshot lastSnap = null;
        for (Map.Entry<Long, DataSnapshot> entry : snapshots.entrySet()) {
            long diff = Math.abs(entry.getKey() - time);
            if (diff > bigDiff)
                continue;
            bigDiff = diff;
            lastSnap = entry.getValue();
        }
        return Optional.ofNullable(lastSnap);
    }

    /**
     * Adds the {@link DataSnapshot} to the list of snapshots
     *
     * @param data The {@link DataSnapshot} to add
     * @return True if the snapshot was successfully added, false if it was not (ie
     * caching)
     */
    public boolean addData(DataSnapshot data) {
        Optional<DataSnapshot> current = getDataAt(data.getDate());
        if (current.isPresent() && data.getDate() < current.get().getDate())
            return false;
        if (current.isPresent() && data.getDate() - current.get().getDate() < CACHE_TIME)
            return false;
        snapshots.put(data.getDate(), data);
        return true;
    }

    @Override
    public int compareTo(ServerData o) {
        if (o.getDataAt(System.currentTimeMillis()).isEmpty() && this.getDataAt(System.currentTimeMillis()).isEmpty())
            return 0;
        int a = o.getDataAt(System.currentTimeMillis()).get().getRank();
        int b = this.getDataAt(System.currentTimeMillis()).get().getRank();
        return Integer.compare(b, a);
    }

    public abstract void save();
}
