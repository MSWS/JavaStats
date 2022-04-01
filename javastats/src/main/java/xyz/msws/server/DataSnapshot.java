package xyz.msws.server;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

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
    @Getter
    @Setter
    protected long date;

    public DataSnapshot() {
        date = System.currentTimeMillis();
    }

    public DataSnapshot(long date) {
        this();
        rank = 0;
        monthlyRank = 0;
        percentile = 0;
        monthlyPercentile = 0;
        this.date = date;
    }

    public DataSnapshot(long date, String name) {
        this(date);
        this.name = name;
    }

    public DataSnapshot(String data) {
        JsonObject element = JsonParser.parseString(data).getAsJsonObject();
        this.rank = element.get("rank").getAsInt();
        // this.monthlyRank = element.get("monthlyRank").getAsInt();
        // this.percentile = element.get("percentile").getAsInt();
        // this.monthlyPercentile = element.get("monthlyPercentile").getAsInt();
        // this.name = element.get("name").getAsString();
        this.date = element.get("date").getAsLong();
    }

    public JsonObject toJSON() {
        JsonObject obj = new JsonObject();
        obj.addProperty("rank", rank);
        // obj.addProperty("monthlyRank", monthlyRank);
        // obj.addProperty("percentile", percentile);
        // obj.addProperty("monthlyPercentile", monthlyPercentile);
        // obj.addProperty("name", name);
        obj.addProperty("date", date);
        return obj;
    }

}