#!/bin/bash

#this script should be run as root

java -jar crawler/target/crawler-1.0-SNAPSHOT.jar

java -jar checker/target/checker-1.0-SNAPSHOT.jar

#remove all cache_peer
sed -i.bak '/cache_peer/d' /etc/squid3/squid.conf

count=$(cat data/proxy.squid | grep HIGH_ANONYMITY | wc -l)
echo Updated $count proxies.

cat data/proxy.squid | grep HIGH_ANONYMITY | sed 's/#HIGH_ANONYMITY.*//' >> /etc/squid3/squid.conf

squid3 -k reconfigure



