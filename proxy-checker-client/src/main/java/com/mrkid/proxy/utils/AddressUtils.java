package com.mrkid.proxy.utils;

import org.jsoup.Jsoup;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * User: xudong
 * Date: 04/11/2016
 * Time: 1:13 PM
 */
public class AddressUtils {
    public static String getMyPublicIp() throws IOException {
        final String body = Jsoup.connect("http://1212.ip138.com/ic.asp").get().body().text();
        final Matcher matcher = Pattern.compile(".*\\[(.*)\\].*").matcher(body);

        if (matcher.find()) {
            return matcher.group(1);
        } else {
            throw new RuntimeException("can not parse ip from " + body);
        }
    }
}
