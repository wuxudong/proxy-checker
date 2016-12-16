package com.mrkid.proxy.repository;

import com.mrkid.proxy.model.Proxy;
import com.mrkid.proxy.model.ProxyKey;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * User: xudong
 * Date: 21/10/2016
 * Time: 11:43 AM
 */
public interface ProxyRepository extends JpaRepository<Proxy, ProxyKey> {

    List<Proxy> findByValidIsTrue();

    List<Proxy> findByValidIsTrueOrRecentFailTimesLessThanOrderByKeyHost(int failTimeLimit, Pageable page);

    List<Proxy> findByValidIsFalseAndRecentFailTimesGreaterThanEqualOrderByKeyHost(int failTimeLimit, Pageable page);
}
