package com.mrkid.proxy.dto;

import lombok.Data;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

/**
 * User: xudong
 * Date: 31/10/2016
 * Time: 6:16 PM
 */
@XmlRootElement(name = "proxies")
@XmlAccessorType(XmlAccessType.FIELD)
@Data
public class ProxyListDTO {
    @XmlElement(name = "proxy")
    private List<ProxyDTO> proxies = new ArrayList<ProxyDTO>();

}
