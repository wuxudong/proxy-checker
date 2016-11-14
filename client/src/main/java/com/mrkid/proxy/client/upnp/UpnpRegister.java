package com.mrkid.proxy.client.upnp;

import com.mrkid.proxy.utils.AddressUtils;
import org.fourthline.cling.UpnpService;
import org.fourthline.cling.UpnpServiceImpl;
import org.fourthline.cling.support.igd.PortMappingListener;
import org.fourthline.cling.support.model.PortMapping;

import java.net.SocketException;
import java.net.UnknownHostException;

/**
 * User: xudong
 * Date: 04/11/2016
 * Time: 3:21 PM
 */
public class UpnpRegister {
    public void portForward() throws SocketException, UnknownHostException {
        String ip = AddressUtils.getMyIp();
        PortMapping desiredMapping =
                new PortMapping(
                        8080,
                        ip,
                        PortMapping.Protocol.TCP,
                        "My Port Mapping"
                );

        UpnpService upnpService =
                new UpnpServiceImpl(
                        new PortMappingListener(desiredMapping)
                );

        upnpService.getControlPoint().search();
    }

}
