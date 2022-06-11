FROM maven:3.8.5-eclipse-temurin-11-alpine as maven_build
WORKDIR /accounts-sso
COPY pom.xml .
# RUN mvn clean package -Dmaven.main.skip -Dmaven.test.skip && rm -r target
COPY src ./src
# RUN mvn clean package -Dmaven.test.skip -Dspring.profiles.active=dev

ENV JAVA_MINIMAL="/opt/customjre"
RUN rm -rf /opt/customjre
RUN --mount=type=cache,target=/root/.m2 mvn clean package  -Dmaven.test.skip -Dspring.profiles.active=dev && \

    jdeps \
    --ignore-missing-deps \
    -q \
    -cp 'lib/*' \
    -recursive \
    --print-module-deps \
    ./target/Accounts-SSO-Provider-0.0.1.jar > jre-deps.info && \

    touch complete-jre-deps.info && \
    tr -d "\n" < jre-deps.info > complete-jre-deps.info && rm -f jre-deps.info && \
    echo ",java.xml,jdk.unsupported,java.sql,java.naming,java.desktop,java.management,java.security.jgss,java.instrument" >> complete-jre-deps.info && \


    /opt/java/openjdk/bin/jlink \
    --verbose \
    --module-path /opt/java/openjdk/jmods \
    --compress=2 --strip-debug --no-header-files --no-man-pages \
    --release-info="add:IMPLEMENTOR=accApp:IMPLEMENTOR_VERSION=accApp_JRE" \
    --output "$JAVA_MINIMAL" \
    --add-modules $(cat complete-jre-deps.info),
#     && rm -f jre-deps.info

FROM alpine:latest
ENV JAVA_HOME=/opt/customjre
ENV PATH=$PATH:$JAVA_HOME/bin
ENV DB_HOST_IP=172.17.0.1
ENV DB_PORT=3306
ENV DB_DATABASE=AccountsDB

# run docker with --network="host"
ENV STORAGE_SERVICE=http://127.0.0.1:8000
COPY --from=maven_build $JAVA_HOME $JAVA_HOME

WORKDIR /accounts-sso
COPY --from=maven_build /accounts-sso/target/*.jar ./
ENTRYPOINT ["java", "-Dspring.profiles.active=dev", "-jar", "Accounts-SSO-Provider-0.0.1.jar"]