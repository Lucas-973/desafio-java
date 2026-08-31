# API de Gestão de Portfólio

API REST para cadastro e acompanhamento de projetos de um portfólio. A aplicação permite criar, consultar, filtrar, atualizar e cancelar projetos, calcula automaticamente sua classificação de risco e disponibiliza indicadores consolidados do portfólio.

O repositório também inclui o módulo `mock-membros`, uma API em memória que simula o serviço externo responsável pelos gerentes e funcionários.

## Tecnologias

- Java 25
- Spring Boot 4.1.1
- Spring Web MVC, Data JPA, Security e Validation
- PostgreSQL
- Flyway
- Springdoc OpenAPI / Swagger UI
- Maven Wrapper
- JUnit, Mockito, H2 e JaCoCo

## Pré-requisitos

- JDK 25
- PostgreSQL em execução

## Configuração

Crie no PostgreSQL um banco chamado `portfolio`:

```sql
CREATE DATABASE portfolio;
```

Depois copie o arquivo de exemplo e preencha as credenciais:

```powershell
Copy-Item .env.exemplo .env
```

```properties
DB_URL=jdbc:postgresql://localhost:5432/portfolio
DB_USERNAME=postgres
DB_PASSWORD=sua_senha

APP_USERNAME=admin
APP_PASSWORD=admin

MEMBROS_API_URL=http://localhost:8090
```

As tabelas e os índices são criados automaticamente pelo Flyway ao iniciar a API principal.

## Executando o projeto

As duas aplicações devem ser iniciadas em terminais separados.

### 1. API mock de membros

No Windows:

```powershell
.\mvnw.cmd -f mock-membros\pom.xml spring-boot:run
```

No Linux ou macOS:

```bash
./mvnw -f mock-membros/pom.xml spring-boot:run
```

O mock ficará disponível em `http://localhost:8090`. Seus dados são mantidos somente em memória e são reiniciados junto com a aplicação.

### 2. API de portfólio

No Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

No Linux ou macOS:

```bash
./mvnw spring-boot:run
```

A API ficará disponível em `http://localhost:8080`.

## Documentação da API

- Portfólio: http://localhost:8080/swagger-ui.html
- Mock de membros: http://localhost:8090/swagger-ui.html
- OpenAPI JSON: http://localhost:8080/v3/api-docs

O Swagger da API principal é público, mas a execução dos endpoints exige as credenciais `APP_USERNAME` e `APP_PASSWORD` configuradas no `.env`.

## Endpoints principais

### Projetos

| Método | Rota | Descrição |
| --- | --- | --- |
| `POST` | `/projetos` | Cria um projeto |
| `GET` | `/projetos/{id}` | Consulta um projeto por ID |
| `GET` | `/projetos` | Lista e filtra projetos com paginação |
| `PATCH` | `/projetos/{id}` | Atualiza parcialmente um projeto |
| `DELETE` | `/projetos/{id}` | Cancela um projeto elegível |
| `GET` | `/relatorios/portfolio` | Retorna indicadores consolidados |

Filtros aceitos em `GET /projetos`:

- `nome`
- `situacao`
- `gerenteId`
- `dataInicioDe` e `dataInicioAte`
- `orcamentoMinimo` e `orcamentoMaximo`
- parâmetros padrão de paginação, como `page`, `size` e `sort`

### Mock de membros

| Método | Rota | Descrição |
| --- | --- | --- |
| `POST` | `/api/membros` | Cadastra um gerente ou funcionário |
| `GET` | `/api/membros/{id}` | Consulta um membro por ID |
| `GET` | `/api/membros` | Lista todos os membros |

## Exemplo de uso

O mock pode ser usado de duas formas.

### Opção 1: usar os membros iniciais

Ao iniciar, o mock já contém gerentes e funcionários. Por exemplo, use o gerente de ID `1` (Wagner Parisoto) e o funcionário de ID `3` (Lucas Teixeira). A lista completa pode ser consultada com:

```bash
curl http://localhost:8090/api/membros
```

### Opção 2: cadastrar novos membros

Se preferir, cadastre um gerente e um funcionário:

```bash
curl -X POST http://localhost:8090/api/membros \
  -H "Content-Type: application/json" \
  -d '{"nome":"Maria Gerente","atribuicao":"GERENTE"}'

curl -X POST http://localhost:8090/api/membros \
  -H "Content-Type: application/json" \
  -d '{"nome":"João Silva","atribuicao":"FUNCIONARIO"}'
```

