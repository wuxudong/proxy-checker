package com.mrkid.proxy.checker;

import com.mrkid.proxy.dto.ProxyCheckResponse;
import com.mrkid.proxy.model.Proxy;

import java.io.Closeable;

/**
 * User: xudong
 * Date: 12/12/2016
 * Time: 5:40 PM
 */
public interface ProxyCheckResponseWriter extends Closeable {
    boolean shouldWrite(ProxyCheckResponse response);

    void write(ProxyCheckResponse response);
}
