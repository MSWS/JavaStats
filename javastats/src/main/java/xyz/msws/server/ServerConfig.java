package xyz.msws.server;

import lombok.Getter;

public class ServerConfig {
    @Getter
    private String ip, name;
    private int port = 27015;

    public ServerConfig(String ip, String name) {
        this.ip = ip;
        this.name = name;
    }

    public ServerConfig(String ip, String name, int port) {
        this(ip, name);
        this.port = port;
    }
}
