package xyz.msws.parser;

import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import xyz.msws.server.DataSnapshot;
import xyz.msws.server.ServerConfig;
import xyz.msws.server.StatConfig;

public class GTParser implements ServerParser<String> {

    private String baseUrl;

    public GTParser(StatConfig config) {
        // baseUrl = "https://www.gametracker.com/server_info/";
        this.baseUrl = config.getTrackerURL();
    }

    @Override
    public DataSnapshot parseData(String data) {
        System.out.printf("Parsing %s\n", data);
        Document doc = Jsoup.parse(data);
        DataSnapshot snapshot = new DataSnapshot();

        String content = doc.select("div.block630_content_left").text();

        int nameStart = "Server Summary Name: ".length();
        int nameEnd = content.indexOf(" Game:", nameStart);
        String name = content.substring(nameStart, nameEnd);

        int rankStart = content.indexOf("Game Server Rank: ") + "Game Server Rank: ".length();
        int rankEnd = content.indexOf("th", rankStart);
        int rank = Integer.parseInt(content.substring(rankStart, rankEnd));

        int percStart = content.indexOf("th", rankEnd) + 4;
        int percEnd = content.indexOf("th Percentile)", percStart);
        int percentile = Integer.parseInt(content.substring(percStart, percEnd));
        System.out.println("Rank: " + rank + " Percentile: " + percentile);

        snapshot.setName(name);
        snapshot.setRank(rank);
        snapshot.setPercentile(percentile);
        return snapshot;
    }

    public DataSnapshot parseData(ServerConfig config) {
        try {
            Document doc = Jsoup.connect("https://www.gametracker.com/server_info/jb.csgo.edgegamers.cc:27015/").get();
            return parseData(doc.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}
