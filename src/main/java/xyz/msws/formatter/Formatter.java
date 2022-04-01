package xyz.msws.formatter;

import java.util.Collection;

import xyz.msws.server.ServerData;

/**
 * Responsible for formatting server data
 */
public interface Formatter {
    String format(Collection<ServerData> data);
}
