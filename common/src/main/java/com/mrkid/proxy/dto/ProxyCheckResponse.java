package com.mrkid.proxy.dto;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;

/**
 * User: xudong
 * Date: 31/10/2016
 * Time: 3:02 PM
 */
@Data
public class ProxyCheckResponse {
    private String originIp;
    private String remoteIp;
    private String xForwardedFor;
    private Proxy proxy;

    private boolean valid;

    private ProxyType proxyType = ProxyType.UNKNOWN;

    public ProxyCheckResponse() {
    }


    public ProxyCheckResponse(String originIp, String remoteIp, String xForwardedFor, Proxy proxy, boolean valid) {
        this.originIp = originIp;
        this.remoteIp = remoteIp;
        this.xForwardedFor = xForwardedFor;
        this.proxy = proxy;
        this.valid = valid;
    }

    public ProxyCheckResponse calculateProxyType() {
        if (StringUtils.isBlank(remoteIp)) {
            proxyType = ProxyType.UNKNOWN;
        }

        if (StringUtils.isBlank(xForwardedFor)) {
            proxyType = ProxyType.HIGH_ANONYMITY_PROXY;
        } else {
            if (xForwardedFor.equals(originIp)) {
                proxyType = ProxyType.TRANSPARENT_PROXY;
            } else if (xForwardedFor.equals(remoteIp)) {
                proxyType = ProxyType.ANONYMOUS_PROXY;
            } else {
                proxyType = ProxyType.DISTORTING_PROXY;
            }
        }

        return this;
    }

}
