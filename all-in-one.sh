#!/bin/bash

#this script should be run as root

java -jar proxy-checker-client/target/proxy-checker-client-1.0-SNAPSHOT.jar -a all

#remove all cache_peer
sed -i.bak '/cache_peer/d' /etc/squid3/squid.conf

cat data/proxy.squid | grep HIGH_ANONYMITY | sed 's/#HIGH_ANONYMITY.*//' >> /etc/squid3/squid.conf

squid3 -k reconfigure



