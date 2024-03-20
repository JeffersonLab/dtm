ARG BUILD_IMAGE=gradle:7.4-jdk17
ARG RUN_IMAGE=jeffersonlab/wildfly:1.0.0

################## Stage 0
FROM ${BUILD_IMAGE} as builder
ARG CUSTOM_CRT_URL
USER root
WORKDIR /
RUN if [ -z "${CUSTOM_CRT_URL}" ] ; then echo "No custom cert needed"; else \
       wget -O /usr/local/share/ca-certificates/customcert.crt $CUSTOM_CRT_URL \
       && update-ca-certificates \
       && keytool -import -alias custom -file /usr/local/share/ca-certificates/customcert.crt -cacerts -storepass changeit -noprompt \
       && export OPTIONAL_CERT_ARG=--cert=/etc/ssl/certs/ca-certificates.crt \
    ; fi
COPY . /app
RUN cd /app && gradle build -x test --no-watch-fs $OPTIONAL_CERT_ARG

################## Stage 1
FROM ${RUN_IMAGE} as runner
COPY --from=builder /app/container/app/app-setup.env /
USER root
RUN /server-setup.sh /app-setup.env wildfly_start_and_wait \
     && /app-setup.sh /app-setup.env config_keycloak_client \
     && /app-setup.sh /app-setup.env config_oracle_client \
     && /server-setup.sh /app-setup.env config_email \
     && /server-setup.sh /app-setup.env wildfly_reload \
     && /server-setup.sh /app-setup.env wildfly_stop \
     && rm -rf /opt/jboss/wildfly/standalone/configuration/standalone_xml_history \
USER jboss:jboss
COPY --from=builder /app/build/libs/* /opt/jboss/wildfly/standalone/deployments