package xyz.msws;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        configs.add(new ServerConfig("jb.csgo.edgegamers.cc", "Jailbreak"));

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
    }
}
