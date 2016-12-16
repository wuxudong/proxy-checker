package com.mrkid.proxy.crawler.vender.ip3366;

import org.junit.Test;

/**
 * User: xudong
 * Date: 15/12/2016
 * Time: 7:06 PM
 */
public class Ip3366Test {
    @Test
    public void test() throws Exception {
        System.out.println(new Ip3366ProxyFetcher().crawl());
    }
}
