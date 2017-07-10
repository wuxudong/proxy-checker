FROM minimum2scp/squid

# From https://github.com/dockerfile/java/blob/master/oracle-java8/Dockerfile
RUN \
  sudo apt-get install software-properties-common -y && \
  echo oracle-java8-installer shared/accepted-oracle-license-v1-1 select true | debconf-set-selections && \
  add-apt-repository -y ppa:webupd8team/java && \
  apt-get update && \
  apt-get install -y --allow-unauthenticated oracle-java8-installer && \
  rm -rf /var/lib/apt/lists/* && \
  rm -rf /var/cache/oracle-jdk8-installer

# Define commonly used JAVA_HOME variable
ENV JAVA_HOME /usr/lib/jvm/java-8-oracle

# Wifiyou
RUN \
  ln -s /etc/squid /etc/squid3 && \
  mkdir -p /opt/wifiyou/proxy-checker/crawler/target/ && \
  mkdir -p /opt/wifiyou/proxy-checker/checker/target/

# Define working directory.
WORKDIR /opt/wifiyou/proxy-checker/

COPY crawler/target/crawler-1.0-SNAPSHOT.jar crawler/target/
COPY checker/target/checker-1.0-SNAPSHOT.jar checker/target/
COPY all-in-one.sh update

RUN chmod +x /opt/wifiyou/proxy-checker/update
