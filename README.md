# Task Scheduler Service

Microsserviço central da plataforma, responsável pela criação, agendamento e gerenciamento de tarefas dos usuários. Integra-se com o serviço `user` (autenticação) e com o serviço `notification` (envio de alertas), fazendo parte de uma arquitetura de microsserviços.

## Responsabilidades

- Criação, consulta, atualização e exclusão de tarefas
- Agendamento de execuções/lembretes
- Validação de identidade via token JWT emitido pelo serviço `user`
- Comunicação com outros serviços via cliente HTTP declarativo (Feign)

## Stack técnica

| Categoria | Tecnologia |
|---|---|
| Linguagem | Java 23 |
| Framework | Spring Boot 3.5.5 |
| Persistência | Spring Data MongoDB |
| Comunicação entre serviços | Spring Cloud OpenFeign |
| Segurança | Spring Security + JWT (JJWT) |
| Mapeamento de objetos | MapStruct |
| Build | Gradle |
| Testes | JUnit 5, Spring Security Test |
| CI/CD | GitHub Actions |
| Boilerplate | Lombok |
| Gerenciamento de versões | Spring Cloud BOM (`spring-cloud-dependencies`) |

## Arquitetura

Este serviço utiliza **MongoDB** como banco de dados, adequado à natureza flexível e evolutiva do modelo de tarefas/agendamentos. A comunicação com outros microsserviços (por exemplo, para validar usuários ou disparar notificações) é feita via **Feign Clients**, evitando acoplamento direto e mantendo contratos de API bem definidos entre os serviços.

## Como executar localmente

### Pré-requisitos
- Java 23 (JDK)
- MongoDB em execução localmente ou via container
- Gradle (o wrapper `./gradlew` já está incluso no projeto)

### Passos

```bash
# Clonar o repositório
git clone https://github.com/gilbertneto/task-scheduler.git
cd task-scheduler

# Build
./gradlew build

# Executar
./gradlew bootRun
```

### Configuração

Defina as seguintes variáveis de ambiente (ou configure em `application.properties`/`application.yml`):

```
MONGODB_URI=mongodb://localhost:27017/task_scheduler_db
JWT_SECRET=<mesma-chave-utilizada-pelo-serviço-user>
USER_SERVICE_URL=http://localhost:8080
NOTIFICATION_SERVICE_URL=http://localhost:8082
```

## Testes

A implementar...

## CI/CD

O repositório possui pipeline configurada em `.github/workflows`, executando build e testes automaticamente a cada push/pull request.

## Roadmap / melhorias futuras

- [x] Documentar endpoints via OpenAPI/Swagger
- [ ] Adicionar exemplos de payload de criação/agendamento de tarefas
- [ ] Adicionar Dockerfile e instruções de execução via container
- [ ] Avaliar uso de mensageria assíncrona (ex.: RabbitMQ/Kafka) para desacoplar chamadas ao serviço de notificação

## Ecossistema Relacionado 

- https://github.com/gilbertneto/user
- https://github.com/gilbertneto/notification	
- https://github.com/gilbertneto/bff-task-scheduler
