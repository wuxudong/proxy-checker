package com.mrkid.proxy;

import com.mrkid.proxy.utils.AddressUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private Logger logger = LoggerFactory.getLogger(BeanConfiguration.class);

    @Value("${isEc2}")
    private boolean isEc2 = false;

    @Bean
    public String originIp() throws SocketException, UnknownHostException {

        String originIp = null;
        if (isEc2) {
            originIp =  new RestTemplate()
                    .getForEntity("http://169.254.169.254/latest/meta-data/public-ipv4", String.class).getBody();

        } else {
            originIp = AddressUtils.getMyIp();
        }

        logger.info("originIp " + originIp);
        return originIp;
    }

    @Bean
    public String proxyCheckUrl(String originIp) {
        return String.format("http://%s:8080/proxy-check", originIp);
    }
}
