package com.mrkid.proxy.crawler.vender.cnproxy;

import com.mrkid.proxy.crawler.vender.XsltTestUtils;
import org.junit.Test;

/**
 * User: xudong
 * Date: 15/12/2016
 * Time: 1:24 PM
 */
public class CnProxyXsltTest {
    @Test
    public void test() throws Exception {
        XsltTestUtils.transform("/html/cn-proxy.html", "/xsl/cn-proxy.xsl", "utf-8");
    }

}
