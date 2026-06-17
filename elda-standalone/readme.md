# ELDA Standalone Application

This module contains a standalone ELDA application which runs an embedded Tomcat server.

To build:
* `mvn clean package`

To run:
* `java -jar target/elda-standalone-jar-with-dependencies.jar`

### Sample Web Server

By default, the application loads a web server configuration, including ELDA API specs,
from the `src/main/webapp` directory, which contains a small sample API.
When running with the sample API, check http://localhost:8080/again/games.ttl to confirm that the
server is running correctly.

Note that the sample API does not support HTML rendering.

### Configuring the Web Server

To run the application with your own web server configuration,
set the `elda_webapp_path` environment variable to the path to your web app directory.
This directory **must contain** a `web.xml` file which configures the web server.

It must also contain the ELDA API specs and static resources (velocity templates, scripts, stylesheets etc.)
you want to serve.

To run the web server with a context path, set the `elda_context_path` environment variable to the context path.

To run the web server with an alternate port (the default is 8080), set the `elda_port` environment variable to the port number.

### Docker Image

To build the standalone application as a Docker image, run:
* `docker build -t elda/standalone:test .`

The image does not contain any web server configuration or ELDA API specs,
hence these must be mounted to a running container as a volume.

To run the image in a Docker container, run:
* `docker run -p 8080:8080 -v {host webapp dir}:{container webapp dir} -e elda_webapp_path={container webapp dir} elda/standalone:test`

For example, to run with the sample web server:
* `docker run -p 8080:8080  -v ./src/main/webapp:/etc/elda -e elda_webapp_path=/etc/elda elda/standalone:test`
