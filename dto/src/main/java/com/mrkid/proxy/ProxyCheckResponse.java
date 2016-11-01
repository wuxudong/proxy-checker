package com.mrkid.proxy;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;

/**
 * User: xudong
 * Date: 31/10/2016
 * Time: 3:02 PM
 */
@Data
@RequiredArgsConstructor
public class ProxyCheckResponse {
    public static int TRANSPARENT_PROXY = 1;
    public static int ANONYMOUS_PROXY = 2;
    public static int DISTORTING_PROXY = 3;
    public static int HIGH_ANONYMITY_PROXY = 4;


    private final String originIp;
    private final String remoteIp;
    private final String xForwardedFor;
    private final Proxy proxy;

    private final boolean valid;

    public int getProxyType() {
        if (StringUtils.isBlank(remoteIp)) {
            return 0;
        }

        if (StringUtils.isBlank(xForwardedFor)) {
            return HIGH_ANONYMITY_PROXY;
        } else {
            if (xForwardedFor.equals(originIp)) {
                return TRANSPARENT_PROXY;
            } else if (xForwardedFor.equals(remoteIp)) {
                return ANONYMOUS_PROXY;
            } else {
                return DISTORTING_PROXY;
            }
        }
    }
}
