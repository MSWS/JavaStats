package xyz.msws.server;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;

/**
 * Represents the base URL and list of {@link ServerConfig}s
 */
public class StatConfig {
    @Getter
    private String trackerURL;
    @Getter
    private List<ServerConfig> servers = new ArrayList<>();

    public StatConfig(String baseURL) {
        this.trackerURL = baseURL;
    }

    public ServerConfig add(ServerConfig config) {
        servers.add(config);
        return config;
    }

    public ServerConfig add(String ip, String name) {
        return add(new ServerConfig(ip, name));
    }
}
