# 🤖 AWS FinOps MCP Server

Servidor FinOps orientado a IA construido con Spring Boot y AWS SDK v2.
El proyecto expone herramientas tipo MCP (Model Context Protocol) para consultar costes AWS, detectar anomalías y generar recomendaciones de optimización cloud.

---

## 🎯 Objetivo

Centralizar capacidades FinOps reutilizables mediante herramientas desacopladas que puedan ser consumidas por:

* 🤖 AI Agents
* 📊 Dashboards internos
* ☁️ Plataformas cloud
* 🛠️ Herramientas DevOps
* 📁 Sistemas de reporting

El sistema permite:

* Obtener costes AWS por servicio
* Detectar anomalías de gasto
* Generar recomendaciones de rightsizing
* Exportar reportes CSV
* Explicar hallazgos FinOps en lenguaje natural
* Trabajar con múltiples cuentas AWS usando AssumeRole

---

## 🏗️ Arquitectura

El proyecto sigue una arquitectura modular basada en herramientas MCP.

* `controller` → endpoints REST
* `dispatcher` → enrutado dinámico de tools
* `tools` → contratos MCP
* `impl` → implementación de herramientas FinOps
* `service` → lógica de negocio
* `aws` → integración AWS SDK v2
* `account` → gestión multi-account
* `csv` → exportación de reportes

---

## ⚙️ Tecnologías

* Java 21
* Spring Boot 3.3.5
* AWS SDK v2
* Maven
* JUnit 5
* Lombok
* Apache Commons CSV
* Docker

---

## 🧠 Principios SOLID aplicados

### **S - Single Responsibility**

* Cada Tool tiene una responsabilidad concreta
* `ToolDispatcher` únicamente enruta herramientas
* Los adapters AWS encapsulan integración cloud

---

### **O - Open/Closed**

* Nuevas herramientas MCP pueden añadirse sin modificar lógica existente

---

### **L - Liskov Substitution**

* Los servicios AWS pueden sustituirse por mocks o implementaciones alternativas

---

### **I - Interface Segregation**

* `FinOpsTool` define contratos simples y específicos

---

### **D - Dependency Inversion**

* La lógica de negocio depende de interfaces y servicios desacoplados

---

## 🧩 Flujo completo

Cliente / AI Agent
↓
McpController
↓
ToolDispatcher
↓
ToolRegistry
↓
FinOpsTool
↓
AWS Adapter
↓
AWS SDK v2
↓
AWS APIs

---

## 🛠️ Herramientas disponibles

### 📊 `get_top_costs_by_service`

Obtiene los servicios AWS con mayor coste.

#### Parámetros

| Parámetro | Tipo    | Default |
| --------- | ------- | ------- |
| limit     | integer | 10      |
| days      | integer | 7       |

---

### 🚨 `detect_cost_anomalies`

Detecta anomalías y picos de gasto cloud.

#### Parámetros

| Parámetro | Tipo    | Default |
| --------- | ------- | ------- |
| days      | integer | 14      |
| threshold | double  | 2.0     |

---

### 💡 `get_rightsizing_recommendations`

Genera recomendaciones de optimización EC2.

---

### 🧠 `explain_findings`

Resume automáticamente hallazgos FinOps combinando anomalías y recomendaciones.

---

## ☁️ Integración AWS

El proyecto utiliza:

* AWS Cost Explorer
* AWS EC2
* AWS CloudWatch
* AWS STS

---

## 🔐 Multi-account AWS

Soporta múltiples cuentas AWS usando AssumeRole.

Ejemplo `application.yml`

```yaml
finops:
  accounts:
    - account-id: "111111111111"
      alias: "prod"
      role-arn: "arn:aws:iam::111111111111:role/FinOpsReadOnlyRole"
      region: "eu-west-1"

    - account-id: "222222222222"
      alias: "staging"
      role-arn: "arn:aws:iam::222222222222:role/FinOpsReadOnlyRole"
      region: "eu-west-1"
```

---

## 📁 Exportación CSV

Los reportes pueden exportarse automáticamente a CSV mediante:

* `CsvExporter`

Ejemplo generado:

```text
output/cost-report.csv
```

---

## 🟢 Cómo ejecutar

### 1️⃣ Clonar proyecto

```bash
git clone <repo-url>
cd AWS-FinOps-MCP-Server
```

---

### 2️⃣ Configurar credenciales AWS

```bash
aws configure
```

---

### 3️⃣ Compilar proyecto

```bash
mvn clean install
```

---

### 4️⃣ Ejecutar aplicación

```bash
mvn spring-boot:run
```

---

## 🚀 Endpoint principal

```http
POST /mcp/tools/cost/report
```

---

## 📬 Ejemplo petición

```json
{
  "tool": "get_top_costs_by_service",
  "arguments": {
    "days": 30,
    "limit": 5
  }
}
```

---

## 🧪 Testing

El proyecto incluye:

* Unit Tests
* Service Tests
* Tool Tests
* Dispatcher Tests

Ejecutar tests:

```bash
mvn test
```

---

## 🐳 Docker

### Build imagen

```bash
docker build -t aws-finops-mcp .
```

---

### Ejecutar contenedor

```bash
docker run -p 8080:8080 aws-finops-mcp
```

---

## 📦 Build del JAR

```bash
mvn clean package
```

El artefacto generado:

```text
target/aws-finops-mcp-1.0.0.jar
```

---

## 📊 Casos de uso

* AI Agents FinOps
* Dashboards de costes cloud
* Monitorización AWS
* Rightsizing automatizado
* Reporting financiero cloud
* Gobierno multi-account

---

## 🚀 Futuras mejoras

* MCP protocol nativo
* Integración OpenAI / Bedrock
* Savings Plans Analyzer
* Kubernetes cost allocation
* Alertas Slack / Teams
* Dashboards Grafana

---

## 🎯 Conclusión

Este proyecto demuestra:

* Arquitectura modular escalable
* Integración avanzada con AWS SDK v2
* Diseño orientado a herramientas IA
* Buenas prácticas SOLID
* Multi-account AWS
* Exportación automatizada de reportes
* Contenedorización con Docker
* Capacidades FinOps reales
