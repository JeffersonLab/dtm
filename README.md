# dtm [![CI](https://github.com/JeffersonLab/dtm/actions/workflows/ci.yml/badge.svg)](https://github.com/JeffersonLab/dtm/actions/workflows/ci.yml) [![Docker](https://img.shields.io/docker/v/jeffersonlab/dtm?sort=semver&label=DockerHub)](https://hub.docker.com/r/jeffersonlab/dtm)
A [Java EE 8](https://en.wikipedia.org/wiki/Jakarta_EE) web application for managing downtime at Jefferson Lab built with the [Smoothness](https://github.com/JeffersonLab/smoothness) web template.

![Screenshot](https://github.com/JeffersonLab/dtm/raw/main/Screenshot.png?raw=true "Screenshot")

---
 - [Overview](https://github.com/JeffersonLab/dtm#overview)
 - [Quick Start with Compose](https://github.com/JeffersonLab/dtm#quick-start-with-compose)
 - [Install](https://github.com/JeffersonLab/dtm#install) 
 - [Configure](https://github.com/JeffersonLab/dtm#configure)
 - [Build](https://github.com/JeffersonLab/dtm#build)
 - [Develop](https://github.com/JeffersonLab/dtm#develop) 
 - [Release](https://github.com/JeffersonLab/dtm#release)
 - [Deploy](https://github.com/JeffersonLab/dtm#deploy)
 - [See Also](https://github.com/JeffersonLab/dtm#see-also)  
---

## Overview
The Downtime application allows Operators to log machine downtime events.  Downtime events are caused by one or more incidents, and incidents may occur concurrently.  The machine requires time to recover after all incidents are resolved, and this time is also part of an event.  Short temporary "trips" caused by Fast Shutdown System (FSD) faults, are also recorded.   A trip that lasts longer than five minutes is considered eligible to be a downtime event.  Most downtime events start off as trips.

## Quick Start with Compose
1. Grab project
```
git clone https://github.com/JeffersonLab/dtm
cd dtm
```
2. Launch [Compose](https://github.com/docker/compose)
```
docker compose up
```
3. Navigate to page
```
http://localhost:8080/dtm
```

**Note**: Login with demo username "tbrown" and password "password".

See: [Docker Compose Strategy](https://gist.github.com/slominskir/a7da801e8259f5974c978f9c3091d52c)

## Install
This application requires a Java 11+ JVM and standard library to run, plus a Java EE 8+ application server (developed with Wildfly).


1. Install service [dependencies](https://github.com/JeffersonLab/dtm/blob/main/deps.yml)
2. Download [Wildfly 26.1.3](https://www.wildfly.org/downloads/)
3. [Configure](https://github.com/JeffersonLab/dtm#configure) Wildfly and start it
4. Download [dtm.war](https://github.com/JeffersonLab/dtm/releases) and deploy it to Wildfly
5. Navigate your web browser to [localhost:8080/dtm](http://localhost:8080/dtm)

## Configure

### Configtime
Wildfly must be pre-configured before the first deployment of the app. The [wildfly bash scripts](https://github.com/JeffersonLab/wildfly#configure) can be used to accomplish this. See the [Dockerfile](https://github.com/JeffersonLab/dtm/blob/main/Dockerfile) for an example.

### Runtime
Uses the [Smoothness Environment Variables](https://github.com/JeffersonLab/smoothness#environment-variables) plus the following application specific:

| Name          | Description                                       |
|---------------|---------------------------------------------------|
| DTM_BOOKS_CSV | Comma Separated Logbooks to post CREATED/DELETED entries to.  Defaults to TLOG.  Generally should be set to ELOG in production. |
| RAR_DIR       | Directory path to store Repair Assessment Reports |
| SRM_URL       | Scheme, host, and port to System Readiness Manager (for Downgrade link; formally named HCO) |
| PAC_SCHEDULE_SERVER_URL | Scheme, host, and port to Program Advisory Committee Schedule (for Joule Report) |

There are some [Settings](https://github.com/JeffersonLab/dtm/blob/33d6890eeda911bfea98edb0b85e6d84c2f7c13b/docker/oracle/setup/02_ddl.sql#L107-L113) in the database as well.

## Build
This project is built with [Java 17](https://adoptium.net/) (compiled to Java 11 bytecode), and uses the [Gradle 7](https://gradle.org/) build tool to automatically download dependencies and build the project from source:

```
git clone https://github.com/JeffersonLab/dtm
cd dtm
gradlew build
```
**Note**: If you do not already have Gradle installed, it will be installed automatically by the wrapper script included in the source

**Note for JLab On-Site Users**: Jefferson Lab has an intercepting [proxy](https://gist.github.com/slominskir/92c25a033db93a90184a5994e71d0b78)

See: [Docker Development Quick Reference](https://gist.github.com/slominskir/a7da801e8259f5974c978f9c3091d52c#development-quick-reference)

## Develop
In order to iterate rapidly when making changes it's often useful to run the app directly on the local workstation, perhaps leveraging an IDE.  In this scenario run the service dependencies with:
```
docker compose -f deps.yml up
```
**Note**: The local install of Wildfly should be [configured](https://github.com/JeffersonLab/dtm#configure) to proxy connections to services via localhost and therefore the environment variables should contain:
```
KEYCLOAK_BACKEND_SERVER_URL=http://localhost:8081
FRONTEND_SERVER_URL=https://localhost:8443
```
Further, the local DataSource must also leverage localhost port forwarding so the `standalone.xml` connection-url field should be: `jdbc:oracle:thin:@//localhost:1521/xepdb1`.  

The [server](https://github.com/JeffersonLab/wildfly/blob/main/scripts/server-setup.sh) and [app](https://github.com/JeffersonLab/wildfly/blob/main/scripts/app-setup.sh) setup scripts can be used to setup a local instance of Wildfly. 

## Release
1. Bump the version number in the VERSION file and commit and push to GitHub (using [Semantic Versioning](https://semver.org/)).
2. The [Publish a Release](https://github.com/JeffersonLab/dtm/blob/main/.github/workflows/release.yml) GitHub Action should run automatically to tag the source, create release notes summarizing any pull requests, and attach a war artifact.   Edit the release notes to add any missing details.
3. The [Publish to DockerHub](https://github.com/JeffersonLab/dtm/blob/main/.github/workflows/docker-publish.yml) GitHub Action should run automatically to create, tag, and publish a new demo Docker image, and bump the [compose.override.yaml](https://github.com/JeffersonLab/dtm/blob/main/compose.override.yaml) to use the new image.

## Deploy
At JLab this app is found at [ace.jlab.org/dtm](https://ace.jlab.org/dtm) and internally at [acctest.acc.jlab.org/dtm](https://acctest.acc.jlab.org/dtm).  However, those servers are proxies for `wildfly5.acc.jlab.org` and `wildflytest5.acc.jlab.org` respectively.   A [deploy script](https://github.com/JeffersonLab/wildfly/blob/main/scripts/deploy.sh) is provided to automate wget and deploy.  Example:

```
/root/setup/deploy.sh dtm v1.2.3
```

**JLab Internal Docs**:  [InstallGuideWildflyRHEL9](https://accwiki.acc.jlab.org/do/view/SysAdmin/InstallGuideWildflyRHEL9)

## See Also
 - [JLab ACE management-app list](https://github.com/search?q=org%3Ajeffersonlab+topic%3Aace+topic%3Amanagement-app&type=repositories)
