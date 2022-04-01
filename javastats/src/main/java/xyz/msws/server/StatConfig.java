package xyz.msws.server;

import java.util.List;

import lombok.Getter;

public class StatConfig {
    @Getter
    private String trackerURL;
    @Getter
    private List<ServerConfig> servers;

    public StatConfig(String baseURL) {
        this.trackerURL = baseURL;
    }
}
