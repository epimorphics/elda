# ELDA Standalone Application

This module contains a standalone ELDA application which runs an embedded Tomcat server.

To build:
* `mvn clean package`

To run:
* `java -jar target/elda-standalone-jar-with-dependencies.jar`

### Sample Web Server

By default, the application loads a web server configuration, including ELDA API specs,
from the `src/main/webapp` directory, which contains a small sample API.
When running with the sample API, check http://localhost:8080/standalone/again/games.ttl to confirm that the
server is running correctly.

Note that the sample API does not support HTML rendering.

### Configuring the Web Server

To run the application with your own web server configuration,
set the `ELDA_WEBAPP_PATH` environment variable to the path to your web app directory.
This directory **must contain** a `web.xml` file which configures the web server.

It must also contain the ELDA API specs and static resources (velocity templates, scripts, stylesheets etc.)
you want to serve.

To run the web server with a context path, set the `ELDA_CONTEXT_PATH` environment variable to the context path.
The default context path is `/standalone`.
To run without a context path set the environment variable to `/`.

To run the web server with an alternate port (the default is 8080), set the `ELDA_PORT` environment variable to the port number.

| Env Var | Description | Default |
|---------|-------------|---------|
| `ELDA_WEBAPP_PATH` | The location of the web app directory. | src/main/webapp |
| `ELDA_CONTEXT_PATH` | The context path to the ELDA API. | /standalone |
| `ELDA_PORT` | The port on which the web server answers requests. | 8080 |

### Docker Image

To build the standalone application as a Docker image, run:
* `docker build -t elda/standalone:test .`

The image does not contain any web server configuration or ELDA API specs,
hence these must be mounted to a running container as a volume.
The default web app directory is `/etc/elda`.

To run the image in a Docker container, run:
* `docker run -p 8080:8080 -v {host webapp dir}:{container webapp dir} -e ELDA_WEBAPP_PATH={container webapp dir} elda/standalone:test`

For example, to run with the sample web server:
* `docker run -p 8080:8080  -v ./src/main/webapp:/etc/elda -e ELDA_WEBAPP_PATH=/etc/elda elda/standalone:test`
