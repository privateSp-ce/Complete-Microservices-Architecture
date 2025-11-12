### Project Logging: ELK Stack Flow 🗺️

Ee diagram simple ga mana flow ni cheptundi:
**[Logback (App)] -\> [Logstash (Collector)] -\> [Elasticsearch (Database)] -\> [Kibana (UI)]**

Ippudu, ee steps ni detail ga chuddam:

1.  **Log Creation (App lo Log Generate Avvadam):**

    * Mana prathi microservice (e.g., `order-service`, `product-service`) Spring Boot app ye.
    * Manam code lo `log.info("User created")` ani raasinappudu, **Logback** (Spring Boot tho vache default logging library) daanini handle chestundi.

2.  **Log Shipping (Log ni Pampinchadam):**

    * Manam prathi service lo `logback-spring.xml` file configure chesam.
    * Ee file Logback ki cheptundi: "Logs ni console lo print cheyyadame kadu, network lo **Logstash** container ki (port `5044` lo) pampinchu" ani.

3.  **Log Aggregation (Logstash Collect Cheyadam):**

    * `Logstash` anedi oka central collector (idi `docker-compose.yml` lo run avtundi).
    * Idi port `5044` lo anni microservices nunchi vache logs ni receive chesukuntundi.
    * Mana `logstash.conf` file prakaram, ee logs ni parse chesi, clean chesi, next step ki pamputundi.

4.  **Log Storage (Elasticsearch lo Store Cheyadam):**

    * `Logstash` process chesina clean logs ni **Elasticsearch** ki pamputundi.
    * `Elasticsearch` oka powerful search database. Idi anni logs ni store chesi, index chestundi (daanivalla search chala fast ga untundi).

5.  **Log Visualization (Kibana lo Chupinchadam):**

    * `Kibana` anedi `Elasticsearch` kosam oka UI (website).
    * Manam Kibana dashboard (`http://localhost:5601`) ki velli, manaki kavalsina logs ni search cheyochu, graphs create cheyochu.

-----

### Logback vs. Logstash (Mukhyamaina Theda) 🤝

Idhi chala important mawa. Ee rendu okadaniki alternative kadu, **kalisi pani chestai (they work together)**.

| Feature | Logback (Mana App loni Library) | Logstash (Separate Server/Container) |
| :--- | :--- | :--- |
| **Purpose (Asala Pani)** | Logging Framework | Log Aggregator & Processor |
| **Where it runs?** | **Inside** your Java application (oka .jar file laaga) | Runs as a **separate, independent service** (oka Docker container laaga) |
| **Responsibility (Badyatha)** | Oka **single** application nunchi logs ni **create** chesi **send** cheyyadam. | **Multiple** sources nunchi logs ni **receive**, **process**, and **forward** cheyyadam. |
| **Analogy (Example)** | `Logback` anedi nee app lo unna **Reporter** ✍️. | `Logstash` anedi **Central News Agency Head Quarters** 🏢. |

**Simple Analogy:** Reporter (`Logback`) news ni collect chesi agency (`Logstash`) ki pampistadu. Agency (`Logstash`) aa reports anni collect chesi, edit chesi, main archive (`Elasticsearch`) lo store chestundi.

-----

### Detailed Journey: Oka Log Line Katha (App to Kibana) 🔬

Oka log line generate ayinappati nunchi Kibana lo kanipinche varaku em jarugutundo chudu:

1.  **Step 1: App Log Generate Chestundi (Logback)**

    * Nee `api-gateway-app` lo `log.info("Handling GET /products")` anagane, nee `logback-spring.xml` file activate avutundi.
    * Adi `LogstashEncoder` ni use chesi, ee log ni oka **JSON object** laaga convert chestundi.
    * **MDC Magic ✨:** `traceId` and `spanId` ee JSON lo ela vachai? Manam `order-service-app/src/main/resources/application.yml` lo `logging.pattern.level: "...%X{traceId:-},%X{spanId:-}]"` ani pettam kabatti, tracing library (Brave) automatic ga aa values ni log lo inject chestundi.
    * **Example JSON Log:**
      ```json
      {
        "@timestamp": "2025-11-12T05:10:23.456Z",
        "level": "INFO",
        "logger": "com.spring_cloud.api_gateway_app.SomeHandler",
        "thread": "reactor-http-epoll-2",
        "message": "Handling GET /products",
        "service_name": "api-gateway-app",
        "traceId": "4f2b9c0f6e9a1a1d",
        "spanId": "8c1a2bd0e1f3a7b4"
      }
      ```
    * Ee complete JSON ni `LogstashTcpSocketAppender` (mana `logback-spring.xml` lo undi) teesukuni `localhost:5044` ki send chestundi.

2.  **Step 2: Logstash Receive & Process Chestundi**

    * Mana `logstash.conf` file lo `input { tcp { port => 5044 codec => json_lines ... } }` ani undi. Idi aa JSON line ni receive chesukuntundi.
    * `output {}` block lo `elasticsearch { ... }` instruction valla, ee JSON ni `Elasticsearch` (running at `http://elasticsearch:9200`) ki pamputundi.
    * **Crucial Point:** Idi log ni `index => "%{service_name}-logs-%{+YYYY.MM.dd}"` format lo save cheyamani cheptundi. Ante, `api-gateway-app-logs-2025.11.12` lanti peru tho index create avutundi.

3.  **Step 3: Elasticsearch Storage & Kibana Display**

    * `Elasticsearch` aa JSON document ni `api-gateway-app-logs-2025.11.12` index lo store chestundi.
    * Ippudu nuvvu `Kibana` (UI) open chesi, "Discover" tab lo ee index ni select cheskuni, ila search cheyochu:
        * `service_name: "api-gateway-app"`
        * `traceId: "4f2b9c0f6e9a1a1d"` (nee specific trace ID ni Zipkin nunchi copy chesi)

-----

### Live Verification (Ela Check Cheyali?) ✅

Ee setup pani chestundo ledo 2 minutes lo check cheyochu:

1.  **Command 1 (Logstash Logs Chudu):**

    * Nee `docker-compose.yml` unna folder lo, ee command run chey:
      ```bash
      docker compose -f infra/compose/docker-compose.yml logs -f logstash
      ```
    * *Nuvvu nee app ni use chestunte, ikkada logs fast ga scroll avvadam nuvvu chustav.*

2.  **Action 2 (API ni Call Chey):**

    * Nee API Gateway endpoint ni call chey (e.g., browser lo `GET http://localhost:9000/products` open chey).

3.  **Click 3 (Kibana lo Chudu):**

    * `Kibana` (`http://localhost:5601`) open chey.
    * Go to **Discover** tab.
    * `api-gateway-app-logs-YYYY.MM.DD` lanti index ni select chesko.
    * Nee kotha logs akkada kanipistai\! Filter `service_name: "api-gateway-app"` petti check chesko.
