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
    public static int TRANSPARENT_PROXY = 1;
    public static int ANONYMOUS_PROXY = 2;
    public static int DISTORTING_PROXY = 3;
    public static int HIGH_ANONYMITY_PROXY = 4;


    private String originIp;
    private String remoteIp;
    private String xForwardedFor;
    private Proxy proxy;

    private boolean valid;

    private int proxyType;

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
            proxyType = 0;
        }

        if (StringUtils.isBlank(xForwardedFor)) {
            proxyType = HIGH_ANONYMITY_PROXY;
        } else {
            if (xForwardedFor.equals(originIp)) {
                proxyType = TRANSPARENT_PROXY;
            } else if (xForwardedFor.equals(remoteIp)) {
                proxyType = ANONYMOUS_PROXY;
            } else {
                proxyType = DISTORTING_PROXY;
            }
        }

        return this;
    }
}
