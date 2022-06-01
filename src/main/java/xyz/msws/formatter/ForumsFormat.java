package xyz.msws.formatter;

import xyz.msws.server.DataSnapshot;
import xyz.msws.server.ServerData;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Forum implementation of a {@link Formatter}
 * Intended with BBCode and Forum-style formatting
 */
public class ForumsFormat implements Formatter {

    private static SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");

    static {
        sdf.setTimeZone(TimeZone.getTimeZone("PST"));
    }

    @Override
    public String format(Collection<ServerData> servers, long time) {
        List<ServerData> sorted = new ArrayList<>(servers);
        StringJoiner builder = new StringJoiner(System.lineSeparator());
        builder.add(sdf.format(time));

        builder.add("[LIST]");
        Collections.sort(sorted); // Sort by ranking "low" to "high"
        for (ServerData data : sorted)
            builder.add(format(data, time));

        builder.add("[/LIST]");
        return builder.toString();
    }

    private String format(ServerData data, long time) {
        StringJoiner builder = new StringJoiner(System.lineSeparator());
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(time);
        DataSnapshot dataNow = data.getDataAt(cal.getTimeInMillis()).get();

        // This should be already in a [LIST] according to the parent format method
        builder.add(String.format("[*][B]%s[/B]", data.getConfig().getName()));
        builder.add("[LIST]");

        cal.add(Calendar.DATE, -1);
        builder.add(String.format("[*]Daily: %s",
                generate(dataNow.getRank(), data.getDataAt(cal.getTimeInMillis()).get().getRank(), false)));

        cal.add(Calendar.DATE, 1);
        cal.set(Calendar.DATE, 1);
        builder.add(String.format("[*]Month-to-Date: %s",
                generate(dataNow.getRank(), data.getDataAt(cal.getTimeInMillis()).get().getRank(), true)));

        cal.set(Calendar.MONTH, 1);
        builder.add(String.format("[*]Year-to-Date: %s",
                generate(dataNow.getRank(), data.getDataAt(cal.getTimeInMillis()).get().getRank(), true)));
        builder.add("[/LIST]");
        return builder.toString();
    }

    private String generate(int nRank, int old, boolean ytd) {
        if (ytd)
            return "[COLOR=" + getColor(old, nRank) + "][B](" + (nRank > old ? "-" : "") + Math.abs(nRank - old)
                    + ")[/B][/COLOR]";
        return "#" + nRank + " " + generate(nRank, old, true);
    }

    private String getColor(int old, int nRank) {
        return old == nRank ? "white" : nRank > old ? "red" : "green";
    }

}
