package com.mrkid.proxy.client;

import com.mrkid.proxy.client.upnp.UpnpRegister;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.net.SocketException;
import java.net.UnknownHostException;

/**
 * User: xudong
 * Date: 31/10/2016
 * Time: 3:17 PM
 */
@SpringBootApplication
public class ClientMain {
    public static void main(String[] args) throws SocketException, UnknownHostException {
        SpringApplication.run(ClientMain.class);

    }

}
