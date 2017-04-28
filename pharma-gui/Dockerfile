FROM openjdk:8-jre-alpine

ENV AD_AUTHORITY https://difa-adfs-if.difa.ad/adfs
ENV AD_CLIENT_ID a2846c81-5959-4187-8bec-76cb4faae2cb

COPY target/pharma-gui-*.jar /pharmacy.no/
# COPY data-dumps/* /data-dumps/

WORKDIR /pharmacy.no
CMD ["java", "-Xmx2g", "-jar", "pharma-gui-0.1-SNAPSHOT.jar"]
