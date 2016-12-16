package com.mrkid.proxy.crawler.vender.coobobo;

import com.mrkid.proxy.crawler.vender.XsltTestUtils;
import org.junit.Test;

/**
 * User: xudong
 * Date: 15/12/2016
 * Time: 1:24 PM
 */
public class CooboboXsltTest {
    @Test
    public void test() throws Exception {
        XsltTestUtils.transform("/html/coobobo.html", "/xsl/coobobo.xsl", "utf-8");
    }
}
