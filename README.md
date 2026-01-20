# 🚚 AlgaDelivery - Arquitetura de Microsserviços

Uma aplicação moderna de gerenciamento de entregas construída com uma **arquitetura de microsserviços escalável e resiliente**, aplicando princípios de **Domain-Driven Design (DDD)**, **Event-Driven Architecture** e padrões de resiliência.

## 📋 Índice

- [Visão Geral da Arquitetura](#visão-geral-da-arquitetura)
- [Microsserviços](#microsserviços)
- [Tecnologias Utilizadas](#tecnologias-utilizadas)
- [Padrões Arquiteturais](#padrões-arquiteturais)
- [Pré-requisitos](#pré-requisitos)
- [Instalação e Execução](#instalação-e-execução)
- [Estrutura do Projeto](#estrutura-do-projeto)
- [Componentes Principais](#componentes-principais)
- [Padrões de Resiliência](#padrões-de-resiliência)
- [Documentação Detalhada](#documentação-detalhada)
- [Contribuições](#contribuições)

---

## 🏗️ Visão Geral da Arquitetura

AlgaDelivery implementa uma **arquitetura de microsserviços distribuída** com os seguintes características principais:

```
┌─────────────────────────────────────────────────────────────┐
│                    Cliente (Frontend)                        │
└────────────────────────┬────────────────────────────────────┘
                         │
                         ▼
        ┌──────────────────────────────┐
        │     API Gateway (Spring       │
        │     Cloud Gateway)            │
        │  - Roteamento inteligente     │
        │  - Circuit Breaker            │
        │  - Rate Limiting              │
        └──────────┬───────────────────┘
                   │
    ┌──────────────┼──────────────┬──────────────┐
    │              │              │              │
    ▼              ▼              ▼              ▼
┌─────────┐  ┌──────────┐  �──────────┐  ┌──────────────┐
│Service  │  │Delivery  │  │Courier   │  │Service       │
│Registry │  │Tracking  │  │Management│  │Registry      │
│(Eureka) │  │Service   │  │Service   │  │(Eureka)      │
└─────────┘  └──────────┘  └──────────┘  └──────────────┘
    │            │             │              │
    └────────────┼─────────────┼──────────────┘
                 │ Domain Events
                 │ (Apache Kafka)
                 ▼
         ┌──────────────────┐
         │  Apache Kafka    │
         │  Message Broker  │
         └──────────────────┘
                 │
    ┌────────────┼────────────┐
    │            │            │
    ▼            ▼            ▼
┌─────────┐  ┌─────────┐  ┌─────────┐
│PostgreSQL   │         │         │
│Database 1   │ Cache   │ Search  │
└─────────┘  └─────────┘  └─────────┘
```

---

## 🔧 Microsserviços

### 1. **Service Registry** (Eureka)
- **Porta**: 8761
- **Função**: Registro e descoberta de serviços
- **Responsabilidade**: Manter registro de todos os microsserviços em execução
- **Stack**: Spring Cloud Eureka Server

```yaml
# Características:
- Descoberta automática de serviços
- Health checks periódicos
- Balanceamento de carga client-side
```

### 2. **API Gateway** (Spring Cloud Gateway)
- **Porta**: 8080
- **Função**: Ponto de entrada único para a aplicação
- **Padrão Implementado**: **API Gateway Pattern**

**Responsabilidades:**
- ✅ Roteamento inteligente de requisições
- ✅ Circuit Breaker com Resilience4j
- ✅ Rate limiting e throttling
- ✅ Autenticação e autorização centralizada
- ✅ Tratamento de timeouts
- ✅ Logging e monitoramento de requisições

```java
// Exemplo de configuração com Circuit Breaker
@Bean
public RouteLocator gatewayRoutes(RouteLocatorBuilder builder) {
    return builder.routes()
        .route("courier-service",
            r -> r.path("/couriers/**")
                .filters(f -> f.circuitBreaker(config -> config
                    .setName("courierCircuitBreaker")
                    .setFallbackUri("forward:/fallback")))
                .uri("lb://COURIER-MANAGEMENT"))
        .build();
}
```

### 3. **Courier Management Service**
- **Porta**: 8081
- **Função**: Gerenciamento de entregadores
- **Contexto de Bounded**: Courier Management

**Recursos:**
- 📦 CRUD de entregadores
- 📍 Rastreamento de localização
- 🔄 Disponibilidade e status
- 📨 Publicação de Domain Events

**Camadas DDD:**
```
src/main/java/com/samuel/algadelivery/
├── courier/
│   ├── domain/
│   │   ├── entity/
│   │   │   └── Courier.java (Aggregate Root)
│   │   ├── value/
│   │   │   ├── CourierId.java
│   │   │   ├── Location.java
│   │   │   └── Status.java
│   │   ├── event/
│   │   │   ├── CourierCreatedEvent.java
│   │   │   ├── CourierAvailableEvent.java
│   │   │   └── CourierLocationUpdatedEvent.java
│   │   └── service/
│   │       └── CourierDomainService.java
│   ├── application/
│   │   ├── dto/
│   │   ├── service/
│   │   │   └── CourierApplicationService.java
│   │   └── event/
│   │       └── CourierEventListener.java
│   ├── infrastructure/
│   │   ├── repository/
│   │   │   └── CourierRepository.java (Spring Data JPA)
│   │   ├── persistence/
│   │   │   └── CourierJpaEntity.java
│   │   ├── config/
│   │   │   └── KafkaProducerConfig.java
│   │   └── api/
│   │       └── CourierController.java (REST)
│   └── model/
│       └── Courier.java (Aggregate)
```

### 4. **Delivery Tracking Service**
- **Porta**: 8082
- **Função**: Rastreamento de entregas
- **Contexto de Bounded**: Delivery Tracking

**Recursos:**
- 📊 Acompanhamento de status de entrega
- 🗺️ Histórico de movimentação
- 🔔 Notificações de cliente
- 📞 Comunicação com Courier Service

**Fluxo de Mensagens:**
```
Courier Management                 Kafka Topic                 Delivery Tracking
───────────────────              ──────────────              ─────────────────

CourierAvailable ─────────────→ courier.events ─────────────→ Subscribe & Process
    Event                                            Update delivery assignment
```

---

## 🛠️ Tecnologias Utilizadas

### **Framework & Linguagem**
- **Java 21** - Versão LTS moderna com records, pattern matching
- **Spring Boot 3.5.9** - Framework principal
- **Maven** - Build tool

### **Infraestrutura & Descoberta**
| Tecnologia | Versão | Uso |
|-----------|--------|-----|
| Spring Cloud | 2025.0.0+ | Microsserviços e cloud |
| Netflix Eureka | - | Service Registry & Discovery |
| Spring Cloud Gateway | - | API Gateway (padrão) |
| Spring Cloud LoadBalancer | - | Balanceamento client-side |

### **Persistência de Dados**
- **PostgreSQL 17.5** - Banco de dados principal
- **Spring Data JPA** - ORM e repositório
- **Hibernate** - Provider JPA padrão
- **pgAdmin 4** - Administração do banco (porta 8083)

### **Mensageria & Eventos**
- **Apache Kafka 4.1.1** - Message Broker
- **Spring Kafka** - Integração com Spring Boot
- **Kafka UI** - Interface de monitoramento (porta 8084)

**Tópicos Kafka:**
```
- courier.events          (Domain Events do Courier)
- delivery.events         (Domain Events de Delivery)
- delivery.tracking       (Eventos de rastreamento)
```

### **Padrões de Resiliência**
- **Resilience4j** - Circuit Breaker, Retry, Timeout
- **Spring Retry** - Retry automático
- **Spring Cloud Circuit Breaker** - Abstração de circuit breaker

### **APIs & Comunicação**
- **Spring RestClient** - Cliente HTTP (novo em Spring 6.1)
- **REST Assured** - Testes de API REST
- **Spring Validation** - Bean Validation (@Valid, @NotNull, etc)

### **Testing**
- **JUnit 5** - Framework de testes
- **REST Assured** - Testes de API REST
- **Testcontainers** - Testes de integração

### **Observabilidade**
- **Spring Boot Actuator** - Health checks e métricas
- **Micrometer** - Coleta de métricas

---

## 📐 Padrões Arquiteturais

### **1. Domain-Driven Design (DDD)**

Cada microsserviço é organizado em camadas DDD com separação clara de responsabilidades:

#### **Camada de Domínio**
```java
// Aggregate Root - Entidade principal do domínio
public class Courier {
    private CourierId id;
    private String name;
    private Location location;
    private CourierStatus status;
    private List<DomainEvent> domainEvents;
    
    // Métodos do negócio (Ubiquitous Language)
    public void becomeAvailable(Location location) {
        this.status = CourierStatus.AVAILABLE;
        this.location = location;
        addDomainEvent(new CourierAvailableEvent(this.id, location));
    }
    
    public void addDomainEvent(DomainEvent event) {
        domainEvents.add(event);
    }
}

// Value Objects
public record Location(BigDecimal latitude, BigDecimal longitude) {}
public record CourierId(UUID value) {}
```

#### **Domain Events**
```java
public abstract class DomainEvent {
    private final Instant occurredAt;
    private final UUID eventId;
}

public class CourierCreatedEvent extends DomainEvent {
    private final CourierId courierId;
    private final String name;
}

public class CourierAvailableEvent extends DomainEvent {
    private final CourierId courierId;
    private final Location location;
}
```

#### **Domain Services**
```java
// Domain Service - Lógica de domínio que não pertence a uma entidade
@Service
public class CourierAssignmentDomainService {
    
    public void assignCourierToDelivery(
        Courier courier, 
        Delivery delivery) {
        
        if (!courier.canAssignToDelivery()) {
            throw new BusinessException("Courier unavailable");
        }
        
        courier.assignToDelivery(delivery);
        delivery.assignCourier(courier);
    }
}
```

#### **Application Services**
```java
@Service
public class CreateCourierUseCase {
    
    private final CourierRepository repository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    
    @Transactional
    public CreateCourierOutput execute(CreateCourierInput input) {
        // Criar entidade
        Courier courier = Courier.create(input.name(), input.email());
        
        // Persistir
        Courier saved = repository.save(courier);
        
        // Publicar eventos
        saved.getDomainEvents()
             .forEach(event -> kafkaTemplate.send("courier.events", event));
        
        return new CreateCourierOutput(saved.getId());
    }
}
```

#### **Repository Pattern**
```java
// Interface de domínio
public interface CourierRepository {
    void save(Courier courier);
    Optional<Courier> findById(CourierId id);
    List<Courier> findAvailable();
}

// Implementação com Spring Data JPA
@Repository
public interface CourierJpaRepository extends JpaRepository<CourierJpaEntity, UUID> {
    List<CourierJpaEntity> findByStatus(CourierStatus status);
}

// Adapter que implementa a interface de domínio
@Component
public class CourierRepositoryAdapter implements CourierRepository {
    
    @Autowired
    private CourierJpaRepository jpaRepository;
    
    @Override
    public void save(Courier courier) {
        CourierJpaEntity entity = CourierMapper.toPersistence(courier);
        jpaRepository.save(entity);
    }
}
```

---

### **2. Event-Driven Architecture**

A comunicação entre microsserviços é baseada em eventos de domínio através do Apache Kafka.

#### **Fluxo de Eventos**
```
Microsserviço 1                 Kafka Topics              Microsserviço 2
─────────────────              ─────────────             ─────────────────

1. Evento ocorre no domínio
   CourierAvailableEvent

2. Publicado na camada                                   3. Subscritor recebe
   de aplicação              ──→ courier.events  ──→      o evento

                                                         4. Processa evento
                                                            (UpdateDelivery)

                                                         5. Emite novo evento
                                                            DeliveryAssigned ──→ delivery.events
```

#### **Produtor de Eventos (Kafka)**
```yaml
# application.yml
spring:
  kafka:
    bootstrap-servers: kafka:9090
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.springframework.kafka.support.serializer.JsonSerializer
```

```java
@Service
public class CourierEventPublisher {
    
    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;
    
    public void publish(DomainEvent event) {
        kafkaTemplate.send("courier.events", event.getEventId().toString(), event)
            .whenComplete((result, exception) -> {
                if (exception != null) {
                    log.error("Erro ao publicar evento", exception);
                } else {
                    log.info("Evento publicado: {}", event);
                }
            });
    }
}
```

#### **Consumidor de Eventos (Kafka)**
```java
@Service
public class DeliveryEventListener {
    
    @KafkaListener(topics = "courier.events", groupId = "delivery-service")
    public void handleCourierAvailableEvent(
        @Payload CourierAvailableEvent event,
        @Header(KafkaHeaders.RECEIVED_PARTITION_ID) int partition) {
        
        log.info("Evento recebido na partição {}: {}", partition, event);
        
        // Processar evento
        deliveryService.assignCourierToDelivery(
            event.getCourierId(),
            event.getLocation()
        );
    }
}
```

---

### **3. API Gateway Pattern**

Implementado com Spring Cloud Gateway, responsável por:

```java
@Configuration
public class GatewayConfig {
    
    @Bean
    public RouteLocator routes(RouteLocatorBuilder builder) {
        return builder.routes()
            // Route para Courier Management
            .route("courier-service", r -> r
                .path("/couriers/**")
                .filters(f -> f
                    .rewritePath("/couriers/(?<segment>.*)", "/$\\{segment}")
                    .circuitBreaker(config -> config
                        .setName("courierCircuitBreaker")
                        .setFallbackUri("forward:/fallback/courier"))
                    .retry(config -> config
                        .setRetries(3)
                        .setMethods(HttpMethod.GET, HttpMethod.POST)))
                .uri("lb://COURIER-MANAGEMENT"))
            
            // Route para Delivery Tracking
            .route("delivery-service", r -> r
                .path("/deliveries/**")
                .filters(f -> f
                    .rewritePath("/deliveries/(?<segment>.*)", "/$\\{segment}")
                    .circuitBreaker(config -> config
                        .setName("deliveryCircuitBreaker")
                        .setFallbackUri("forward:/fallback/delivery")))
                .uri("lb://DELIVERY-TRACKING"))
            
            .build();
    }
}
```

**Responsabilidades do Gateway:**
- ✅ Roteamento baseado em path
- ✅ Reescrita de URLs
- ✅ Circuit Breaker para cada serviço
- ✅ Retry automático em falhas transitórias
- ✅ Rate limiting
- ✅ Autenticação JWT (extensível)

---

### **4. Persistência com Spring Data JPA**

#### **Mapeamento ORM**
```java
@Entity
@Table(name = "couriers")
public class CourierJpaEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(nullable = false)
    private String name;
    
    @Embedded
    private LocationEmbedded location;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CourierStatus status;
    
    @Column(name = "created_at")
    private Instant createdAt;
    
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DeliveryAssignmentEntity> deliveries;
}

@Embeddable
public class LocationEmbedded {
    @Column(name = "latitude")
    private BigDecimal latitude;
    
    @Column(name = "longitude")
    private BigDecimal longitude;
}
```

#### **Repository**
```java
@Repository
public interface CourierJpaRepository extends JpaRepository<CourierJpaEntity, UUID> {
    
    @Query("SELECT c FROM CourierJpaEntity c WHERE c.status = :status")
    List<CourierJpaEntity> findByStatus(@Param("status") CourierStatus status);
    
    @Query("SELECT c FROM CourierJpaEntity c WHERE c.location.latitude BETWEEN :minLat AND :maxLat")
    List<CourierJpaEntity> findNearby(
        @Param("minLat") BigDecimal minLat,
        @Param("maxLat") BigDecimal maxLat
    );
}
```

---

### **5. Padrões de Resiliência**

#### **a) Circuit Breaker com Resilience4j**

```yaml
# application.yml
resilience4j:
  circuitbreaker:
    instances:
      courierService:
        registerHealthIndicator: true
        slidingWindowSize: 10
        minimumNumberOfCalls: 5
        permittedNumberOfCallsInHalfOpenState: 3
        slowCallDurationThreshold: 2000
        slowCallRateThreshold: 100
        failureRateThreshold: 50
        waitDurationInOpenState: 10000
        transitionFromOpenToHalfOpenEnabled: true
```

```java
@Service
public class CourierServiceClient {
    
    @Circuitbreaker(
        name = "courierService",
        fallbackMethod = "getAvailableCouriersFallback"
    )
    public List<Courier> getAvailableCouriers() {
        return restClient.get()
            .uri("http://localhost:8081/couriers/available")
            .retrieve()
            .body(new ParameterizedTypeReference<>() {});
    }
    
    public List<Courier> getAvailableCouriersFallback(Exception e) {
        log.warn("Circuit breaker ativado, retornando cache local");
        return getCacheFromLocalStorage();
    }
}
```

**Estados do Circuit Breaker:**
```
┌─────────┐
│ CLOSED  │ ◄── Requisições passam normalmente
└────┬────┘
     │ (Falhas excedem threshold)
     ▼
┌─────────┐
│ OPEN    │ ◄── Requisições são rejeitadas
└────┬────┘
     │ (Após wait duration)
     ▼
┌────────────┐
│ HALF_OPEN │ ◄── Testa se serviço recuperou
└────┬───────┘
     │
     ├─(Sucesso)──→ CLOSED
     └─(Falha)────→ OPEN
```

#### **b) Retry Pattern**

```yaml
# application.yml
resilience4j:
  retry:
    instances:
      courierApi:
        maxAttempts: 3
        waitDuration: 1000
        retryExceptions:
          - java.io.IOException
          - java.net.SocketTimeoutException
        ignoreExceptions:
          - com.samuel.algadelivery.BusinessException
```

```java
@Service
public class CourierServiceRetry {
    
    @Retry(
        name = "courierApi",
        fallbackMethod = "getDefaultCourierFallback"
    )
    public Courier getCourier(UUID courierId) {
        return restClient.get()
            .uri("http://localhost:8081/couriers/{id}", courierId)
            .retrieve()
            .body(Courier.class);
    }
    
    public Courier getDefaultCourierFallback(
        UUID courierId, 
        Exception e) {
        log.warn("Retries esgotados, retornando valor default");
        return new Courier(); // valor padrão
    }
}
```

**Fluxo de Retry:**
```
Requisição 1 ──→ FALHA (IOException) ──→ Aguarda 1s
            ↓
Requisição 2 ──→ FALHA (Timeout) ──→ Aguarda 1s
            ↓
Requisição 3 ──→ SUCESSO ✓
            ↓
Retorna resposta
```

#### **c) Timeout Pattern**

```yaml
# application.yml
resilience4j:
  timelimiter:
    instances:
      courierApi:
        timeoutDuration: 5000
        cancelRunningFuture: true
```

```java
@Service
public class CourierServiceWithTimeout {
    
    @TimeLimiter(
        name = "courierApi",
        fallbackMethod = "getCouriersTimeoutFallback"
    )
    public CompletableFuture<List<Courier>> getAvailableCouriers() {
        return CompletableFuture.supplyAsync(() -> 
            restClient.get()
                .uri("http://localhost:8081/couriers/available")
                .retrieve()
                .body(new ParameterizedTypeReference<>() {})
        );
    }
    
    public CompletableFuture<List<Courier>> getCouriersTimeoutFallback(
        Exception e) {
        return CompletableFuture.completedFuture(Collections.emptyList());
    }
}
```

#### **d) Combinando Padrões (Resilience Pattern Chain)**

```java
@Service
public class ResilientCourierService {
    
    @CircuitBreaker(name = "courierService", fallbackMethod = "fallback")
    @Retry(name = "courierApi")
    @TimeLimiter(name = "courierApi")
    @Bulkhead(name = "courierApi")
    public CompletableFuture<Courier> getCourier(UUID courierId) {
        return CompletableFuture.supplyAsync(() -> {
            // Implementação
            return new Courier();
        });
    }
    
    public CompletableFuture<Courier> fallback(UUID id, Exception e) {
        log.error("Todas as estratégias de resiliência falharam", e);
        return CompletableFuture.failedFuture(e);
    }
}
```

---

### **6. REST APIs com Spring RestClient**

Spring 6.1+ introduz o novo `RestClient` como alternativa mais moderna ao `RestTemplate`.

#### **Configuração**
```java
@Configuration
public class HttpClientConfig {
    
    @Bean
    public RestClient restClient(RestClientCustomizer... customizers) {
        return RestClient.builder()
            .baseUrl("http://localhost:8081")
            .requestInterceptor((request, body, execution) -> {
                request.getHeaders().add("X-Client", "AlgaDelivery");
                return execution.execute(request, body);
            })
            .customizers(customizers)
            .build();
    }
}
```

#### **Uso em Serviço**
```java
@Service
public class CourierServiceClient {
    
    private final RestClient restClient;
    
    public CourierServiceClient(RestClient restClient) {
        this.restClient = restClient;
    }
    
    // GET
    public Courier getCourierById(UUID id) {
        return restClient.get()
            .uri("/couriers/{id}", id)
            .retrieve()
            .body(Courier.class);
    }
    
    // GET com List
    public List<Courier> getAvailable() {
        return restClient.get()
            .uri("/couriers/available")
            .retrieve()
            .body(new ParameterizedTypeReference<>() {});
    }
    
    // POST
    public Courier createCourier(CreateCourierRequest request) {
        return restClient.post()
            .uri("/couriers")
            .contentType(MediaType.APPLICATION_JSON)
            .body(request)
            .retrieve()
            .body(Courier.class);
    }
    
    // PUT
    public void updateCourier(UUID id, UpdateCourierRequest request) {
        restClient.put()
            .uri("/couriers/{id}", id)
            .body(request)
            .retrieve()
            .toBodilessEntity();
    }
    
    // DELETE
    public void deleteCourier(UUID id) {
        restClient.delete()
            .uri("/couriers/{id}", id)
            .retrieve()
            .toBodilessEntity();
    }
}
```

---

### **7. Testes com REST Assured**

REST Assured simplifica testes de API REST com uma sintaxe fluida e intuitiva.

#### **Teste de API REST**
```java
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class CourierControllerTest {
    
    @LocalServerPort
    private int port;
    
    @BeforeEach
    public void setup() {
        RestAssured.port = port;
    }
    
    @Test
    void should_create_courier_successfully() {
        CreateCourierRequest request = new CreateCourierRequest(
            "João Silva",
            "joao@email.com"
        );
        
        given()
            .contentType(ContentType.JSON)
            .body(request)
        .when()
            .post("/couriers")
        .then()
            .statusCode(201)
            .header("Location", notNullValue())
            .body("name", equalTo("João Silva"))
            .body("email", equalTo("joao@email.com"))
            .body("status", equalTo("INACTIVE"));
    }
    
    @Test
    void should_get_courier_by_id() {
        UUID courierId = createCourierAndGetId();
        
        given()
            .pathParam("id", courierId)
        .when()
            .get("/couriers/{id}")
        .then()
            .statusCode(200)
            .body("id", equalTo(courierId.toString()))
            .body("status", equalTo("INACTIVE"));
    }
    
    @Test
    void should_list_available_couriers() {
        given()
        .when()
            .get("/couriers/available")
        .then()
            .statusCode(200)
            .body("$", hasSize(greaterThanOrEqualTo(0)));
    }
    
    @Test
    void should_update_courier_location() {
        UUID courierId = createCourierAndGetId();
        
        LocationRequest location = new LocationRequest(
            new BigDecimal("-23.5505"),
            new BigDecimal("-46.6333")
        );
        
        given()
            .contentType(ContentType.JSON)
            .pathParam("id", courierId)
            .body(location)
        .when()
            .patch("/couriers/{id}/location")
        .then()
            .statusCode(204);
    }
    
    @Test
    void should_return_404_when_courier_not_found() {
        UUID nonExistentId = UUID.randomUUID();
        
        given()
            .pathParam("id", nonExistentId)
        .when()
            .get("/couriers/{id}")
        .then()
            .statusCode(404);
    }
}
```

#### **Teste com Validação de Campos**
```java
@Test
void should_validate_courier_creation_fields() {
    CreateCourierRequest invalidRequest = new CreateCourierRequest("", "invalid-email");
    
    given()
        .contentType(ContentType.JSON)
        .body(invalidRequest)
    .when()
        .post("/couriers")
    .then()
        .statusCode(400)
        .body("errors", hasSize(2))
        .body("errors[0].field", anyOf(equalTo("name"), equalTo("email")));
}
```

---

## 📦 Pré-requisitos

### **Versões Requeridas**
- **Java**: 21+
- **Docker**: 20.10+
- **Docker Compose**: 2.0+
- **Maven**: 3.8+

### **Verificar Instalações**
```bash
java -version
docker --version
docker-compose --version
mvn --version
```

---

## 🚀 Instalação e Execução

### **1. Clonar o Repositório**
```bash
git clone https://github.com/seu-usuario/algadelivery.git
cd AlgaDelivery
```

### **2. Iniciar Infraestrutura (Docker Compose)**
```bash
docker-compose up -d
```

**Serviços iniciados:**
- PostgreSQL: `localhost:5432`
- pgAdmin: `http://localhost:8083` (dba@algadelivery.com / algadelivery)
- Kafka: `localhost:9092`
- Kafka UI: `http://localhost:8084`

### **3. Compilar o Projeto**
```bash
mvn clean install
```

### **4. Executar Microsserviços**

#### **Terminal 1 - Service Registry (Eureka)**
```bash
cd Microservices/Service-Registry
mvn spring-boot:run
```
Acesse: `http://localhost:8761`

#### **Terminal 2 - Courier Management Service**
```bash
cd Microservices/Courier-Management
mvn spring-boot:run
```
Porta: `8081`

#### **Terminal 3 - Delivery Tracking Service**
```bash
cd Microservices/Delivery-Tracking
mvn spring-boot:run
```
Porta: `8082`

#### **Terminal 4 - API Gateway**
```bash
cd Microservices/Gateway
mvn spring-boot:run
```
Porta: `8080`

### **5. Acessar Endpoints**

**Via Gateway (recomendado):**
```bash
# Criar entregador
curl -X POST http://localhost:8080/couriers \
  -H "Content-Type: application/json" \
  -d '{
    "name": "João Silva",
    "email": "joao@algadelivery.com"
  }'

# Listar entregadores disponíveis
curl http://localhost:8080/couriers/available

# Rastrear entrega
curl http://localhost:8080/deliveries/{id}
```

---

## 📁 Estrutura do Projeto

```
AlgaDelivery/
├── docker-compose.yml                 # Orquestração de serviços
├── Docs/                              # Documentação
└── Microservices/
    ├── Service-Registry/              # Eureka Server (porta 8761)
    │   ├── pom.xml
    │   └── src/
    │       └── main/java/...ServiceRegistryApplication.java
    │
    ├── Gateway/                       # API Gateway (porta 8080)
    │   ├── pom.xml
    │   ├── src/main/
    │   │   ├── java/.../GatewayApplication.java
    │   │   ├── java/.../config/GatewayConfig.java
    │   │   └── resources/application.yml
    │   └── src/test/...
    │
    ├── Courier-Management/            # Microsserviço (porta 8081)
    │   ├── pom.xml
    │   └── src/
    │       ├── main/
    │       │   ├── java/com/samuel/algadelivery/
    │       │   │   ├── CourierManagementApplication.java
    │       │   │   ├── courier/
    │       │   │   │   ├── domain/
    │       │   │   │   │   ├── entity/Courier.java
    │       │   │   │   │   ├── value/CourierId.java
    │       │   │   │   │   ├── value/Location.java
    │       │   │   │   │   ├── event/
    │       │   │   │   │   │   ├── CourierCreatedEvent.java
    │       │   │   │   │   │   └── CourierAvailableEvent.java
    │       │   │   │   │   └── service/CourierDomainService.java
    │       │   │   │   ├── application/
    │       │   │   │   │   ├── dto/
    │       │   │   │   │   ├── service/CourierApplicationService.java
    │       │   │   │   │   └── event/CourierEventListener.java
    │       │   │   │   ├── infrastructure/
    │       │   │   │   │   ├── repository/CourierJpaRepository.java
    │       │   │   │   │   ├── persistence/CourierJpaEntity.java
    │       │   │   │   │   ├── config/KafkaProducerConfig.java
    │       │   │   │   │   └── api/CourierController.java
    │       │   │   │   └── model/Courier.java
    │       │   │   └── shared/
    │       │   │       ├── domain/DomainEvent.java
    │       │   │       └── infrastructure/KafkaConfig.java
    │       │   └── resources/
    │       │       ├── application.yml
    │       │       └── db/migration/...
    │       └── test/
    │           ├── java/.../courier/...Test.java
    │           └── resources/application-test.yml
    │
    └── Delivery-Tracking/             # Microsserviço (porta 8082)
        ├── pom.xml
        └── src/
            ├── main/
            │   ├── java/com/samuel/algadelivery/
            │   │   ├── DeliveryTrackingApplication.java
            │   │   ├── delivery/
            │   │   │   ├── domain/
            │   │   │   ├── application/
            │   │   │   │   └── event/DeliveryEventListener.java
            │   │   │   └── infrastructure/
            │   │   │       └── api/DeliveryController.java
            │   │   └── shared/
            │   └── resources/application.yml
            └── test/
```

---

## 🔑 Componentes Principais

### **1. Aggregate Roots**
Entidades principais que encapsulam lógica de negócio:
- `Courier` - Representação de um entregador
- `Delivery` - Representação de uma entrega

### **2. Value Objects**
Objetos sem identidade que representam conceitos do domínio:
- `CourierId` - Identificador único de entregador
- `Location` - Coordenadas geográficas
- `CourierStatus` - Status do entregador

### **3. Domain Events**
Eventos que representam fatos importantes no domínio:
- `CourierCreatedEvent` - Entregador criado
- `CourierAvailableEvent` - Entregador disponível
- `DeliveryAssignedEvent` - Entrega atribuída

### **4. Repositories**
Abstrações de persistência:
- `CourierRepository` - Interface de domínio
- `CourierJpaRepository` - Implementação JPA

### **5. Application Services**
Orquestração de casos de uso:
- `CreateCourierUseCase` - Criar novo entregador
- `AssignCourierToDeliveryUseCase` - Atribuir entrega

### **6. Event Listeners**
Processadores de eventos Kafka:
- `CourierEventListener` - Processa eventos de courier
- `DeliveryEventListener` - Processa eventos de delivery

---

## 🛡️ Padrões de Resiliência

### **Matriz de Padrões por Serviço**

| Padrão | Gateway | Courier | Delivery |
|--------|---------|---------|----------|
| Circuit Breaker | ✅ | ✅ | ✅ |
| Retry | ✅ | ✅ | ✅ |
| Timeout | ✅ | ✅ | ✅ |
| Rate Limiting | ✅ | - | - |
| Bulkhead | - | ✅ | ✅ |

### **Configurações Recomendadas**

```yaml
# Produção
resilience4j:
  circuitbreaker:
    instances:
      default:
        slidingWindowSize: 100
        failureRateThreshold: 50
        slowCallRateThreshold: 100
        slowCallDurationThreshold: 3000
        waitDurationInOpenState: 30000
        
  retry:
    instances:
      default:
        maxAttempts: 3
        waitDuration: 1000
        
  timelimiter:
    instances:
      default:
        timeoutDuration: 5000
```

---

## 📚 Documentação Detalhada

### **Padrões de Código**
- [Domain-Driven Design](Docs/DDD.md)
- [Event-Driven Architecture](Docs/Event-Driven.md)
- [API Gateway Pattern](Docs/API-Gateway.md)
- [Padrões de Resiliência](Docs/Resilience-Patterns.md)

### **Guias de Desenvolvimento**
- [Como criar um novo Microsserviço](Docs/New-Microservice.md)
- [Adicionando Domain Events](Docs/Domain-Events.md)
- [Testes com REST Assured](Docs/Testing.md)

### **Deployment**
- [Deployment Docker](Docs/Docker-Deployment.md)
- [Deployment Kubernetes](Docs/K8s-Deployment.md)

---

## 🧪 Testes

### **Executar Testes Unitários**
```bash
mvn test
```

### **Executar Testes de Integração**
```bash
mvn verify
```

### **Testes com Coverage**
```bash
mvn test jacoco:report
open target/site/jacoco/index.html
```

---

## 📊 Monitoramento

### **Actuator Endpoints**
```bash
# Health Check
curl http://localhost:8081/actuator/health

# Métricas
curl http://localhost:8081/actuator/metrics

# Informações da Aplicação
curl http://localhost:8081/actuator/info
```

### **Kafka UI**
Acesse `http://localhost:8084` para visualizar:
- Tópicos
- Mensagens
- Consumers
- Partições

---

## 🔒 Segurança

### **Implementações Recomendadas**
- [ ] OAuth 2.0 no Gateway
- [ ] Validação de JWT
- [ ] Rate Limiting por cliente
- [ ] HTTPS/TLS
- [ ] Secrets Management (Spring Cloud Config)

---