Guarde os IDs retornados pelo mock. Como os seis membros iniciais ocupam os IDs de `1` a `6`, em uma instância recém-iniciada esses novos cadastros receberão os IDs `7` e `8`.

Em seguida, crie um projeto. O exemplo abaixo usa os IDs da opção 1; caso tenha escolhido a opção 2, substitua `gerenteId` e `membrosIds` pelos IDs retornados nos cadastros:

```bash
curl -X POST http://localhost:8080/projetos \
  -u admin:admin \
  -H "Content-Type: application/json" \
  -d '{
    "nome": "Modernização do portal",
    "dataInicio": "2026-09-01",
    "dataFimPrevisao": "2026-12-01",
    "orcamento": 95000.00,
    "descricao": "Atualização da plataforma institucional",
    "gerenteId": 1,
    "membrosIds": [3]
  }'
```

As datas de criação devem respeitar a data corrente: `dataInicio` não pode estar no passado e `dataFimPrevisao` não pode ser anterior ao início.

## Regras de negócio

### Status

Um projeto é criado como `EM_ANALISE`. As mudanças devem seguir esta ordem:

```text
EM_ANALISE → ANALISE_REALIZADA → ANALISE_APROVADA → PLANEJADO
→ INICIADO → EM_ANDAMENTO → ENCERRADO
```

Projetos nos estados `INICIADO`, `EM_ANDAMENTO` ou `ENCERRADO` não podem ser excluídos. A exclusão permitida é lógica e altera o status para `CANCELADO`.

### Classificação de risco

| Risco | Critério |
| --- | --- |
| `BAIXO` | orçamento até R$ 100.000 e duração de até 3 meses |
| `MEDIO` | orçamento acima de R$ 100.000 ou duração acima de 3 meses |
| `ALTO` | orçamento acima de R$ 500.000 ou duração acima de 6 meses |

O maior critério aplicável prevalece.

### Equipe

- o gerente informado deve possuir atribuição `GERENTE`;
- os IDs da equipe devem pertencer a membros com atribuição `FUNCIONARIO`;
- um projeto deve possuir entre 1 e 10 funcionários;
- cada funcionário pode participar de no máximo 3 projetos ativos.

## Testes e cobertura

Execute a suíte da API principal:

```powershell
.\mvnw.cmd test
```

Execute também a suíte do mock:

```powershell
.\mvnw.cmd -f mock-membros\pom.xml test
```

Para executar as verificações do JaCoCo e gerar o relatório de cobertura:

```powershell
.\mvnw.cmd verify
```

O build exige pelo menos 70% de cobertura de linhas no pacote de serviços. O relatório HTML é gerado em `target/site/jacoco/index.html`.

## Estrutura do projeto

```text
.
├── src/
│   ├── main/
│   │   ├── java/com/desafio_java/desafio_java/
│   │   │   ├── client/              # Integração com a API de membros
│   │   │   ├── config/              # Segurança, OpenAPI e tratamento JSON
│   │   │   ├── controller/          # Endpoints REST
│   │   │   ├── dto/                 # Objetos de entrada e saída
│   │   │   ├── entity/              # Entidades e enumerações de domínio
│   │   │   ├── exception/           # Exceções e tratamento global
│   │   │   ├── mapper/              # Conversão entre entidades e DTOs
│   │   │   ├── repository/          # Persistência, projeções e filtros
│   │   │   ├── service/             # Regras de negócio
│   │   │   └── DesafioJavaApplication.java
│   │   └── resources/
│   │       ├── application.yaml     # Configuração da API principal
│   │       └── db/migration/        # Migrações Flyway
│   └── test/                        # Testes da API principal
├── mock-membros/
│   ├── src/main/java/com/desafio_java/mock_membros/
│   │   ├── controller/              # Endpoints do mock
│   │   ├── dto/                     # Objetos de entrada e saída
│   │   ├── model/                   # Modelo de atribuição
│   │   ├── service/                 # Armazenamento em memória
│   │   └── MockMembrosApplication.java
│   ├── src/main/resources/          # Configuração do mock
│   ├── src/test/                    # Testes do mock
│   └── pom.xml                      # Build do mock
├── .mvn/                            # Configuração do Maven Wrapper
├── .env.exemplo                    # Variáveis de ambiente de exemplo
├── mvnw                             # Maven Wrapper para Linux e macOS
├── mvnw.cmd                         # Maven Wrapper para Windows
├── pom.xml                          # Build Maven principal
└── README.md                        # Documentação do projeto
```
