# Complete Microservices Architecture - Observability & Runbook

# Complete Microservices Architecture - Observability & Runbook

> **Complete documentation for the production-grade observability stack**
> 

> Last Updated: November 2025
> 

---

## 📋 Table of Contents

1. [Architecture Overview](#architecture-overview)
2. [Three Pillars of Observability](#three-pillars)
3. [Infrastructure Setup](#infrastructure-setup)
4. [Service Configuration](#service-configuration)
5. [Using the Stack](#using-the-stack)
6. [Troubleshooting Guide](#troubleshooting)
7. [Runbook - Operations](#runbook)
8. [Advanced Topics](#advanced-topics)

---

## 🏗️ Architecture Overview

### System Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                        USER REQUEST                              │
└────────────────────────────┬────────────────────────────────────┘
                             ↓
                   ┌─────────────────┐
                   │  API Gateway    │ traceId: abc123
                   │  (Port: 9000)   │
                   └────────┬────────┘
                            ↓
          ┌─────────────────┼─────────────────┐
          ↓                 ↓                 ↓
    ┌──────────┐      ┌──────────┐     ┌──────────┐
    │  Order   │      │ Product  │     │ Payment  │
    │ (8083)   │─────→│ (8081)   │     │ (8082)   │
    └─────┬────┘      └──────────┘     └──────────┘
          │
          ↓ Kafka Message
    ┌──────────────┐
    │ Notification │
    │   (8084)     │
    └──────────────┘
```

### Observability Stack Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                     MICROSERVICES                                │
│  (Order, Product, Payment, Notification, API Gateway)           │
└───────┬──────────────────┬──────────────────┬───────────────────┘
        │                  │                  │
        │ Logs             │ Metrics          │ Traces
        ↓                  ↓                  ↓
┌───────────────┐  ┌─────────────┐  ┌──────────────┐
│   Logstash    │  │ Prometheus  │  │   Zipkin     │
│  (Port 5044)  │  │ (Port 9090) │  │ (Port 9411)  │
└───────┬───────┘  └──────┬──────┘  └──────┬───────┘
        │                 │                 │
        ↓                 ↓                 ↓
┌──────────────────────────────────────────────────┐
│            Elasticsearch (Port 9200)              │
│  • Stores logs with traceId                      │
│  • Stores Zipkin traces                          │
│  • Enables correlation by traceId                │
└─────┬────────────────────────────────────┬───────┘
      │                                    │
      ↓                                    ↓
┌─────────────┐                    ┌──────────────┐
│   Kibana    │                    │   Grafana    │
│ (Port 5601) │                    │ (Port 3000)  │
│ Log Search  │                    │  Dashboards  │
└─────────────┘                    └──────────────┘
```

---

## 🎯 Three Pillars of Observability

### 1️⃣ Logs (ELK Stack)

**Purpose:** Understand what happened in your system

**Components:**

- **Elasticsearch** (9200, 9300): Stores and indexes logs
- **Logstash** (5044, 9600): Aggregates and parses logs from all services
- **Kibana** (5601): Visualizes and searches logs

**What We Track:**

- Application logs with traceId & spanId
- Error messages and stack traces
- Business events (order created, payment processed)
- Service-to-service communication logs

**Example Log Entry:**

```json
{
  "@timestamp": "2025-11-13T10:00:01.123Z",
  "level": "INFO",
  "service_name": "order-service-app",
  "traceId": "abc123def456",
  "spanId": "span001",
  "thread": "http-nio-8083-exec-1",
  "logger": "OrderController",
  "message": "Order created successfully"
}
```

---

### 2️⃣ Metrics (Prometheus + Grafana)

**Purpose:** Measure system health and performance

**Components:**

- **Prometheus** (9090): Scrapes and stores metrics from all services
- **Grafana** (3000): Creates dashboards for visualization

**What We Track:**

- **JVM Metrics:** Heap memory, garbage collection, thread count
- **HTTP Metrics:** Request count, duration, error rate
- **System Metrics:** CPU usage, disk I/O
- **Custom Metrics:** Orders per minute, payment success rate

**Example Metrics:**

```
http_server_requests_seconds_count{service="order-service",uri="/orders",status="200"} 1500
http_server_requests_seconds_sum{service="order-service",uri="/orders",status="200"} 45.2
jvm_memory_used_bytes{service="order-service",area="heap"} 524288000
```

---

### 3️⃣ Traces (Zipkin)

**Purpose:** Track request flow across multiple services

**Components:**

- **Zipkin** (9411): UI for viewing distributed traces
- **Elasticsearch**: Backend storage for traces (persistent)
- **Micrometer + Brave**: Auto-instruments services

**What We Track:**

- Request path: API Gateway → Order → Product → Payment
- Time spent in each service
- Network latency between services
- Errors and bottlenecks

**Example Trace:**

```
TraceId: abc123def456

Span 1: API Gateway     [========] 250ms
Span 2: Order Service   [====] 120ms
Span 3: Product Service [==] 80ms
Span 4: Payment Service [===] 100ms

Total: 550ms
```

---

## 🚀 Infrastructure Setup

### Prerequisites

```bash
# Required software
- Docker Desktop (Windows/Mac) or Docker Engine + Docker Compose (Linux)
- Minimum 8GB RAM (16GB recommended)
- 20GB free disk space
- Java 21 JDK
- Maven 3.9+
```

### Step 1: Start Infrastructure

**Navigate to infra directory:**

```bash
cd infra/compose
```

**Start all infrastructure services:**

```bash
docker-compose up -d
```

**This starts 9 containers:**

1. Zookeeper (Kafka coordination)
2. Kafka (Event streaming)
3. Elasticsearch (Log & trace storage)
4. Logstash (Log aggregation)
5. Kibana (Log visualization)
6. Prometheus (Metrics collection)
7. Grafana (Metrics dashboards)
8. Zipkin (Trace visualization)
9. RabbitMQ (Config refresh bus)

**Verify all containers are running:**

```bash
docker-compose ps
```

**Expected output:**

```
NAME                STATUS              PORTS
elasticsearch       Up (healthy)        0.0.0.0:9200->9200/tcp
logstash            Up                  0.0.0.0:5044->5044/tcp
kibana              Up                  0.0.0.0:5601->5601/tcp
prometheus          Up                  0.0.0.0:9090->9090/tcp
grafana             Up                  0.0.0.0:3000->3000/tcp
zipkin              Up                  0.0.0.0:9411->9411/tcp
kafka               Up                  0.0.0.0:9092->9092/tcp
zookeeper           Up                  0.0.0.0:2181->2181/tcp
rabbitmq            Up                  0.0.0.0:5672->5672/tcp
```

**Wait for Elasticsearch health check (40 seconds):**

```bash
curl [http://elasticsearch:9200](http://elasticsearch:9200)
```

---

### Step 2: Verify Infrastructure Components

**Check Elasticsearch:**

```bash
curl [http://elasticsearch:9200](http://elasticsearch:9200)
# Should return cluster info with status "green" or "yellow"
```

**Check Kibana:**

```bash
Open browser: toolu_01RxZidi8Gs1Nvv8WBqQdwmD
# Should show Kibana home page
```

**Check Prometheus:**

```bash
Open browser: [https://github.com/privateSp-ce/Complete-Microservices-Architecture/blob/main/services/configserver/src/main/java/com/spring_cloud/configserver/ConfigserverApplication.java](https://github.com/privateSp-ce/Complete-Microservices-Architecture/blob/main/services/configserver/src/main/java/com/spring_cloud/configserver/ConfigserverApplication.java)
# Go to Status → Targets to see service scraping status
```

**Check Grafana:**

```bash
Open browser: toolu_01X7j4Knhfko3kpMFWwAVdHv
# Login: admin / admin
# Should see pre-configured dashboards
```

**Check Zipkin:**

```bash
Open browser: [http://localhost:9411](http://localhost:9411)
# Should show Zipkin UI (will be empty until services run)
```

---

## ⚙️ Service Configuration

### Order Service Configuration (Reference)

**File:** `order-service-app/src/main/resources/application.yml`

```yaml
spring:
  application:
    name: order-service-app

management:
  # Zipkin endpoint configuration
  zipkin:
    tracing:
      endpoint: [http://localhost:9411/api/v2/spans](http://localhost:9411/api/v2/spans)
  
  # Tracing settings
  tracing:
    sampling:
      probability: 1.0  # 100% sampling (use 0.1 in production)
    propagation:
      type: W3C
      produce: W3C
      consume: [W3C, B3]  # Support both formats
  
  # Prometheus metrics
  prometheus:
    metrics:
      export:
        enabled: true
  
  # Actuator endpoints
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus

# Logging with traceId pattern
logging:
  pattern:
    level: "%5p [${[spring.application.name](http://spring.application.name):},%X{traceId:-},%X{spanId:-}]"
```

**File:** `order-service-app/src/main/resources/logback-spring.xml`

```xml
<configuration>
    <!-- Console appender for local development -->
    <include resource="org/springframework/boot/logging/logback/console-appender.xml"/>

    <!-- Logstash appender for centralized logging -->
    <appender name="LOGSTASH" class="net.logstash.logback.appender.LogstashTcpSocketAppender">
        <destination>[localhost:5044](http://localhost:5044)</destination>
        <encoder class="net.logstash.logback.encoder.LogstashEncoder">
            <customFields>{"service_name":"order-service-app"}</customFields>
        </encoder>
    </appender>

    <!-- Send logs to both console and Logstash -->
    <root level="INFO">
        <appender-ref ref="CONSOLE"/>
        <appender-ref ref="LOGSTASH"/>
    </root>
</configuration>
```

### Configuration Checklist for All Services

**Each service needs:**

- ✅ `micrometer-tracing-bridge-brave` dependency
- ✅ `zipkin-reporter-brave` dependency
- ✅ `spring-boot-starter-actuator` dependency
- ✅ `logstash-logback-encoder` dependency
- ✅ `micrometer-registry-prometheus` dependency
- ✅ `application.yml` with Zipkin endpoint
- ✅ `logback-spring.xml` with Logstash appender

---

## 📊 Using the Observability Stack

### Scenario: Debugging a Slow Request

**Step 1: Identify the Issue in Zipkin**

1. Open Zipkin: [http://localhost:9411](http://localhost:9411)
2. Click "Run Query" to see recent traces
3. Look for traces with long duration (e.g., > 1 second)
4. Click on the slow trace
5. Note the `traceId` (e.g., `abc123def456`)

**Visual in Zipkin:**

```
Trace: abc123def456 (Total: 1250ms)

[API Gateway]        [==========] 250ms
  ↓
[Order Service]      [==================] 800ms  ← SLOW!
  ↓
[Product Service]    [====] 150ms
  ↓
[Payment Service]    [====] 50ms
```

**Step 2: View Metrics in Grafana**

1. Open Grafana: toolu_01X7j4Knhfko3kpMFWwAVdHv
2. Go to "Spring Boot 3 Dashboard"
3. Select service: `order-service-app`
4. Time range: Last 15 minutes
5. Look at:
    - HTTP request duration (P95, P99)
    - Error rate
    - JVM heap usage
    - Thread count

**What to look for:**

- High P99 latency → Performance issue
- Increasing heap usage → Memory leak
- High thread count → Thread pool exhaustion

**Step 3: Analyze Logs in Kibana**

1. Open Kibana: toolu_01RxZidi8Gs1Nvv8WBqQdwmD
2. Go to "Discover"
3. Search: `traceId:"abc123def456"`
4. See ALL logs from ALL services for this request
5. Filter by `level:ERROR` to find errors

**Example Query:**

```
traceId:"abc123def456" AND service_name:"order-service-app"
```

**Logs show:**

```
10:00:01.000 INFO  [order-service-app,abc123,span001] Order received
10:00:01.050 INFO  [order-service-app,abc123,span001] Validating order
10:00:01.100 ERROR [order-service-app,abc123,span001] Database connection timeout!
10:00:01.800 INFO  [order-service-app,abc123,span001] Retry successful
```

**Root Cause Found:** Database connection timeout causing 700ms delay!

---

### Scenario: Monitoring System Health

**Daily Health Check Routine:**

**1. Check Grafana Dashboard (5 minutes)**

```
Open: toolu_01X7j4Knhfko3kpMFWwAVdHv
Dashboard: "Spring Boot 3 Overview"

Look for:
- All services: Response time < 500ms
- Error rate: < 1%
- JVM heap: < 80% used
- HTTP 5xx errors: 0
```

**2. Check Prometheus Targets (2 minutes)**

```
Open: [https://github.com/privateSp-ce/Complete-Microservices-Architecture/blob/main/services/configserver/src/main/java/com/spring_cloud/configserver/ConfigserverApplication.java/targets](https://github.com/privateSp-ce/Complete-Microservices-Architecture/blob/main/services/configserver/src/main/java/com/spring_cloud/configserver/ConfigserverApplication.java/targets)

Verify:
- All 7 services: UP and green
- Last scrape: < 30 seconds ago
- Scrape duration: < 1 second
```

**3. Check Kibana for Errors (5 minutes)**

```
Open: toolu_01RxZidi8Gs1Nvv8WBqQdwmD/discover

Search: level:ERROR AND @timestamp:[now-1h TO now]

Review:
- Any critical errors in last hour?
- Error rate trending up?
- Same error repeating?
```

**4. Sample Zipkin Traces (3 minutes)**

```
Open: [http://localhost:9411](http://localhost:9411)

Check:
- Recent traces loading?
- Average request time?
- Any failed traces (red)?
```

---

### Scenario: Tracking a User Request

**User reports:** "My order failed but I got charged!"

**Step 1: Find the Request**

```
Kibana Search:
message:"order" AND message:"failed" AND @timestamp:[now-1h TO now]

Find log:
"Order creation failed for userId=user123, orderId=ORD-500"
traceId: "xyz789abc"
```

**Step 2: View Complete Flow in Zipkin**

```
Zipkin:
Search by traceId: xyz789abc

Trace shows:
✅ API Gateway → 200 OK
✅ Order Service → 200 OK
✅ Product Service → 200 OK
❌ Payment Service → 500 ERROR
❌ Notification Service → Not reached
```

**Step 3: Check Payment Service Logs**

```
Kibana:
traceId:"xyz789abc" AND service_name:"payment-service-app"

Logs show:
"Payment processed successfully" ← Payment actually worked!
"Order service callback failed - timeout" ← Order service didn't get response
```

**Root Cause:** Network timeout between Payment and Order services. Payment succeeded but Order service thought it failed.

**Fix:** Increase timeout + Add retry logic + Idempotency check

---

## 🔧 Troubleshooting Guide

### Common Issues

### Issue 1: Logs Not Appearing in Kibana

**Symptoms:**

- Services running
- Kibana accessible
- No logs in Kibana

**Diagnosis:**

```bash
# Check if Logstash is receiving logs
docker logs logstash --tail 50

# Should see: "message" => "Your log message"
```

**Solutions:**

**A. Logstash not receiving logs:**

```bash
# Test connection from service
telnet [localhost](http://localhost) 5044
# If fails, check firewall/network
```

**B. Grok pattern not matching:**

```bash
# Check Logstash logs for grok errors
docker logs logstash | grep "grok"

# Test pattern at: 533
```

**C. Elasticsearch not storing:**

```bash
# Check ES indices
curl [http://elasticsearch:9200/_cat/indices?v](http://elasticsearch:9200/_cat/indices?v)

# Should see indices like: order-service-logs-2025.11.13
```

**D. Kibana index pattern missing:**

```
1. Go to Kibana → Stack Management → Index Patterns
2. Create index pattern: *-logs-*
3. Time field: @timestamp
4. Refresh field list
```

---

### Issue 2: Traces Not Showing in Zipkin

**Symptoms:**

- Services running with tracing dependencies
- Zipkin UI loads
- No traces visible

**Diagnosis:**

```bash
# Check if Zipkin is receiving data
curl 534/health

# Check Elasticsearch for Zipkin indices
curl [http://elasticsearch:9200/_cat/indices?v](http://elasticsearch:9200/_cat/indices?v) | grep zipkin
```

**Solutions:**

**A. Zipkin endpoint not configured:**

```yaml
# Add to application.yml
management:
  zipkin:
    tracing:
      endpoint: [http://localhost:9411/api/v2/spans](http://localhost:9411/api/v2/spans)
```

**B. Services not sending traces:**

```bash
# Check service logs for tracing errors
# Look for: "Failed to send spans to Zipkin"

# Verify traceId in logs
grep "traceId" order-service.log
```

**C. Sampling rate too low:**

```yaml
# Set to 100% for testing
management:
  tracing:
    sampling:
      probability: 1.0  # Was 0.1 (10%)
```

**D. Elasticsearch storage issue:**

```bash
# Check Zipkin logs
docker logs zipkin

# Look for ES connection errors
# Verify ES_HOSTS environment variable
```

---

### Issue 3: Prometheus Not Scraping Metrics

**Symptoms:**

- Prometheus UI shows targets as DOWN
- Grafana dashboards empty

**Diagnosis:**

```bash
# Check Prometheus targets
Open: [https://github.com/privateSp-ce/Complete-Microservices-Architecture/blob/main/services/configserver/src/main/java/com/spring_cloud/configserver/ConfigserverApplication.java/targets](https://github.com/privateSp-ce/Complete-Microservices-Architecture/blob/main/services/configserver/src/main/java/com/spring_cloud/configserver/ConfigserverApplication.java/targets)

# Red targets indicate scraping failure
```

**Solutions:**

**A. Service not exposing /actuator/prometheus:**

```bash
# Test endpoint directly
curl 535

# Should return metrics in Prometheus format
```

**B. Actuator endpoint not exposed:**

```yaml
# Add to application.yml
management:
  endpoints:
    web:
      exposure:
        include: prometheus,health,info,metrics
```

**C. Prometheus can't reach service:**

```bash
# From Prometheus container
docker exec prometheus curl 536

# Should return metrics
# If fails, check host.docker.internal resolution
```

**D. Prometheus config error:**

```bash
# Validate prometheus.yml
docker exec prometheus promtool check config /etc/prometheus/prometheus.yml

# Reload config
curl -X POST 537
```

---

### Issue 4: TraceId Not Propagating Across Services

**Symptoms:**

- Each service has different traceId
- Can't correlate logs across services

**Diagnosis:**

```bash
# Check logs from API Gateway and downstream service
# TraceIds should match

grep "traceId" api-gateway.log | tail -1
grep "traceId" order-service.log | tail -1
```

**Solutions:**

**A. Feign client not instrumented:**

```xml
<!-- Add to pom.xml -->
<dependency>
    <groupId>io.github.openfeign</groupId>
    <artifactId>feign-micrometer</artifactId>
</dependency>
```

**B. Propagation format mismatch:**

```yaml
# Ensure all services use same formats
management:
  tracing:
    propagation:
      produce: W3C
      consume: [W3C, B3]  # Accept both
```

**C. RestTemplate not using builder:**

```java
// Wrong
@Bean
public RestTemplate restTemplate() {
    return new RestTemplate();  // Not instrumented!
}

// Correct
@Bean
@LoadBalanced
public RestTemplate restTemplate(RestTemplateBuilder builder) {
    return [builder.build](http://builder.build)();  // Auto-instrumented!
}
```

**D. Async execution losing context:**

```java
// Add TaskDecorator for async methods
@Bean
public ThreadPoolTaskExecutor taskExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setTaskDecorator(runnable -> 
        ContextSnapshot.captureAll().wrap(runnable)
    );
    return executor;
}
```

---

### Issue 5: Kafka Messages Not Traced

**Symptoms:**

- TraceId lost after Kafka message
- Notification service shows different traceId

**Solutions:**

**A. Missing tracing dependency:**

```xml
<!-- notification-service-app/pom.xml -->
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-tracing-bridge-brave</artifactId>
</dependency>
```

**B. Kafka not propagating headers:**

```yaml
# Ensure Spring Boot 3.x auto-configuration active
# No manual configuration needed with:
# - spring-kafka dependency
# - micrometer-tracing-bridge-brave dependency
```

**C. Consumer not extracting traceId:**

```java
// Spring Boot 3.x does this automatically
// Just ensure @KafkaListener has tracing dependencies

@KafkaListener(topics = "orders")
public void consume(OrderEvent event) {
    // TraceId automatically available in MDC
    [log.info](http://log.info)("Processing order");  // Contains traceId!
}
```

---

## 📖 Runbook - Operations Guide

### Daily Operations

### Morning Startup Routine (10 minutes)

**1. Start Infrastructure (if not running)**

```bash
cd infra/compose
docker-compose up -d

# Wait for health checks
sleep 60

# Verify all containers
docker-compose ps | grep "Up"
```

**2. Start Microservices (in order)**

```bash
# Terminal 1: Config Server
cd services/configserver
mvn spring-boot:run

# Wait for: "Started ConfigserverApplication"

# Terminal 2: Eureka Server
cd services/eureka-server-app
mvn spring-boot:run

# Wait for: "Started EurekaServerAppApplication"

# Terminal 3-6: Business Services (parallel)
cd services/product-service-app && mvn spring-boot:run &
cd services/payment-service-app && mvn spring-boot:run &
cd services/order-service-app && mvn spring-boot:run &
cd services/notification-service-app && mvn spring-boot:run &

# Terminal 7: API Gateway (last)
cd services/api-gateway-app
mvn spring-boot:run
```

**3. Health Check Dashboard**

```bash
# Check all services registered
Open: [http://localhost:8761/eureka/](http://localhost:8761/eureka/)
# Should see 7 applications

# Check Prometheus targets
Open: [https://github.com/privateSp-ce/Complete-Microservices-Architecture/blob/main/services/configserver/src/main/java/com/spring_cloud/configserver/ConfigserverApplication.java/targets](https://github.com/privateSp-ce/Complete-Microservices-Architecture/blob/main/services/configserver/src/main/java/com/spring_cloud/configserver/ConfigserverApplication.java/targets)
# All should be UP

# Check Grafana
Open: toolu_01X7j4Knhfko3kpMFWwAVdHv
# Dashboards should show data
```

**4. Smoke Test**

```bash
# Create test order
curl -X POST 538 \
  -H "Content-Type: application/json" \
  -d '{
    "productId": "PROD-001",
    "quantity": 1,
    "customerId": "test-user"
  }'

# Check in Zipkin (after 10 seconds)
# Should see complete trace
```

---

### Evening Shutdown Routine (5 minutes)

**1. Graceful Service Shutdown**

```bash
# Stop services in reverse order
# Ctrl+C in each terminal

# Or kill all Java processes
pkill -f "spring-boot:run"
```

**2. Stop Infrastructure (optional - for overnight)**

```bash
cd infra/compose
docker-compose stop

# Or full cleanup (removes data!)
docker-compose down -v
```

---

### Maintenance Tasks

### Weekly: Clean Up Old Data

**Elasticsearch Indices:**

```bash
# List indices older than 7 days
curl [http://elasticsearch:9200/_cat/indices?v](http://elasticsearch:9200/_cat/indices?v) | grep "$(date -d '7 days ago' +%Y.%m.%d)"

# Delete old indices
curl -X DELETE [http://elasticsearch:9200/order-service-logs-2025.11.01](http://elasticsearch:9200/order-service-logs-2025.11.01)
curl -X DELETE [http://elasticsearch:9200/product-service-logs-2025.11.01](http://elasticsearch:9200/product-service-logs-2025.11.01)

# Automated cleanup script
curl -X DELETE "[http://elasticsearch:9200/*-logs-$(date](http://elasticsearch:9200/*-logs-$(date) -d '7 days ago' +%Y.%m.%d)"
```

**Zipkin Traces:**

```bash
# Zipkin auto-deletes old traces (default: 7 days)
# Check current storage
curl 534/api/v2/traces?limit=1
```

---

### Monthly: Optimize Elasticsearch

**Force Merge Segments:**

```bash
# Reduce segment count for better performance
curl -X POST "[http://elasticsearch:9200/_forcemerge?max_num_segments=1](http://elasticsearch:9200/_forcemerge?max_num_segments=1)"
```

**Reindex Old Data:**

```bash
# Create new index with better settings
curl -X PUT [http://elasticsearch:9200/logs-optimized-2025.11](http://elasticsearch:9200/logs-optimized-2025.11) -H 'Content-Type: application/json' -d'
{
  "settings": {
    "number_of_shards": 1,
    "number_of_replicas": 0
  }
}'

# Reindex from old
curl -X POST [http://elasticsearch:9200/_reindex](http://elasticsearch:9200/_reindex) -H 'Content-Type: application/json' -d'
{
  "source": {"index": "*-logs-2025.10*"},
  "dest": {"index": "logs-optimized-2025.11"}
}'
```

---

### Emergency Procedures

### Scenario: Elasticsearch Out of Disk Space

**Symptoms:**

- Kibana shows "No results"
- Elasticsearch logs: "disk usage exceeded"

**Immediate Action:**

```bash
# 1. Check disk usage
docker exec elasticsearch df -h

# 2. Delete oldest indices
curl -X DELETE "[http://elasticsearch:9200/*-logs-$(date](http://elasticsearch:9200/*-logs-$(date) -d '30 days ago' +%Y.%m)*"

# 3. Force merge remaining
curl -X POST "[http://elasticsearch:9200/_forcemerge?max_num_segments=1](http://elasticsearch:9200/_forcemerge?max_num_segments=1)"

# 4. Clear cache
curl -X POST [http://elasticsearch:9200/_cache/clear](http://elasticsearch:9200/_cache/clear)
```

**Prevention:**

- Monitor disk usage in Grafana
- Set up automated cleanup
- Increase disk size if needed

---

### Scenario: High Memory Usage (OOM)

**Symptoms:**

- Service crashes with "OutOfMemoryError"
- Container restarts frequently

**Immediate Action:**

```bash
# 1. Check memory usage
docker stats

# 2. Restart affected service with more memory
docker-compose stop order-service
# Edit docker-compose.yml:
#   environment:
#     - JAVA_OPTS=-Xmx2g -Xms512m
docker-compose up -d order-service

# 3. Check heap dump
cd services/order-service-app
jcmd <PID> GC.heap_dump heap-dump.hprof

# 4. Analyze with VisualVM or Eclipse MAT
```

**Root Cause Analysis:**

```bash
# Check Grafana: JVM Memory dashboard
# Look for:
# - Heap usage constantly at 100%
# - Frequent GC cycles
# - Memory leak pattern (saw-tooth graph)
```

---

### Scenario: Distributed Trace Not Complete

**Symptoms:**

- Zipkin shows partial trace
- Some services missing from trace

**Diagnosis:**

```bash
# 1. Check logs for all services
for service in order product payment notification; do
  echo "=== $service ==="
  grep "traceId" $service-service.log | tail -1
done

# 2. Verify traceId matches
# 3. Check which service breaks the chain
```

**Fix:**

```bash
# Service not propagating traceId
# Check dependencies:
mvn dependency:tree | grep micrometer

# Should see:
# io.micrometer:micrometer-tracing-bridge-brave

# If missing, add to pom.xml and restart
```

---

### Performance Tuning

### Optimize Logstash Pipeline

**Current Config:** Single pipeline processing all logs

**Optimized Config:**

```
# infra/observability/logs/logstash/pipeline/logstash.conf

input {
  tcp {
    port => 5044
    codec => json_lines
    host => "0.0.0.0"
    # Add multiple worker threads
    workers => 4
  }
}

filter {
  # ... existing grok pattern ...
  
  # Add conditionals to skip unnecessary processing
  if [level] == "DEBUG" {
    drop {}
  }
}

output {
  elasticsearch {
    hosts => ["[http://elasticsearch:9200](http://elasticsearch:9200)"]
    index => "%{service_name}-logs-%{+[YYYY.MM](http://YYYY.MM).dd}"
    # Bulk indexing for better performance
    flush_size => 500
    idle_flush_time => 1
  }
}
```

---

### Optimize Prometheus Scraping

**Reduce Scrape Interval for Non-Critical Services:**

```yaml
# prometheus.yml
scrape_configs:
  # Critical services: 15s
  - job_name: 'order-service'
    scrape_interval: 15s
    static_configs:
      - targets: ['host.docker.internal:8083']
  
  # Non-critical: 60s
  - job_name: 'config-server'
    scrape_interval: 60s
    static_configs:
      - targets: ['host.docker.internal:8888']
```

---

### Optimize Zipkin Sampling (Production)

**Development:** 100% sampling

```yaml
management:
  tracing:
    sampling:
      probability: 1.0
```

**Production:** Adaptive sampling

```yaml
management:
  tracing:
    sampling:
      probability: 0.1  # 10% of requests
      # Or use rate-limiting:
      rate: 10  # Max 10 traces per second
```

---

## 🎓 Advanced Topics

### Custom Metrics

**Example: Track Order Processing Time**

```java
@RestController
public class OrderController {
    
    private final MeterRegistry meterRegistry;
    private final Counter ordersCreated;
    private final Timer orderProcessingTime;
    
    public OrderController(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        
        // Counter: Total orders
        this.ordersCreated = Counter.builder("orders.created")
            .description("Total orders created")
            .tag("service", "order-service")
            .register(meterRegistry);
        
        // Timer: Processing duration
        this.orderProcessingTime = Timer.builder("orders.processing.time")
            .description("Time to process an order")
            .tag("service", "order-service")
            .register(meterRegistry);
    }
    
    @PostMapping("/orders")
    public ResponseEntity<Order> createOrder(@RequestBody OrderRequest request) {
        return orderProcessingTime.record(() -> {
            Order order = orderService.createOrder(request);
            ordersCreated.increment();
            return ResponseEntity.ok(order);
        });
    }
}
```

**Query in Prometheus:**

```
# Orders created per minute
rate(orders_created_total[1m])

# Average processing time
rate(orders_processing_time_seconds_sum[5m]) / rate(orders_processing_time_seconds_count[5m])

# P95 processing time
histogram_quantile(0.95, rate(orders_processing_time_seconds_bucket[5m]))
```

---

### Custom Spans in Traces

**Example: Track Database Query Time**

```java
@Service
public class OrderService {
    
    private final Tracer tracer;
    
    public OrderService(Tracer tracer) {
        this.tracer = tracer;
    }
    
    public Order createOrder(OrderRequest request) {
        // Create custom span
        Span span = tracer.nextSpan().name("database.insert.order");
        
        try (Tracer.SpanInScope ws = tracer.withSpan(span.start())) {
            // Add tags
            span.tag("db.table", "orders");
            span.tag("[order.id](http://order.id)", request.getId());
            
            // Your database logic
            Order order = [orderRepository.save](http://orderRepository.save)(request);
            
            span.tag("db.rows_affected", "1");
            return order;
            
        } catch (Exception e) {
            span.tag("error", "true");
            span.tag("error.message", e.getMessage());
            throw e;
        } finally {
            span.end();
        }
    }
}
```

**View in Zipkin:**

```
Trace: abc123

[Order Service]           [==========] 500ms
  ↓
  [database.insert.order] [======] 300ms  ← Your custom span!
  Tags:
    - db.table: orders
    - [order.id](http://order.id): ORD-123
    - db.rows_affected: 1
```

---

### Alerting with Prometheus

**Create Alert Rules:**

```yaml
# prometheus/alert-rules.yml
groups:
  - name: service_alerts
    interval: 30s
    rules:
      # High error rate
      - alert: HighErrorRate
        expr: |
          rate(http_server_requests_seconds_count{status=~"5.."}[5m]) > 0.05
        for: 5m
        labels:
          severity: critical
        annotations:
          summary: "High error rate on  $labels.service "
          description: "Error rate is  $value  (threshold: 0.05)"
      
      # High response time
      - alert: HighResponseTime
        expr: |
          histogram_quantile(0.95, 
            rate(http_server_requests_seconds_bucket[5m])
          ) > 1
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "Slow responses on  $labels.service "
          description: "P95 latency is  $value s (threshold: 1s)"
      
      # Service down
      - alert: ServiceDown
        expr: up == 0
        for: 2m
        labels:
          severity: critical
        annotations:
          summary: "Service  $labels.job  is down"
```

**Configure Alertmanager:**

```yaml
# alertmanager.yml
route:
  receiver: 'slack'
  group_by: ['alertname', 'service']
  group_wait: 30s
  group_interval: 5m
  repeat_interval: 3h

receivers:
  - name: 'slack'
    slack_configs:
      - api_url: '539'
        channel: '#alerts'
        title: ' .GroupLabels.alertname '
        text: ' range .Alerts  .Annotations.description  end '
```

---

### Log Correlation Queries

**Kibana Advanced Queries:**

**1. Find all logs for a user's journey:**

```
# Assuming you add userId to MDC
userId:"user123" AND @timestamp:[now-1h TO now]
```

**2. Find errors with context (5 logs before and after):**

```
# Use "Surrounding documents" feature in Kibana
level:ERROR AND service_name:"order-service-app"
```

**3. Correlate across services:**

```
# Get traceId from order service error
traceId:"abc123" AND (service_name:"order-service-app" OR service_name:"payment-service-app")
```

**4. Find slow requests:**

```
# Add custom field in logs
duration:>1000 AND uri:"/orders"
```

---

### Production Recommendations

### Resource Limits

**Elasticsearch:**

```yaml
services:
  elasticsearch:
    environment:
      - ES_JAVA_OPTS=-Xms2g -Xmx2g  # Increase for production
    deploy:
      resources:
        limits:
          memory: 4g
```

**Logstash:**

```yaml
services:
  logstash:
    environment:
      - LS_JAVA_OPTS=-Xms512m -Xmx1g
    deploy:
      resources:
        limits:
          memory: 2g
```

### Security

**Enable Elasticsearch Security:**

```yaml
services:
  elasticsearch:
    environment:
      - [xpack.security](http://xpack.security).enabled=true
      - ELASTIC_PASSWORD=your_strong_password
```

**Secure Grafana:**

```yaml
services:
  grafana:
    environment:
      - GF_SECURITY_ADMIN_PASSWORD=${GRAFANA_PASSWORD}
      - GF_USERS_ALLOW_SIGN_UP=false
      - GF_AUTH_ANONYMOUS_ENABLED=false
```

**TLS for Zipkin:**

```yaml
services:
  zipkin:
    environment:
      - ARMERIA_SSL_ENABLED=true
      - ARMERIA_SSL_KEY_STORE=/path/to/keystore.p12
```

---

## 📚 Quick Reference

### URLs

| Service | URL | Credentials |
| --- | --- | --- |
| Kibana | toolu_01RxZidi8Gs1Nvv8WBqQdwmD | None |
| Grafana | toolu_01X7j4Knhfko3kpMFWwAVdHv | admin/admin |
| Prometheus | [https://github.com/privateSp-ce/Complete-Microservices-Architecture/blob/main/services/configserver/src/main/java/com/spring_cloud/configserver/ConfigserverApplication.java](https://github.com/privateSp-ce/Complete-Microservices-Architecture/blob/main/services/configserver/src/main/java/com/spring_cloud/configserver/ConfigserverApplication.java) | None |
| Zipkin | [http://localhost:9411](http://localhost:9411) | None |
| Elasticsearch | [http://elasticsearch:9200](http://elasticsearch:9200) | None |
| RabbitMQ UI | 540 | admin/admin |
| Eureka | [http://localhost:8761/eureka/](http://localhost:8761/eureka/) | None |
| API Gateway | 541 | None |

### Ports

| Service | Port | Purpose |
| --- | --- | --- |
| API Gateway | 9000 | Entry point |
| Order Service | 8083 | Orders |
| Product Service | 8081 | Products |
| Payment Service | 8082 | Payments |
| Notification | 8084 | Notifications |
| Config Server | 8888 | Configuration |
| Eureka | 8761 | Service registry |
| Elasticsearch | 9200 | REST API |
| Elasticsearch | 9300 | Cluster communication |
| Logstash | 5044 | Log ingestion |
| Kibana | 5601 | Web UI |
| Prometheus | 9090 | Metrics UI |
| Grafana | 3000 | Dashboards |
| Zipkin | 9411 | Traces UI |
| Kafka | 9092 | Messaging |
| RabbitMQ | 5672 | AMQP |
| RabbitMQ UI | 15672 | Management |

### Commands Cheat Sheet

```bash
# Start infrastructure
cd infra/compose && docker-compose up -d

# Check status
docker-compose ps

# View logs
docker logs -f <container_name>

# Stop infrastructure
docker-compose stop

# Clean up (⚠️ removes data)
docker-compose down -v

# Restart single service
docker-compose restart zipkin

# Check ES health
curl [http://elasticsearch:9200](http://elasticsearch:9200)

# List ES indices
curl [http://elasticsearch:9200/_cat/indices?v](http://elasticsearch:9200/_cat/indices?v)

# Delete old indices
curl -X DELETE [http://elasticsearch:9200/order-service-logs-2025.11.01](http://elasticsearch:9200/order-service-logs-2025.11.01)

# Check Prometheus targets
curl [https://github.com/privateSp-ce/Complete-Microservices-Architecture/blob/main/services/configserver/src/main/java/com/spring_cloud/configserver/ConfigserverApplication.java/api/v1/targets](https://github.com/privateSp-ce/Complete-Microservices-Architecture/blob/main/services/configserver/src/main/java/com/spring_cloud/configserver/ConfigserverApplication.java/api/v1/targets)

# Reload Prometheus config
curl -X POST 537

# Export Grafana dashboard
curl [http://prometheus:9090/api/dashboards/db/spring-boot-3](http://prometheus:9090/api/dashboards/db/spring-boot-3)
```

---

## 🎯 Success Metrics

**Your observability stack is working when:**

✅ **Logs:**

- All services sending logs to Kibana
- Can search by traceId and see full request flow
- Logs appear within 5 seconds of generation

✅ **Metrics:**

- Prometheus scraping all 7 services
- Grafana dashboards showing live data
- Can see request rate, latency, errors

✅ **Traces:**

- Zipkin showing complete traces across services
- Can identify slow services in trace timeline
- TraceId propagates through Kafka messages

✅ **Correlation:**

- Same traceId in logs, metrics tags, and traces
- Can start from any pillar and find related data
- End-to-end visibility of user requests

---

**📞 Support:** For issues, check docs/[OBSERVABILITY.md](http://OBSERVABILITY.md) and Troubleshooting section

**🔄 Updates:** Last updated November 2025 | Spring Boot 3.5.6 | Spring Cloud 2025.0.0

**⭐ Best Practices:**

- Always correlate by traceId
- Use all three pillars together
- Monitor dashboards daily
- Clean up old data weekly
- Test trace propagation after code changes

---

**Built with ❤️ for Complete-Microservices-Architecture**