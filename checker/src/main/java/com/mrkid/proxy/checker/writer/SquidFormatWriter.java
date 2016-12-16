package com.mrkid.proxy.checker.writer;

import com.mrkid.proxy.checker.ProxyCheckResponseWriter;
import com.mrkid.proxy.dto.ProxyCheckResponse;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * User: xudong
 * Date: 16/12/2016
 * Time: 5:26 PM
 */
public class SquidFormatWriter implements ProxyCheckResponseWriter {
    final PrintWriter writer;

    private final Object object = new Object();

    /**
     * same ip, diff port is not allowed in squid
     */
    private Map<String, Object> exportedHosts = new ConcurrentHashMap<>();

    public SquidFormatWriter(File dataDirectory) throws IOException {
        writer = new PrintWriter(new FileWriter(new File(dataDirectory,
                "proxy.squid")));
    }

    @Override
    public boolean shouldWrite(ProxyCheckResponse response) {
        return response.isValid() && "http".equalsIgnoreCase(response.getProxy().getSchema());
    }

    @Override
    public void write(ProxyCheckResponse response) {
        String host = response.getProxy().getHost();
        if (exportedHosts.putIfAbsent(host, object) == null) {
            writer.println(
                    String.format(
                            "cache_peer %s parent %d 0 round-robin no-query connect-fail-limit=1 #%s %s",
                            host,
                            response.getProxy().getPort(),
                            response.getProxyType().name(),
                            response.getProxy().getSource()));
        }

    }

    @Override
    public void close() throws IOException {
        writer.close();

    }
}
