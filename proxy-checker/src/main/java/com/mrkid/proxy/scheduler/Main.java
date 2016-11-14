package com.mrkid.proxy.scheduler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mrkid.proxy.dto.Proxy;
import org.apache.commons.io.FileUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * User: xudong
 * Date: 31/10/2016
 * Time: 3:37 PM
 */
@SpringBootApplication
public class Main {
    public static void main(String[] args) throws IOException {
        if (args.length != 4) {
            System.out.printf("Usage: <originIp> <proxyFile> <proxyCheckerUrl> <output>");
            return;
        }

        String originIp = args[0];
        String proxyFile = args[1];
        String proxyCheckerUrl = args[2];
        String output = args[3];

        final ConfigurableApplicationContext context = SpringApplication.run(Main.class);
        final ProxyChecker proxyChecker = context.getBean(ProxyChecker.class);

        ObjectMapper objectMapper = new ObjectMapper();

        try (final PrintWriter writer = new PrintWriter(new FileWriter(output))) {
            final List<Proxy> proxies = FileUtils.readLines(new File(proxyFile), "utf-8")
                    .stream().map(line
                            -> {
                        try {
                            return objectMapper.readValue(line, Proxy.class);
                        } catch (IOException e) {
                            return null;
                        }
                    }).filter(p -> p != null).collect(Collectors.toList());

            proxyChecker.check(originIp, proxyCheckerUrl,
                    proxies).stream().forEach(line -> writer.println(line));
        }

        context.close();
    }
}
