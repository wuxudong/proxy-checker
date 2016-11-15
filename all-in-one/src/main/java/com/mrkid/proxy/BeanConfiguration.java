package com.mrkid.proxy;

import com.mrkid.proxy.utils.AddressUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.net.SocketException;
import java.net.UnknownHostException;

/**
 * User: xudong
 * Date: 03/11/2016
 * Time: 4:35 PM
 */
@Configuration
public class BeanConfiguration {
    @Value("isEc2")
    private boolean isEc2 = false;

    @Bean
    public String originIp() throws SocketException, UnknownHostException {
        if (isEc2) {
            return new RestTemplate()
                    .getForEntity("http://169.254.169.254/latest/meta-data/public-ipv4", String.class).getBody();

        } else {
            return AddressUtils.getMyIp();

        }

    }
}
