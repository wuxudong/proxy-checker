package com.mrkid.proxy.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

import java.util.Date;

/**
 * User: xudong
 * Date: 31/10/2016
 * Time: 6:16 PM
 */
@Data
@EqualsAndHashCode(of = {"schema", "host", "port"})
public class Proxy {
    private final String schema;
    private final String host;
    private final int port;

    private String location;
    private Date lastCheckSuccess;
    private Date lastCheckFail;

    private String source = "";
}
