package com.mrkid.proxy.service;

import com.mrkid.proxy.dto.ProxyCheckResponse;
import com.mrkid.proxy.dto.ProxyDTO;
import com.mrkid.proxy.model.LowQualityProxy;
import com.mrkid.proxy.model.Proxy;
import com.mrkid.proxy.model.ProxyKey;
import com.mrkid.proxy.repository.LowQualityProxyRepository;
import com.mrkid.proxy.repository.ProxyRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * User: xudong
 * Date: 12/12/2016
 * Time: 1:40 PM
 */
@Component
public class ProxyService {

    @Autowired
    private ProxyRepository proxyRepository;

    @Autowired
    private LowQualityProxyRepository lowQualityProxyRepository;

    private final int failTimeLimit = 3;


    @Transactional
    public Proxy saveProxy(ProxyDTO proxyDTO) {
        ProxyKey key = new ProxyKey(proxyDTO.getType(), proxyDTO.getHost(), proxyDTO.getPort());
        Proxy proxy = proxyRepository.findOne(key);
        if (proxy == null) {
            proxy = new Proxy();
            proxy.setKey(key);

            proxy.setSource(proxyDTO.getSource());
            proxy.setLocation(proxyDTO.getLocation());
        } else {
            proxy.setSource(proxyDTO.getSource());
            proxy.setLocation(proxyDTO.getLocation());
        }

        return proxyRepository.save(proxy);
    }

    @Transactional
    public Proxy saveProxyCheckResponse(ProxyCheckResponse proxyCheckResponse) {

        ProxyDTO proxyDTO = proxyCheckResponse.getProxy();
        ProxyKey key = new ProxyKey(proxyDTO.getType(), proxyDTO.getHost(), proxyDTO.getPort());

        Proxy proxy = proxyRepository.getOne(key);

        proxy.setValid(proxyCheckResponse.isValid());
        proxy.setAnonymityType(proxyCheckResponse.getAnonymityType().getKey());

        if (proxyCheckResponse.isValid()) {
            proxy.setLastCheckSuccess(new Date());
        } else {
            proxy.setLastCheckFail(new Date());
            proxy.setRecentFailTimes(proxy.getRecentFailTimes() + 1);
        }

        return proxyRepository.save(proxy);
    }

    @Transactional
    public List<Proxy> getValidProxies() {
        return proxyRepository.findByValidIsTrue();

    }

    @Transactional
    public List<Proxy> getCheckableProxies(int page, int size) {
        return proxyRepository.findByValidIsTrueOrRecentFailTimesLessThanOrderByKeyHost(failTimeLimit, new PageRequest
                (page, size));
    }


    @Transactional
    public List<Proxy> getInactiveProxies(int page, int size) {
        return proxyRepository.findByValidIsFalseAndRecentFailTimesGreaterThanEqualOrderByKeyHost(failTimeLimit, new
                PageRequest(page, size));
    }

    @Transactional
    public void archive(Proxy proxy) {
        proxyRepository.delete(proxy);

        LowQualityProxy lowQualityProxy = lowQualityProxyRepository.findOne(proxy.getKey());

        if (lowQualityProxy == null) {
            lowQualityProxy = new LowQualityProxy();
            BeanUtils.copyProperties(proxy, lowQualityProxy);

            lowQualityProxyRepository.save(lowQualityProxy);
        }
    }


}
