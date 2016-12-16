package com.mrkid.proxy.crawler.vender.kuaidaili;

import org.junit.Test;

/**
 * User: xudong
 * Date: 15/12/2016
 * Time: 1:24 PM
 */
public class KuaidailiTest {
    @Test
    public void test() throws Exception {
        System.out.println(new KuaiDaiLiProxyFetcher().crawl());
    }

}
