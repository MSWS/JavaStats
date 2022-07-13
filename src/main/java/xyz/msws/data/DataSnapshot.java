package xyz.msws.data;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.Getter;
import lombok.Setter;

/**
 * @author MSWS
 * Represents a snapshot of the server's data.
 */
public class DataSnapshot {
    @Getter
    @Setter
    protected int rank, monthlyHigh, monthlyLow, percentile; // -1 if not available
    @Getter
    @Setter
    protected String name; // May be null
    @Getter
    @Setter
    protected long date;

    public DataSnapshot() {
        date = System.currentTimeMillis();
    }

    public DataSnapshot(long date) {
        this();
        rank = -1;
        monthlyHigh = -1;
        monthlyLow = -1;
        percentile = -1;
        this.date = date;
    }

    public DataSnapshot(long date, String name) {
        this(date);
        this.name = name;
    }

    public DataSnapshot(String data) {
        JsonObject element = JsonParser.parseString(data).getAsJsonObject();
        this.rank = element.get("rank").getAsInt(); // If this doesn't work then UH OH
        this.date = element.get("date").getAsLong(); // If this doesn't work then UH OH
        this.monthlyHigh = getDefault(element, "monthlyHigh", -1);
        this.monthlyHigh = getDefault(element, "monthlyLow", -1);
        this.percentile = getDefault(element, "percentile", -1);
        // this.name = getDefault(element, "name", null);
    }

    public JsonObject toJSON() {
        JsonObject obj = new JsonObject();
        obj.addProperty("rank", rank);
        obj.addProperty("monthlyHigh", monthlyHigh);
        obj.addProperty("monthlyLow", monthlyLow);
        obj.addProperty("percentile", percentile);
        // obj.addProperty("name", name);
        obj.addProperty("date", date);
        return obj;
    }

    public int getDefault(JsonObject obj, String key, int def) {
        if (obj.get(key) == null || obj.get(key).isJsonNull())
            return def;
        return obj.get(key).getAsInt();
    }

    public long getDefault(JsonObject obj, String key, long def) {
        if (obj.get(key) == null || obj.get(key).isJsonNull())
            return def;
        return obj.get(key).getAsLong();
    }

    public String getDefault(JsonObject obj, String key, String def) {
        if (obj.get(key) == null || obj.get(key).isJsonNull())
            return def;
        return obj.get(key).getAsString();
    }

}