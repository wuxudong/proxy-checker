package com.mrkid.proxy.model;

import lombok.Data;

import javax.persistence.Embeddable;
import java.io.Serializable;

/**
 * User: xudong
 * Date: 20/10/2016
 * Time: 7:50 PM
 */
@Data
@Embeddable
public class ProxyKey implements Serializable{
    private String type;

    private String host;

    private int port;

    public ProxyKey() {
    }

    public ProxyKey(String type, String host, int port) {
        this.type = type;
        this.host = host;
        this.port = port;
    }
}
