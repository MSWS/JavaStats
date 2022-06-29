package xyz.msws.parser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import xyz.msws.server.DataSnapshot;
import xyz.msws.server.ServerConfig;
import xyz.msws.server.StatConfig;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * GameTracker implementation of {@link ServerParser}.
 * Scrapes raw HTML of respective server's page.
 */
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
        int rankEnd = getIndex(content, rankStart);
        int rank = Integer.parseInt(content.substring(rankStart, rankEnd));

        int percStart = getIndex(content, rankEnd) + 4;
        int percEnd = getIndex(content, " Percentile)", percStart);
        int percentile = Integer.parseInt(content.substring(percStart, percEnd));

        int monthHighest = 0, monthLowest = 0;
        try {
            int monthHighestStart = content.indexOf("Highest (past month): ") + "Highest (past month): ".length();
            int monthHighestEnd = getIndex(content, monthHighestStart);
            monthHighest = Integer.parseInt(content.substring(monthHighestStart, monthHighestEnd));

            int monthLowestStart = content.indexOf("past month): ", monthHighestEnd) + "past month): ".length();
            int monthLowestEnd = getIndex(content, monthLowestStart);
            monthLowest = Integer.parseInt(content.substring(monthLowestStart, monthLowestEnd));
        } catch (NumberFormatException e) {
            System.out.println("Error occurred when fetching monthly statistics");
            e.printStackTrace();
        }

        snapshot.setName(name);
        snapshot.setRank(rank);
        snapshot.setPercentile(percentile);
        snapshot.setMonthlyHigh(monthHighest);
        snapshot.setMonthlyLow(monthLowest);
        return snapshot;
    }

    private int getIndex(String content, String suffix, int start) {
        List<Integer> inds = new ArrayList<>();
        inds.add(content.indexOf("nd" + suffix, start));
        inds.add(content.indexOf("rd" + suffix, start));
        inds.add(content.indexOf("st" + suffix, start));
        inds.add(content.indexOf("th" + suffix, start));
        inds.removeIf(i -> i == -1);
        try {
            inds.sort(Integer::compareTo);
        } catch (NumberFormatException e) {
            return -1;
        }
        return inds.get(0);
    }

    private int getIndex(String content, int start) {
        return getIndex(content, "", start);
    }

    public DataSnapshot parseData(ServerConfig config) {
        try {
            Document doc = Jsoup.connect(baseUrl + config.getIp()).get();
            try {
                return parseData(doc.toString());
            } catch (IndexOutOfBoundsException e) {
                System.out.println("Failed to parse " + doc.toString());
                throw e;
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Failed to fetch data from " + baseUrl + config.getIp());
        }
        return null;
    }

}
