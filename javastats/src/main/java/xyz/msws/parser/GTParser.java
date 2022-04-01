package xyz.msws.parser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import xyz.msws.server.DataSnapshot;
import xyz.msws.server.ServerConfig;
import xyz.msws.server.StatConfig;

public class GTParser implements ServerParser<String> {

    private String baseUrl;

    public GTParser(StatConfig config) {
        this.baseUrl = config.getTrackerURL();
    }

    @Override
    public DataSnapshot parseData(String data) {
        Document doc = Jsoup.parse(data);
        DataSnapshot snapshot = new DataSnapshot();

        String content = doc.select("div.block630_content_left").text();

        int nameStart = "Server Summary Name: ".length();
        int nameEnd = content.indexOf(" Game:", nameStart);
        String name = content.substring(nameStart, nameEnd);

        int rankStart = content.indexOf("Game Server Rank: ") + "Game Server Rank: ".length();
        // int rankEnd = content.indexOf("th", rankStart);
        int rankEnd = getIndex(content, rankStart);
        int rank = Integer.parseInt(content.substring(rankStart, rankEnd));

        // int percStart = content.indexOf("th", rankEnd) + 4;
        int percStart = getIndex(content, rankEnd, 4);
        int percEnd = content.indexOf("th Percentile)", percStart);
        int percentile = Integer.parseInt(content.substring(percStart, percEnd));
        System.out.println("Rank: " + rank + " Percentile: " + percentile);

        snapshot.setName(name);
        snapshot.setRank(rank);
        snapshot.setPercentile(percentile);
        return snapshot;
    }

    private int getIndex(String content, int start, int offset) {
        List<Integer> inds = new ArrayList<>();
        inds.add(content.indexOf("th", start));
        inds.add(content.indexOf("nd", start));
        inds.add(content.indexOf("st", start));
        inds.removeIf(i -> i == -1);
        inds.sort(Integer::compareTo);
        return inds.get(0) + offset;
    }

    private int getIndex(String content, int start) {
        return getIndex(content, start, 0);
    }

    public DataSnapshot parseData(ServerConfig config) {
        try {
            Document doc = Jsoup.connect("https://www.gametracker.com/server_info/" + config.getIp()).get();
            return parseData(doc.toString());
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println(
                    "Failed to fetch data from \"https://www.gametracker.com/server_info/" + config.getIp() + "\"");
        }
        return null;
    }

}
