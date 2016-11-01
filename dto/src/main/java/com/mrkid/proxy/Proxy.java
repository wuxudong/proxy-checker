package com.mrkid.proxy;

import lombok.Data;
import lombok.RequiredArgsConstructor;

/**
 * User: xudong
 * Date: 31/10/2016
 * Time: 6:16 PM
 */
@Data
public class Proxy {
    private final String schema;
    private final String host;
    private final int port;
}
