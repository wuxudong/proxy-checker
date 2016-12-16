package com.mrkid.proxy.etl;

import com.mrkid.proxy.dto.ProxyDTO;
import com.mrkid.proxy.service.ProxyService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * User: xudong
 * Date: 31/10/2016
 * Time: 3:17 PM
 */
@SpringBootApplication(scanBasePackages = "com.mrkid.proxy")
@EnableJpaRepositories("com.mrkid.proxy")
@EntityScan("com.mrkid.proxy")
@Component
public class EtlApp {
    @Autowired
    private ProxyService proxyService;

    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            System.out.println("java EtlApp [file]");
            return;
        }

        File file = new File(args[0]);

        final ConfigurableApplicationContext context = SpringApplication.run(EtlApp.class);

        context.getBean(EtlApp.class).etl(file);

        context.close();

    }

    private void etl(File file) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line = null;
            while ((line = reader.readLine()) != null) {
                if (StringUtils.isNotBlank(line)) {
                    String[] tokens = line.split(":");
                    String host = tokens[0];
                    int port = Integer.valueOf(tokens[1]);

                    ProxyDTO proxy = new ProxyDTO();
                    proxy.setHost(host);
                    proxy.setPort(port);

                    proxyService.saveProxy(proxy);
                }
            }
        }
    }
}
