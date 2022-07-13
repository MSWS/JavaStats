package xyz.msws.parser;

import xyz.msws.data.DataSnapshot;

/**
 * Responsible for parsing specific types of data into {@link DataSnapshot}s.
 */
public interface ServerParser<T> {
    DataSnapshot parseData(T data);
}
