package com.mrkid.scheduler;

import org.apache.commons.io.FileUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
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
        }

        String originIp = args[0];
        String proxyFile = args[1];
        String proxyCheckerUrl = args[2];
        String output = args[3];

        final ConfigurableApplicationContext context = SpringApplication.run(Main.class);
        final Scheduler scheduler = context.getBean(Scheduler.class);


        try (final PrintWriter writer = new PrintWriter(new FileWriter(output))) {
            final List<ProxyInput> proxyInputs = FileUtils.readLines(new File(proxyFile), "utf-8").stream().map(line
                    -> {

                final String[] tokens = line.split(":");
                String host = tokens[0];
                int port = Integer.valueOf(tokens[1]);
                return new ProxyInput(host, port);

            }).collect(Collectors.toList());

            scheduler.check(originIp, proxyCheckerUrl,
                    proxyInputs).stream().forEach(line -> writer.println(line));
        }

        context.close();
    }
}
