# Prometheus Metrics

ELDA supports simple metrics for system monitoring through Prometheus.
To enable metrics, add the following to your application's `web.xml` server configuration file:

```xml
<listener>
    <listener-class>com.epimorphics.lda.metrics.PrometheusServletContextListener</listener-class>
</listener>
```

Metrics are served over by the `/metrics` endpoint.
The following metrics are provided by default:

| Metric             | Type              | Description                                                                                        |
|--------------------|-------------------|----------------------------------------------------------------------------------------------------|
| `http_request_duration` | Timer (Histogram) | The time taken by each request.                                                                    |
| `http_request_status`   | Counter           | The number of requests completed, tagged by their status code classification (`2xx`, `4xx`, `5xx`) |

The `http_request_status` counter has a tag, `status` with the following values:
* `2xx` - Counts successful requests.
* `4xx` - Counts requests which were rejected due to a client error.
* `5xx` - Counts requests which failed due to an internal error.

### JVM Metrics

ELDA can also emit metrics that describe the state of the JVM, including memory usage, garbage collection, and threads.
To enable this feature, add the following to your `web.xml`:

```xml
<context-param>
    <param-name>enableJvmMetrics</param-name>
    <param-value>true</param-value>
</context-param>
```