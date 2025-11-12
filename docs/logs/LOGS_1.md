Logs "automatic" ga kadu, manam **"configure"** cheyadam valla ala jarugutundi. 

### Logback Deep Dive: Appenders (ఒకేసారి Console & Logstash కి ఎలా?)

Nee clarification prakaram, Logback rendu panulu (console lo print cheyadam + network lo pampinchadam) okate saari (**simultaneously**) cheyagaladu. Ee magic `Appenders` ane concept lo undi.

* **Appender ante enti?** Simple ga cheppali ante, adi log ki **destination** (gamyasthanam). "Ee log ni ekkadiki pampali?" anedi Appender ye decide chestundi.

* **Multiple Appenders:** Manam Logback ki okate saari enni destinations (Appenders) ayina cheppavachu.

    * `ConsoleAppender`: Deeni pani logs ni console (mana terminal) lo print cheyadam.
    * `LogstashTcpSocketAppender`: Deeni pani logs ni network lo (TCP socket) Logstash server ki pampinchadam.

-----

### Configuration (The Real Magic) ✨

Ee multiple destinations anedi mana `logback-spring.xml` file lo unna configuration valla sadhyam avutundi.

Nuvvu ichina example perfect ga suit avutundi:

```xml
<configuration>

  <appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender">
    <encoder>
      <pattern>%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
    </encoder>
  </appender>

  <appender name="LOGSTASH" class="net.logstash.logback.appender.LogstashTcpSocketAppender">
    <destination>logstash:5044</destination> <encoder class="net.logstash.logback.encoder.LogstashEncoder"/>
  </appender>

  <root level="INFO">
    <appender-ref ref="STDOUT" />
    
    <appender-ref ref="LOGSTASH" />
  </root>

</configuration>
```

### Ekkada Em Jarugutundi (Step-by-Step):

1.  Mana code lo `log.info("Order placed successfully")` anagane...
2.  Logback aa message ni teesukuni `root` logger ki istundi.
3.  `root` logger chustundi: "Naaku rendu `appender-ref` lu unnai."
4.  **At the same time (Simultaneously):**
    * Adi log message ni `STDOUT` appender ki pampistundi -\> Log mana IntelliJ/VSCode console lo print avutundi.
    * Adi log message ni `LOGSTASH` appender ki kuda pampistundi -\> Log network lo travel chesi `logstash:5044` lo unna container ki vellipotundi.

-----

### Practical Usecase (Dev vs Prod) 💻 vs 🚀

Idi chala important practical knowledge mawa, 40LPA level ki idey kavalsindi:

* **Development Environment (Mana Laptop lo):**

    * Manam **rendu Appenders** (`STDOUT` + `LOGSTASH`) vadatam.
    * **Enduku?** Code test chesetappudu logs ventane console lo chudali (instant feedback). Ade samayam lo, anni microservices (Order, Product, Payment) logs kalipi Kibana lo ela kanipistunnayo chudali (aggregated view).

* **Production Environment (Live Server lo):**

    * Manam `ConsoleAppender` (`STDOUT`) ni **teesivestham (disable chestam)**. Kevalam `LOGSTASH` appender ni matrame unchutam.
    * **Enduku?** Production server lo console lo logs print cheyadam anedi performance ni taggistundi (I/O operations waste). Akkada manaki kavalsindi logs anni centralized ga `ELK Stack` ki velladame.
