# proxy-checker
It will 

* crawl proxies from www.coobobo.com, cn-proxy.com, kxdaili.com, 881free.com, haoip.cc, , ip3366.net, www.66ip.cn.  
  the free proxies of www.goubanjia.com and www.kuaidaili.com are always unavailable, they are removed.   
 you can also add other proxy website as you like.
* check proxies availability
* figure out their type
  * TRANSPARENT_PROXY
  * ANONYMOUS_PROXY
  * DISTORTING_PROXY
  * HIGH_ANONYMITY_PROXY
* output proxies in squid format
* Generally, you will get around ~500 avaiable proxies, which contain ~200 high anonymity proxies.

## usage
`mvn clean -Dmaven.test.skip packge`
 
* run crawler & checker client
 
    * only run crawler   
    
    `java -jar proxy-checker-client-1.0-SNAPSHOT.jar -a crawl`


    * only run checker
    
    `java -jar proxy-checker-client-1.0-SNAPSHOT.jar -a check`
    
    * run them both
    
    `java -jar proxy-checker-client-1.0-SNAPSHOT.jar -a all`
