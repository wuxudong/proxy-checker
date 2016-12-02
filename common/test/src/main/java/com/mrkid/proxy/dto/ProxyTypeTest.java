package com.mrkid.proxy.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

/**
 * User: xudong
 * Date: 02/12/2016
 * Time: 9:21 AM
 */
public class ProxyTypeTest {

    @Test
    public void test1() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();

        Proxy p = new Proxy("http", "127.0.0.1", 3128);

        ProxyCheckResponse r1 = new ProxyCheckResponse();
        r1.setProxy(p);
        r1.setProxyType(ProxyType.HIGH_ANONYMITY_PROXY);

        String out = objectMapper.writeValueAsString(r1);

        System.out.println(out);


        final ProxyCheckResponse r2 = objectMapper.readValue(out, ProxyCheckResponse.class);

        Assert.assertEquals(r1, r2);

    }

}