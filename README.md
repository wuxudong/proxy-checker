# proxy-checker
It will 

* crawl proxies from cn-proxy.com, you can also add other proxy website as you like.
* check proxies availability
* figure out their type
  * TRANSPARENT_PROXY
  * ANONYMOUS_PROXY
  * DISTORTING_PROXY
  * HIGH_ANONYMITY_PROXY
* output proxies in squid format

## usage
 mvn clean -Dmaven.test.skip packge
 
 java -DisEc2=true/false -jar proxy-checker/all-in-one/target/all-in-one-1.0-SNAPSHOT.jar <outputdir>
