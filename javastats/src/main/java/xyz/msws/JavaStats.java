package xyz.msws;

import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import xyz.msws.parser.GTParser;
import xyz.msws.parser.ServerParser;
import xyz.msws.server.DataSnapshot;

/**
 * Hello world!
 *
 */
public class JavaStats {
    public static void main(String[] args) {
        try {
            Document doc = Jsoup.connect("https://www.gametracker.com/server_info/jb.csgo.edgegamers.cc:27015/").get();
            ServerParser<String> parser = new GTParser();
            parser.parseData(doc.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
