FROM lgsd/diamond
MAINTAINER thomas

ENV JAVA_HOME /usr/lib/jvm/java-se-8u40-ri
ENV PATH $PATH:$JAVA_HOME/bin

ADD dockerbase/java-se-8u40-ri $JAVA_HOME/

ADD dockerbase/lib/ld-linux.so.2 /lib/
ADD dockerbase/lib32 /lib/
RUN chmod +x /lib/*

ADD dockerbase/cacerts ${JAVA_HOME}/lib/security/

ADD dockerbase/hello.jar hello.jar
ENTRYPOINT ["java","-jar","hello.jar"]