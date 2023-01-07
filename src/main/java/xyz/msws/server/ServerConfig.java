package xyz.msws.server;

import lombok.Getter;

public class ServerConfig {
    @Getter
    private final String ip;
    @Getter
    private final String name;

    public ServerConfig(String ip, String name) {
        this.ip = ip;
        this.name = name;
    }
}
