package xyz.msws.formatter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.StringJoiner;

import xyz.msws.server.DataSnapshot;
import xyz.msws.server.ServerData;

public class ForumsFormat implements Formatter {

    private static SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");

    @Override
    public String format(Collection<ServerData> servers) {
        List<ServerData> sorted = new ArrayList<>(servers);
        StringJoiner builder = new StringJoiner(System.lineSeparator());
        builder.add(sdf.format(System.currentTimeMillis()));
        builder.add("[B]Counter-Strike: Global Offensive[/B]");
        builder.add("[LIST]");
        Collections.sort(sorted);
        for (ServerData data : sorted) {
            builder.add(format(data));
        }
        builder.add("[/LIST]");
        return builder.toString();
    }

    private String format(ServerData data) {
        StringJoiner builder = new StringJoiner(System.lineSeparator());
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(System.currentTimeMillis());
        DataSnapshot dataNow = data.getDataAt(cal.getTimeInMillis()).get();

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
        return "#" + nRank + " [B][COLOR=" + getColor(old, nRank) + "]" + generate(nRank, old, true)
                + "[/COLOR][/B]";
    }

    private String getColor(int old, int nRank) {
        return old == nRank ? "white" : nRank > old ? "red" : "green";
    }

}
