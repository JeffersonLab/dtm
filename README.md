# dtm [![CI](https://github.com/JeffersonLab/dtm/actions/workflows/ci.yaml/badge.svg)](https://github.com/JeffersonLab/dtm/actions/workflows/ci.yaml) [![Docker](https://img.shields.io/docker/v/jeffersonlab/dtm?sort=semver&label=DockerHub)](https://hub.docker.com/r/jeffersonlab/dtm)
A [Jakarta EE 10](https://en.wikipedia.org/wiki/Jakarta_EE) web application for managing downtime at Jefferson Lab built with the [Smoothness](https://github.com/JeffersonLab/smoothness) web template.

![Screenshot](https://github.com/JeffersonLab/dtm/raw/main/Screenshot.png?raw=true "Screenshot")

---
 - [Overview](https://github.com/JeffersonLab/dtm#overview)
 - [User Guide](https://github.com/JeffersonLab/dtm/blob/main/docs/user-guide/README.md)
 - [Quick Start with Compose](https://github.com/JeffersonLab/dtm#quick-start-with-compose)
 - [Dependencies](https://github.com/JeffersonLab/dtm#dependencies)
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

See the [User Guide](https://jeffersonlab.github.io/dtm/latest/) for how to use it.

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

**Note**: Login with demo username "jdoe" and password "password".

### Ports
The containers publish these ports on the host, listening only on its local address (127.0.0.1), so other computers can't reach the demo database, accounts, and management consoles.  To use them from another computer, forward them over SSH, such as `ssh -N -L 8443:localhost:8443 -L 8081:localhost:8081 <host>` for the app and Keycloak.  If another project already uses some of them, or you run a second copy of the demo, set other ports in a `.env` file next to `compose.yaml`, which Compose reads, or in the environment:

| Variable                     | Default | Port of                   |
|------------------------------|---------|---------------------------|
| DTM_HTTPS_PORT               | 8443    | app (HTTPS)               |
| DTM_HTTP_PORT                | 8080    | app (HTTP)                |
| DTM_MANAGEMENT_PORT          | 9990    | Wildfly management        |
| DTM_KEYCLOAK_PORT            | 8081    | Keycloak                  |
| DTM_KEYCLOAK_MANAGEMENT_PORT | 9991    | Keycloak management       |
| DTM_ORACLE_PORT              | 1521    | Oracle                    |
| DTM_ORACLE_EM_PORT           | 5500    | Oracle Enterprise Manager |
| DTM_PUPPET_PORT              | 3000    | Puppet Show               |
| DTM_SMTP_PORT                | 1025    | MailHog SMTP              |
| DTM_MAILHOG_PORT             | 8025    | MailHog web UI            |

The app and Keycloak are set up for the ports you choose, so log in at `https://localhost:<DTM_HTTPS_PORT>/dtm`.

Compose names the containers, network, and images after the project's directory, such as `dtm-oracle-1`, so a copy in another directory, such as a git worktree, gets its own.

See: [Docker Compose Strategy](https://gist.github.com/slominskir/a7da801e8259f5974c978f9c3091d52c)

## Dependencies
What dtm depends on: what each is used for, where it comes from, its license, and how it is maintained, as of November 2026.  Library versions are pinned in [build.gradle](https://github.com/JeffersonLab/dtm/blob/main/build.gradle), and the container images are named in [deps.yaml](https://github.com/JeffersonLab/dtm/blob/main/deps.yaml) and the [Dockerfile](https://github.com/JeffersonLab/dtm/blob/main/Dockerfile).

### To run it

| Dependency | Used for | Source | License | Maintenance |
|---|---|---|---|---|
| Java 17 or later | Runs the application server | [Eclipse Temurin](https://adoptium.net/) or another OpenJDK build | GPL-2.0 with Classpath Exception | Active (Eclipse Adoptium) |
| [WildFly](https://www.wildfly.org/) 37.0.1 | Jakarta EE 10 application server; provides the Jakarta EE API, JSTL, and Hibernate ORM with Envers, which keeps the audit history, to the app | [wildfly.org](https://www.wildfly.org/downloads/) | Apache-2.0; the Hibernate ORM 6.6 in it is LGPL-2.1 | Active; the project's latest is 42.0.0 (October 2026) |
| Oracle Database 19 or later | Stores the downtime events | Oracle | Commercial | Supported by Oracle |
| Oracle JDBC driver | WildFly's datasource for the database | [Maven Central](https://central.sonatype.com/artifact/com.oracle.database.jdbc/ojdbc17) | Oracle Free Use Terms and Conditions | Active (Oracle) |
| [Keycloak](https://www.keycloak.org/) | Login with OpenID Connect, and the user directory | [keycloak.org](https://www.keycloak.org/downloads); tested with 26.6 | Apache-2.0 | Active; latest is 26.8.2 (November 2026) |
| [Puppet Show](https://github.com/JeffersonLab/puppet-show) | PDF and image export of pages | [Docker Hub](https://hub.docker.com/r/jeffersonlab/puppet-show) | MIT | JLab's own; last release 3.0.2 (December 2024) |
| SMTP server | Sends email | Any | | |
| [Smoothness weblib](https://github.com/JeffersonLab/smoothness) | Page layout, login, settings and setup pages, and the shared scripts, such as Flot for charts; in the war | [Maven Central](https://central.sonatype.com/artifact/org.jlab/smoothness-weblib) | MIT | JLab's own; active, latest is 5.3.0 (October 2026) |
| [jLog](https://github.com/JeffersonLab/jlog) | Posts incidents to the JLab logbook; installed in WildFly | [Maven Central](https://central.sonatype.com/artifact/org.jlab/jlog) | MIT | JLab's own; last release 5.2.0 (August 2025) |
| [Apache POI](https://poi.apache.org/) | Excel exports of the reports; installed in WildFly | [Maven Central](https://central.sonatype.com/artifact/org.apache.poi/poi) | Apache-2.0 | Active; latest is 6.0.0 (October 2026) |
| [jsoup](https://jsoup.org/) | Reads the app's own email pages to send them; in the war | [Maven Central](https://central.sonatype.com/artifact/org.jsoup/jsoup) | MIT | Active; latest is 1.24.1 (November 2026) |
| [UrlRewriteFilter](https://github.com/paultuckey/urlrewritefilter) | Rewrites versioned resource links and incident addresses; installed in WildFly | [Maven Central](https://central.sonatype.com/artifact/org.tuckey/urlrewritefilter) | BSD | Dormant: 5.1.3 (October 2023) is the latest release, and the repository last changed in February 2024 |
| [jsTree](https://github.com/vakata/jstree) 3.3.8 | The category tree | Copied into [resources/jstree](https://github.com/JeffersonLab/dtm/tree/main/src/main/webapp/resources/jstree) | MIT | Latest is 3.3.17 (September 2024), the repository's last change |
| [Flot](https://github.com/flot/flot) plugins: dashes, growraf, and a changed copy of the time plugin | Dashed lines, animation and time axes in the charts | Copied into [resources/js](https://github.com/JeffersonLab/dtm/tree/main/src/main/webapp/resources/js) | MIT for growraf and time; the dashes file states none | Maintained here: Flot's last release was 0.8.3 (April 2014) |

With their settings on, dtm also posts to the JLab logbook, and reads SRM and the NPES schedule on JLab's servers: see the [Settings](https://github.com/JeffersonLab/dtm/blob/main/container/oracle/initdb.d/04_settings.sql).

### To build and test it

| Dependency | Used for | Source | License | Maintenance |
|---|---|---|---|---|
| [Gradle](https://gradle.org/) 9 | Builds the war | The wrapper in this repository; the `gradle:9-jdk21` image for the Docker build | Apache-2.0 | Active |
| [JUnit](https://junit.org/) 5 | Unit tests | Maven Central | EPL-2.0 | Active; the project's current line is JUnit 6 |
| [Mockito](https://site.mockito.org/) | Stand-ins for collaborators in the unit tests | Maven Central | MIT | Active; latest is 5.24.0 (September 2026) |
| [Eclipse Parsson](https://github.com/eclipse-ee4j/parsson) | JSON-P for the tests; WildFly provides it to the app | Maven Central | EPL-2.0 or GPL-2.0 with Classpath Exception | Active |
| [Spotless](https://github.com/diffplug/spotless) with google-java-format | Checks formatting | Gradle Plugin Portal | Apache-2.0 | Active |
| [gvenzl/oracle-xe](https://github.com/gvenzl/oci-oracle-xe) image | Oracle database for development and tests | Docker Hub | Image scripts Apache-2.0; Oracle Database XE under Oracle's terms | Repository last changed in December 2024 |
| [jeffersonlab/wildfly](https://github.com/JeffersonLab/wildfly) and [jeffersonlab/keycloak](https://github.com/JeffersonLab/keycloak) images, tag `4` | WildFly and Keycloak, set up for development, tests and the demo image | Docker Hub | MIT | JLab's own; active, latest is 4.1.0 (November 2026) |
| [MailHog](https://github.com/mailhog/MailHog) image | Catches email in development and tests | Docker Hub | MIT | Unmaintained: last release 1.0.1 (August 2020) |
| [Zensical](https://zensical.org/) | Builds the user guide website | PyPI, pinned by `uv.lock` | MIT | Active |

## Install
This application requires a Java 17+ JVM and standard library to run, plus a Jakarta EE 10 application server (developed with Wildfly).


1. Install service [dependencies](https://github.com/JeffersonLab/dtm/blob/main/deps.yaml)
2. Download [Wildfly 37.0.1](https://www.wildfly.org/downloads/)
3. [Configure](https://github.com/JeffersonLab/dtm#configure) Wildfly and start it
4. Download [dtm.war](https://github.com/JeffersonLab/dtm/releases) and deploy it to Wildfly
5. Navigate your web browser to [localhost:8080/dtm](http://localhost:8080/dtm)

## Configure

### Configtime
Wildfly must be pre-configured before the first deployment of the app. The [wildfly bash scripts](https://github.com/JeffersonLab/wildfly#configure) can be used to accomplish this. See the [Dockerfile](https://github.com/JeffersonLab/dtm/blob/main/Dockerfile) for an example.

### Runtime
Uses the [Smoothness Environment Variables](https://github.com/JeffersonLab/smoothness#environment-variables) plus the following application specific:

| Name          | Description                                                                                 |
|---------------|---------------------------------------------------------------------------------------------|
| RAR_DIR       | Directory path to store Repair Assessment Reports                                           |

There are some [Settings](https://github.com/JeffersonLab/dtm/blob/main/container/oracle/initdb.d/04_settings.sql) in the database as well that can be edited on the Setup tab by admins.

### Database
This application requires an Oracle 19+ database with the following [schema](https://github.com/JeffersonLab/dtm/tree/main/container/oracle/initdb.d) installed.   The application server hosting this app must also be configured with a JNDI datasource.

## Build
This project is built with [Java 21](https://adoptium.net/) (compiled to Java 17 bytecode), and uses the [Gradle 9](https://gradle.org/) build tool to automatically download dependencies and build the project from source:

```
git clone https://github.com/JeffersonLab/dtm
cd dtm
gradlew build
```
**Note**: If you do not already have Gradle installed, it will be installed automatically by the wrapper script included in the source

**Note for JLab On-Site Users**: Jefferson Lab has an intercepting [proxy](https://gist.github.com/slominskir/92c25a033db93a90184a5994e71d0b78)

See: [Docker Development Quick Reference](https://gist.github.com/slominskir/a7da801e8259f5974c978f9c3091d52c#development-quick-reference)

### User Guide website
The [user guide](https://github.com/JeffersonLab/dtm/tree/main/docs/user-guide) is Markdown, readable on GitHub, and also built into a website with [Zensical](https://zensical.org) ([zensical.toml](zensical.toml)). With [uv](https://docs.astral.sh/uv/getting-started/installation/) installed:
```
uv run --locked zensical build --strict
```
builds it into `site/`, as CI does; `--strict` fails on a link to a missing page or heading. `uv run --locked zensical serve` previews it at http://localhost:8000.

CI builds it and each release publishes it with the [user guide workflows](https://github.com/JeffersonLab/general-workflows/blob/main/docs/user-guide/README.md) of general-workflows.

## Develop
In order to iterate rapidly when making changes it's often useful to run the app directly on the local workstation, perhaps leveraging an IDE.  In this scenario run the service dependencies with:
```
docker compose -f deps.yaml up
```
**Note**: The local install of Wildfly should be [configured](https://github.com/JeffersonLab/dtm#configure) to proxy connections to services via localhost and therefore the environment variables should contain:
```
KEYCLOAK_BACKEND_SERVER_URL=http://localhost:8081
FRONTEND_SERVER_URL=https://localhost:8443
```
Further, the local DataSource must also leverage localhost port forwarding so the `standalone.xml` connection-url field should be: `jdbc:oracle:thin:@//localhost:1521/xepdb1`.  These are the default [ports](#ports); use yours if you set others.

The [server](https://github.com/JeffersonLab/wildfly/blob/main/scripts/server-setup.sh) and [app](https://github.com/JeffersonLab/wildfly/blob/main/scripts/app-setup.sh) setup scripts can be used to setup a local instance of Wildfly. 

## Release
1. Bump the version number in the VERSION file and commit and push to GitHub (using [Semantic Versioning](https://semver.org/)).
2. The [CD](https://github.com/JeffersonLab/dtm/blob/main/.github/workflows/cd.yaml) GitHub Action should run automatically invoking:
    - The [Create release](https://github.com/JeffersonLab/java-workflows/blob/main/.github/workflows/gh-release.yaml) GitHub Action to tag the source and create release notes summarizing any pull requests.   Edit the release notes to add any missing details.  A war file artifact is attached to the release.
    - The [Publish docker image](https://github.com/JeffersonLab/container-workflows/blob/main/.github/workflows/docker-publish.yaml) GitHub Action to create a new demo Docker image.
    - The [Deploy to JLab](https://github.com/JeffersonLab/general-workflows/blob/main/.github/workflows/jlab-deploy-app.yaml) GitHub Action to deploy to the JLab test environment.
    - The [Publish user guide](https://github.com/JeffersonLab/general-workflows/blob/main/.github/workflows/user-guide-publish.yaml) GitHub Action to publish the [User Guide](https://jeffersonlab.github.io/dtm/) to GitHub Pages, in a folder for the release and in `latest/`.

## Deploy
The deploy to JLab's acctest is handled automatically via the release workflow.

At JLab this app is found at [ace.jlab.org/dtm](https://ace.jlab.org/dtm) and internally at [acctest.acc.jlab.org/dtm](https://acctest.acc.jlab.org/dtm).  However, those servers are proxies for `wildfly3.acc.jlab.org` and `wildflytest3.acc.jlab.org` respectively.   A [deploy script](https://github.com/JeffersonLab/wildfly/blob/main/scripts/deploy.sh) is provided on each server to automate wget and deploy.  Example:

```
/opt/wildfly/cd/deploy.sh dtm v1.2.3
```

**JLab Internal Docs**: [RHEL9 Wildfly](https://acgdocs.acc.jlab.org/en/ace/builds/rhel9-wildfly)

## See Also
 - [System Downtime Management at 12 GeV CEBAF](https://misportal.jlab.org/sti/publications/13665), presented at [Accelerator Reliability Workshop 2015](https://neutrons2.ornl.gov/conf/arw2015/)
 - [JLab ACE management-app list](https://github.com/search?q=org%3Ajeffersonlab+topic%3Aace+topic%3Amanagement-app&type=repositories)
