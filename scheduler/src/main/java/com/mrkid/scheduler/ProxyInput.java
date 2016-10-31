package com.mrkid.scheduler;

import lombok.Data;
import lombok.RequiredArgsConstructor;

/**
 * User: xudong
 * Date: 31/10/2016
 * Time: 6:16 PM
 */
@Data
@RequiredArgsConstructor
public class ProxyInput {
    private final String host;
    private final int port;
}
