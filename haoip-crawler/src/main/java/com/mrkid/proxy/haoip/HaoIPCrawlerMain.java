package com.mrkid.proxy.haoip;

import java.io.IOException;

/**
 * User: xudong
 * Date: 30/11/2016
 * Time: 10:03 AM
 */
public class HaoIPCrawlerMain {
    public static void main(String[] args) throws IOException {
        System.out.println(new HaoIPCrawler().fetchProxy());
    }
}
