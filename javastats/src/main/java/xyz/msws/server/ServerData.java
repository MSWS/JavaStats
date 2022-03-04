package xyz.msws.server;

import java.util.HashMap;
import java.util.Map;

public abstract class ServerData {
    private Map<Long, Integer> ranks, monthlyRanks;
    private Map<Long, Integer> percentiles, monthlyPercentiles;

    public ServerData() {
        ranks = new HashMap<>();
        monthlyRanks = new HashMap<>();
        percentiles = new HashMap<>();
        monthlyPercentiles = new HashMap<>();
    }

    public abstract void save();
}
