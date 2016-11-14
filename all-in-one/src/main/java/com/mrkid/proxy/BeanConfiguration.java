package com.mrkid.proxy;

import com.mrkid.proxy.utils.AddressUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.SocketException;
import java.net.UnknownHostException;

/**
 * User: xudong
 * Date: 03/11/2016
 * Time: 4:35 PM
 */
@Configuration
public class BeanConfiguration {
    @Bean
    public String originIp() throws SocketException, UnknownHostException {
        return AddressUtils.getMyIp();
    }
}
