package xyz.msws.server;

import org.junit.Before;
import org.junit.Test;
import xyz.msws.data.DataSnapshot;

import static org.junit.Assert.*;

public class TestServerData {

    private ServerData data;

    @Before
    public void generateTestServer() {
        this.data = new ServerData(null) {
            @Override
            public void save() {
                throw new UnsupportedOperationException("testData expected to save");
            }
        };
    }

    @Test
    public void testExact() {
        assertTrue(data.addData(new DataSnapshot(2000)));
        assertTrue(data.addData(new DataSnapshot(2000 + ServerData.CACHE_TIME + 1)));
        assertTrue(data.addData(new DataSnapshot(2000 + ServerData.CACHE_TIME * 2 + 1)));

        assertEquals("Failed to fetch exact date", data.getDataAt(2000 + ServerData.CACHE_TIME + 1).get().getDate(), 2000 + ServerData.CACHE_TIME + 1);
    }

    @Test
    public void testSingularPrevious() {
        assertTrue(data.addData(new DataSnapshot(2000)));

        assertEquals("Failed to fetch date previous", data.getDataAt(1000).get().getDate(), 2000);
    }

    @Test
    public void testSingularAfter() {
        assertTrue(data.addData(new DataSnapshot(2000)));

        assertEquals("Failed to fetch date after", data.getDataAt(3000).get().getDate(), 2000);
    }

    @Test
    public void testMiddle() {
        assertTrue(data.addData(new DataSnapshot(999)));
        assertTrue(data.addData(new DataSnapshot(2000 + ServerData.CACHE_TIME)));

        assertEquals("Failed to fetch middle date", data.getDataAt(1000).get().getDate(), 999);
    }

    @Test
    public void testMiddleHigh() {
        assertTrue(data.addData(new DataSnapshot(999)));
        assertTrue(data.addData(new DataSnapshot(2000 + ServerData.CACHE_TIME)));

        assertEquals("Failed to fetch date previous", data.getDataAt(1999).get().getDate(), 999);
    }

    @Test
    public void testAbsent() {
        assertTrue(data.getDataAt(0).isEmpty());
    }

    @Test
    public void testSaveOne() {
        DataSnapshot testSnap = new DataSnapshot(2000);
        assertTrue(data.addData(testSnap));
        assertEquals(1, data.snapshots.size());
        assertEquals(data.snapshots.get(2000L), testSnap);
    }

    @Test
    public void testSaveTwo() {
        DataSnapshot testSnapA = new DataSnapshot(2000);
        DataSnapshot testSnapB = new DataSnapshot(2000 + ServerData.CACHE_TIME);
        assertTrue(data.addData(testSnapA));
        assertTrue(data.addData(testSnapB));
        assertEquals(2, data.snapshots.size());
        assertEquals(data.snapshots.get(2000L), testSnapA);
        assertEquals(data.snapshots.get(2000 + ServerData.CACHE_TIME), testSnapB);
    }

    @Test
    public void testSaveCache() {
        DataSnapshot testSnapA = new DataSnapshot(2000);
        DataSnapshot testSnapB = new DataSnapshot(2000 + ServerData.CACHE_TIME / 2);
        assertTrue(data.addData(testSnapA));
        assertFalse(data.addData(testSnapB));
        assertEquals(1, data.snapshots.size());
        assertEquals(data.snapshots.get(2000L), testSnapA);
    }

    @Test
    public void testSaveContinuity() {
        DataSnapshot testSnapA = new DataSnapshot(2000 + ServerData.CACHE_TIME);
        DataSnapshot testSnapB = new DataSnapshot(2000);
        assertTrue(data.addData(testSnapA));
        assertFalse(data.addData(testSnapB));
        assertEquals(1, data.snapshots.size());
        assertEquals(data.snapshots.get(2000 + ServerData.CACHE_TIME), testSnapA);
    }

    @Test
    public void testSort() {
        DataSnapshot decoyA = new DataSnapshot(0), decoyB = new DataSnapshot(0);
        decoyA.setRank(1);
        decoyB.setRank(2);
        data.addData(decoyA);
        ServerData data2 = new ServerData(null) {
            @Override
            public void save() {
                throw new UnsupportedOperationException();
            }
        };
        data2.addData(decoyB);
        assertEquals(data.compareTo(data2), -1);
    }
}
