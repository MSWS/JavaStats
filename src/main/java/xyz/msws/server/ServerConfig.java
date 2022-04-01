package xyz.msws.server;

import lombok.Getter;

public class ServerConfig {
    @Getter
    private String ip, name;

    public ServerConfig(String ip, String name) {
        this.ip = ip;
        this.name = name;
    }
}
