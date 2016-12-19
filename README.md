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
`mvn clean -Dmaven.test.skip package`
 
* run crawler & checker client
 
    * run crawler   
    
			java -jar crawler/target/crawler-1.0-SNAPSHOT.jar


    * run checker
    
        	java -jar checker/target/checker-1.0-SNAPSHOT.jar
    
     Valid proxies are exported to ./data/ in plain/squid/... format.
     
     By default, the check concurrency is 1000, you can increase concurrency by modifying ProxyCheckerConfiguration.maxConcurrency. 
     
     If you want high concurrency, do remember to update linux file-max, otherwise it will crash because of *too many open files* problem.
     
    * run them both
    
    
    		java -jar crawler/target/crawler-1.0-SNAPSHOT.jar         
     		java -jar checker/target/checker-1.0-SNAPSHOT.jar



* By default, h2base is used, and data is stored at ./db/proxy.mv.db  

  You can switch to mysql by updating core/src/main/resources/application.properties.
  
* The *etl* is used to batch import proxies manually.
      
   `java -jar etl/target/etl-1.0-SNAPSHOT.jar [filepath] [source_name]`
   
* The *archive* is used to archive inactive proxies. 

  If a proxy fail to check 3 times recently, it is considered as inactive.
 

