package com.mrkid.proxy.p881free;

import java.io.IOException;

/**
 * User: xudong
 * Date: 30/11/2016
 * Time: 10:04 AM
 */
public class P881FreeCrawlerMain {
    public static void main(String[] args) throws IOException {
        System.out.println(new P881FreeCrawler().fetchProxy());
    }
}
