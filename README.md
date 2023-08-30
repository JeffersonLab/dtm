# dtm [![CI](https://github.com/JeffersonLab/dtm/actions/workflows/ci.yml/badge.svg)](https://github.com/JeffersonLab/dtm/actions/workflows/ci.yml) [![Docker](https://img.shields.io/docker/v/jeffersonlab/dtm?sort=semver&label=DockerHub)](https://hub.docker.com/r/jeffersonlab/dtm)
A [Java EE 8](https://en.wikipedia.org/wiki/Jakarta_EE) web application for managing downtime at Jefferson Lab built with the [Smoothness](https://github.com/JeffersonLab/smoothness) web template.

![Screenshot](https://github.com/JeffersonLab/dtm/raw/main/Screenshot.png?raw=true "Screenshot")

---
 - [Overview](https://github.com/JeffersonLab/dtm#overview)
 - [Quick Start with Compose](https://github.com/JeffersonLab/dtm#quick-start-with-compose)
 - [Install](https://github.com/JeffersonLab/dtm#install) 
 - [Configure](https://github.com/JeffersonLab/dtm#configure)
 - [Build](https://github.com/JeffersonLab/dtm#build)
 - [Release](https://github.com/JeffersonLab/dtm#release)
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

| Name    | Description                                       |
|---------|---------------------------------------------------|
| RAR_DIR | Directory path to store Repair Assessment Reports |
| SRM_URL | Scheme, host, and port to System Readiness Manager (for Downgrade link; formally named HCO) |
| PAC_SCHEDULE_SERVER_URL | Scheme, host, and port to Program Advisory Committee Schedule (for Joule Report) |

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

## Release
1. Bump the version number and release date in build.gradle and commit and push to GitHub (using [Semantic Versioning](https://semver.org/)).
2. Create a new release on the GitHub Releases page corresponding to the same version in the build.gradle. The release should enumerate changes and link issues. A war artifact can be attached to the release to facilitate easy install by users.
3. Build and publish a new Docker image [from the GitHub tag](https://gist.github.com/slominskir/a7da801e8259f5974c978f9c3091d52c#8-build-an-image-based-of-github-tag). GitHub is configured to do this automatically on git push of semver tag (typically part of GitHub release) or the [Publish to DockerHub](https://github.com/JeffersonLab/dtm/actions/workflows/docker-publish.yml) action can be manually triggered after selecting a tag.
4. Bump and commit quick start [image version](https://github.com/JeffersonLab/dtm/blob/main/docker-compose.override.yml)
