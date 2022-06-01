package xyz.msws.server;

import static org.junit.Assert.assertEquals;

import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

import lombok.Getter;

public abstract class ServerData implements Comparable<ServerData> {
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
            if (entry.getKey() > time)
                return Optional.ofNullable(lastSnap == null ? snapshots.values().stream().findAny().orElse(null)
                        : lastSnap);
            lastSnap = entry.getValue();
        }
        return Optional.ofNullable(lastSnap);
    }

    /**
     * Adds the {@link DataSnapshot} to the list of snapshots
     * 
     * @param data The {@link DataSnapshot} to add
     * @return True if the snapshot was successfully added, false if it was not (ie
     *         caching)
     */
    public boolean addData(DataSnapshot data) {
        if (getDataAt(System.currentTimeMillis()).isPresent()
                && System.currentTimeMillis() - getDataAt(System.currentTimeMillis()).get().getDate() < TimeUnit.HOURS
                        .toMillis(8))
            return false;
        snapshots.put(data.getDate(), data);
        return true;
    }

    @Override
    public int compareTo(ServerData o) {
        if (o.getDataAt(System.currentTimeMillis()) == null && this.getDataAt(System.currentTimeMillis()) == null)
            return 0;
        int a = o.getDataAt(System.currentTimeMillis()).get().getRank();
        int b = this.getDataAt(System.currentTimeMillis()).get().getRank();
        return Integer.compare(b, a);
    }

    public abstract void save();

    public ServerData generateTestServer() {
        return new ServerData(config) {
            @Override
            public void save() {
                throw new UnsupportedOperationException("testData expected to save");
            }
        };
    }

    @Test
    public void testExact() {
        ServerData data = generateTestServer();
        data.addData(new DataSnapshot(2000));
        data.addData(new DataSnapshot(5000));
        data.addData(new DataSnapshot(6000));

        assertEquals("Failed to fetch exact date", data.getDataAt(5000).get().getDate(), 5000);
    }

    @Test
    public void testSingularPrevious() {
        ServerData data = generateTestServer();
        data.addData(new DataSnapshot(2000));

        assertEquals("Failed to fetch date previous", data.getDataAt(1000).get().getDate(), 1000);
    }

    @Test
    public void testSingularAfter() {
        ServerData data = generateTestServer();
        data.addData(new DataSnapshot(2000));

        assertEquals("Failed to fetch date after", data.getDataAt(3000).get().getDate(), 3000);
    }
}
