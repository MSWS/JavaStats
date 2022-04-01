package xyz.msws.server;

import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import lombok.Getter;

public abstract class ServerData {
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
        DataSnapshot lastSnap = null;
        for (Map.Entry<Long, DataSnapshot> entry : snapshots.entrySet()) {
            if (entry.getKey() <= time)
                return Optional.of(entry.getValue());
            lastSnap = entry.getValue();
        }
        return Optional.ofNullable(lastSnap);
    }

    public void addData(DataSnapshot data) {
        if (System.currentTimeMillis() - getDataAt(System.currentTimeMillis()).get().getDate() < TimeUnit.HOURS
                .toMillis(12))
            return;
        snapshots.put(data.getDate(), data);
    }

    public abstract void save();
}
