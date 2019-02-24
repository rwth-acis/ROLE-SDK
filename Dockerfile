FROM openjdk:8-jdk-alpine

ENV ROLE_SDK_PORT=8073
ENV ROLE_SDK_VERSION=m10
ENV ROLE_SDK_HOME=/opt/role-${ROLE_SDK_VERSION}-sdk

RUN apk add --update bash maven && rm -f /var/cache/apk/*

COPY . /tmp/role-sdk
WORKDIR /tmp/role-sdk
# build, extract and delete build file in one step to reduce image size
RUN mvn clean package && \
    tar -xvzf assembly/target/role-${ROLE_SDK_VERSION}-sdk.tar.gz -C /opt && \
    rm -rf /tmp/role-sdk

# create unprivileged user for execution
RUN addgroup -g 1000 -S role && \
    adduser -u 1000 -S role -G role && \
    chown -R role:role ${ROLE_SDK_HOME}

USER role
WORKDIR ${ROLE_SDK_HOME}
EXPOSE ${ROLE_SDK_PORT}
CMD java -Djetty.host=127.0.0.1 -Djetty.port=${ROLE_SDK_PORT} -jar webapps/jetty-runner.jar --port ${ROLE_SDK_PORT} webapps/role-uu-prototype --path /role .
