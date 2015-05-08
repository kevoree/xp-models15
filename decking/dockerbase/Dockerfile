FROM lgsd/diamond
MAINTAINER thomas

ENV JAVA_HOME /usr/lib/jvm/java-se-8u40-ri-compact1
ENV PATH $PATH:$JAVA_HOME/bin

ADD dockerbase/java-se-8u40-ri-compact1 $JAVA_HOME/

ADD dockerbase/lib/ld-linux.so.2 /lib/
ADD dockerbase/lib32 /lib/
RUN chmod +x /lib/*

ADD dockerbase/cacerts ${JAVA_HOME}/lib/security/

ADD dockerbase/client.jar client.jar

ENTRYPOINT ["java","-jar","client.jar"]