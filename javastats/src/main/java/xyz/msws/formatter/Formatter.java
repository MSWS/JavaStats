package xyz.msws.formatter;

import java.util.Collection;

import xyz.msws.server.ServerData;

public interface Formatter {
    String format(Collection<ServerData> data);
}
