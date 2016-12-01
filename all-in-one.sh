#!/bin/bash

#this script should be run as root

ip=`curl http://169.254.169.254/latest/meta-data/public-ipv4`

java -jar proxy-checker-client/target/proxy-checker-client-1.0-SNAPSHOT.jar -s http://${ip}:8080/proxy-check -a all

#remove all cache_peer
sed -i.bak '/cache_peer/d' /etc/squid/squid.conf

cat data/high_anonymity_proxy.squid >> /etc/squid/squid.conf

squid -k reconfigure



