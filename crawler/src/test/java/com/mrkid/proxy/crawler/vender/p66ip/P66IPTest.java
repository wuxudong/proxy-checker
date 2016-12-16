package com.mrkid.proxy.crawler.vender.p66ip;

import org.junit.Test;

/**
 * User: xudong
 * Date: 15/12/2016
 * Time: 1:24 PM
 */
public class P66IPTest {
    @Test
    public void test() throws Exception {
        System.out.println(new P66IPProxyFetcher(false).crawl());
    }

}
