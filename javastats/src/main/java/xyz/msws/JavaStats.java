package xyz.msws;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import xyz.msws.formatter.Formatter;
import xyz.msws.formatter.ForumsFormat;
import xyz.msws.parser.GTParser;
import xyz.msws.server.FileServerData;
import xyz.msws.server.ServerConfig;
import xyz.msws.server.ServerData;
import xyz.msws.server.StatConfig;

public class JavaStats {
    private Map<String, ServerData> servers = new HashMap<>();
    private List<ServerConfig> configs = new ArrayList<>();
    private GTParser parser;

    public static void main(String[] args) {
        new JavaStats();
    }

    public JavaStats() {
        configs.add(new ServerConfig("jb.csgo.edgegamers.cc:27015", "Jailbreak"));
        configs.add(new ServerConfig("ttt.csgo.edgegamers.cc:27015", "Trouble in Terrorist Town"));
        configs.add(new ServerConfig("ttt.csgo.edgegamers.cc:27015", "Surf"));
        configs.add(new ServerConfig("surf.csgo.edgegamers.cc:27015", "Surf Summer"));
        configs.add(new ServerConfig("mg.csgo.edgegamers.cc:27015", "Minigames"));
        configs.add(new ServerConfig("bhop.csgo.edgegamers.cc:27015", "Bhop"));
        configs.add(new ServerConfig("104.128.58.205:27015", "AWP Only"));
        configs.add(new ServerConfig("104.128.58.204:27015", "KZ Climb"));

        configs.sort((a, b) -> a.getName().compareTo(b.getName()));

        for (ServerConfig config : configs) {
            servers.put(config.getName(), new FileServerData(new File(config.getName() + ".txt"), config));
        }

        StatConfig statConfig = new StatConfig("https://www.gametracker.com/server_info/");

        parser = new GTParser(statConfig);

        for (ServerConfig config : configs) {
            ServerData data = servers.get(config.getName());
            data.addData(parser.parseData(config));
            data.save();
        }
        Formatter format = new ForumsFormat();
        System.out.println(format.format(servers.values()));
    }
}
