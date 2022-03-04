package xyz.msws.server;

import lombok.Getter;
import lombok.Setter;

/**
 * @author MSWS
 *         Represents a snapshot of the server's data.
 */
public class DataSnapshot {
    @Getter
    @Setter
    protected int rank, monthlyRank, percentile, monthlyPercentile;
    @Getter
    @Setter
    protected String name;

    public DataSnapshot() {
        rank = 0;
        monthlyRank = 0;
        percentile = 0;
        monthlyPercentile = 0;
    }

    public DataSnapshot(String name) {
        this();
        this.name = name;
    }

}