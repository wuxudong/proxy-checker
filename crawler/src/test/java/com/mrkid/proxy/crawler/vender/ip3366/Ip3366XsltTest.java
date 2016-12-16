package com.mrkid.proxy.crawler.vender.ip3366;

import com.mrkid.proxy.crawler.vender.XsltTestUtils;
import org.junit.Test;

/**
 * User: xudong
 * Date: 15/12/2016
 * Time: 1:24 PM
 */
public class Ip3366XsltTest {

    @Test
    public void test() throws Exception {
        XsltTestUtils.transform("/html/ip3366.html", "/xsl/ip3366.xsl", "gb2312");
    }
}
