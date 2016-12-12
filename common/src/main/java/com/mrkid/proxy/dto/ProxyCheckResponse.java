package com.mrkid.proxy.dto;

import lombok.Data;

/**
 * User: xudong
 * Date: 31/10/2016
 * Time: 3:02 PM
 */
@Data
public class ProxyCheckResponse {
    private String originIp;
    private String remoteIp;
    private ProxyDTO proxy;

    private boolean valid;

    private ProxyType proxyType = ProxyType.UNKNOWN;

    public ProxyCheckResponse() {
    }


    public ProxyCheckResponse(String originIp, String remoteIp, ProxyDTO proxy, boolean valid) {
        this.originIp = originIp;
        this.remoteIp = remoteIp;
        this.proxy = proxy;
        this.valid = valid;
    }

}
