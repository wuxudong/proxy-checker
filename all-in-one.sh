#!/bin/bash

java -DisEc2=true -jar all-in-one/target/all-in-one-1.0-SNAPSHOT.jar

sudo su
#remove all cache_peer

sed -i.bak '/cache_peer/d' /etc/squid/squid.conf

cat data/high_anonymity_proxy.squid >> /etc/squid/squid.conf

squid -k reconfigure



