package xyz.msws.parser;

import xyz.msws.server.DataSnapshot;

public interface ServerParser<T> {
    DataSnapshot parseData(T data);
}
