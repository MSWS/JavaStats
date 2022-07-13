package xyz.msws.formatter;

import xyz.msws.server.ServerData;

import java.util.Collection;

/**
 * Responsible for formatting server data
 */
public interface Formatter {
    default String format(Collection<ServerData> data) {
        return format(data, System.currentTimeMillis());
    }

    String format(Collection<ServerData> data, long time);
}
