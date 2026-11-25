# task-scheduler

Microsserviço central de agendamento de tarefas: criação, consulta por período, atualização, alteração de status e exclusão.

## Responsabilidades

- CRUD de tarefas vinculadas a um usuário (identificado pelo token JWT recebido).
- Consulta de tarefas agendadas em um intervalo de datas.
- Controle de status de notificação da tarefa (ex.: `PENDENTE`, `NOTIFICADO`).
- Validação do usuário autenticado via chamada **Feign** ao serviço `user`.
- Controle de concorrência.

## Stack

| Camada | Tecnologia |
|---|---|
| Linguagem | Java 23 |
| Framework | Spring Boot 3.5.5 |
| Persistência | Spring Data MongoDB (NoSQL) |
| Integração | Spring Cloud OpenFeign (serviço `user`) |
| Mapeamento | MapStruct |
| Testes | JUnit 5, Mockito (inclui teste de concorrência) |
| Container | Docker |

## Endpoints principais (`/tarefas`)

| Método | Rota | Descrição |
|---|---|---|
| `POST` | `/tarefas` | Cria uma nova tarefa |
| `GET` | `/tarefas` | Lista tarefas do usuário autenticado |
| `GET` | `/tarefas/eventos?dataInicial=&dataFinal=` | Lista tarefas agendadas em um período |
| `PUT` | `/tarefas?id=` | Atualiza uma tarefa |
| `PATCH` | `/tarefas?status=&id=` | Altera o status de notificação da tarefa |
| `DELETE` | `/tarefas?id=` | Cancela/remove uma tarefa |

Todas as rotas (exceto quando indicado) exigem o header `Authorization: Bearer <token>`.

## Configuração

Variáveis definidas em `src/main/resources/application.properties`:

```properties
spring.data.mongodb.uri=mongodb://localhost:27017/db_nome
spring.data.mongodb.auto-index-creation=true
user.url=localhost:8080
server.port=8081
```

`user.url` deve apontar para a instância em execução do microsserviço `user`.

## 🐳 Docker

O `Dockerfile` faz build multi-stage com Gradle e roda o `.jar` final em `openjdk:23-jdk`; a porta exposta (`8081`) já é consistente com `server.port`.

```bash
docker build -t task-scheduler-service .

# cria uma rede para o serviço "enxergar" o Mongo pelo nome do container
docker network create tasks-net

docker run -d --name mongo --network tasks-net -p 27017:27017 mongo:latest

docker run --rm --network tasks-net -p 8081:8081 \
  -e SPRING_DATA_MONGODB_URI=mongodb://mongo:27017/db_nome \
  -e USER_URL=http://user:8080 \
  task-scheduler-service
```

Este serviço não tem `docker-composer.yml` próprio — para subir o ecossistema completo (`user` + `task-scheduler` + `notification` + `bff-task-scheduler` + bancos), use o compose disponível no repositório **bff-task-scheduler**.

## Como rodar localmente (sem Docker)

Requer uma instância do MongoDB ativa (local ou via Docker).

```bash
./gradlew bootRun
```

Aplicação sobe em `http://localhost:8081`.

## Testes

```bash
./gradlew test
```

Inclui testes unitários (`TasksServiceTest`) e um teste de integração de concorrência (`TasksConcurrencyIntegrationTest`).

## Integração com outros serviços

- Consome o microsserviço **user** (via `OpenFeign`) para validar/consultar dados do usuário autenticado.
- É consumido pelo **bff-task-scheduler**, que orquestra a rotina de notificação de tarefas próximas do vencimento.
