package xyz.msws.server;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public abstract class ServerData {
    protected Map<Long, DataSnapshot> snapshots = new HashMap<>();
    protected final ServerConfig config;

    public ServerData(ServerConfig config) {
        this.config = config;
        snapshots = new HashMap<>();
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
        snapshots.put(data.getDate(), data);
    }

    public abstract void save();
}
