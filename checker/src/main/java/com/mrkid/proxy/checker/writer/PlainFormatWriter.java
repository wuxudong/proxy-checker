package com.mrkid.proxy.checker.writer;

import com.mrkid.proxy.checker.ProxyCheckResponseWriter;
import com.mrkid.proxy.dto.ProxyCheckResponse;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * User: xudong
 * Date: 16/12/2016
 * Time: 5:25 PM
 */
public class PlainFormatWriter implements ProxyCheckResponseWriter {
    final PrintWriter writer;

    public PlainFormatWriter(File dataDirectory) throws IOException {
        writer = new PrintWriter(new FileWriter(new File(dataDirectory,
                "proxy.plain")));
    }

    @Override
    public boolean shouldWrite(ProxyCheckResponse response) {
        return response.isValid() && "http".equalsIgnoreCase(response.getProxy().getSchema());
    }

    @Override
    public void write(ProxyCheckResponse response) {
        writer.println(response.getProxy().getHost() + ":" + response.getProxy().getPort());

    }

    @Override
    public void close() throws IOException {
        writer.close();
    }
}
