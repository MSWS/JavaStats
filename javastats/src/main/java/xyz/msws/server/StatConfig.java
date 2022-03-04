package xyz.msws.server;

import java.util.List;

public class StatConfig {
    private String trackerURL;
    private List<ServerConfig> servers;

    public StatConfig(String tracker) {
        this.trackerURL = tracker;
    }
}
