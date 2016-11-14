package com.mrkid.proxy.utils;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Collections;

/**
 * User: xudong
 * Date: 04/11/2016
 * Time: 1:13 PM
 */
public class AddressUtils {
    public static String getMyIp() throws SocketException, UnknownHostException {
        return Collections.list(NetworkInterface.getNetworkInterfaces())
                .stream()
                .flatMap(i -> Collections.list(i.getInetAddresses()).stream())
                .filter(addr -> !addr.isLoopbackAddress() && addr.getHostAddress().contains("."))
                .findFirst().orElse(InetAddress.getLocalHost())
                .getHostAddress();

    }

}
