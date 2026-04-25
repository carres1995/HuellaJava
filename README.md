# huellas

> Java SE project built with **Specification-Driven Development (SDD)** using **MVC architecture** (Controller → Service → Repository/DAO → DB).

## Tech Stack

| Component | Technology |
|-----------|-----------|
| Language | Java 17+ |
| Build | Maven |
| Architecture | MVC (Controller → Service → Repository → DB) |
| Testing | JUnit 5 |
| Database | PostgreSQL (Production) / H2 (Tests) |
| DB Access | Pure JDBC |
| UI | JavaFX |

## Quick Start

```bash
# Compile
mvn compile

# Run tests
mvn test

# Package
mvn package
```

## SDD Workflow

1. Copy `sdd_assets/SPEC_TEMPLATE.md` → `sdd_assets/SPEC_[Feature].md`
2. Fill out Business Rules and BDD Scenarios
3. Feed `CONSTITUTION.md` + filled spec to AI
4. Receive generated code: SQL → Exception → Model → Tests → Repository → Service → Controller → View
5. Run `mvn test` → verify all pass
6. Commit following Conventional Commits (see CONSTITUTION.md §5)

## Project Structure

```
src/main/resources/
├── database.properties  Connection config (url, user, password, driver)
├── app.properties       Application settings
└── sql/                 Database schemas and migration scripts

src/test/resources/
└── database.properties  H2 in-memory — overrides main config for tests

src/main/java/com/huellas/
├── view/           UI layer (JavaFX)
├── controller/     Entry points (MVC Controller)
├── service/        Business logic + transactions
├── repository/     JDBC data access (Repository/DAO)
├── model/          Domain entities (MVC Model)
├── exception/      Business exceptions
├── config/         ConnectionFactory
└── util/           Shared utilities
```
