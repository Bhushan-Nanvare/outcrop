# Outcrop

Outcrop reads a Java repository and produces a map of how it is actually put
together: the dependency graph between its classes and packages, the layers it
falls into, the request flows that run from an endpoint down to the database,
and the places where the structure has drifted from its intended shape.

The analysis is static. Outcrop parses the source into an abstract syntax tree
and derives the graph from it, so the structure it reports is the structure that
exists. A language model is used only to name clusters and narrate flows that
have already been computed, never to decide what the architecture is, and every
statement it produces is anchored to a file and a line.

## Status

Stage 3 of 11. An analysis can be created and then run, which shallow-clones the
repository, records the commit it resolved to and how many Java files it holds,
and deletes the working copy. Nothing parses the source yet.

## Stack

| Layer | Choice |
| --- | --- |
| Backend | Java 21, Spring Boot 4.1 |
| Frontend | Angular 22, TypeScript 6 |
| Database | PostgreSQL, Flyway migrations |
| Parsing | JavaParser (stage 4) |
| Graph algorithms | JGraphT (stage 5) |
| Narration | Gemini (stage 9) |

## Running locally

Requires JDK 21, Node 20 or newer, and PostgreSQL.

Create the database once:

```
createdb -U postgres outcrop
```

The password is read from the environment and is never committed:

```
setx OUTCROP_DB_PASSWORD your-postgres-password
```

Backend, from `outcrop-api`:

```
./mvnw spring-boot:run
```

Listens on port 8080. Flyway applies migrations on startup.

| Endpoint | Purpose |
| --- | --- |
| `POST /api/analyses` | Create an analysis for a GitHub repository URL |
| `POST /api/analyses/{id}/run` | Clone the repository and record the result |
| `GET /api/analyses/{id}` | Fetch one analysis |
| `GET /api/analyses` | List analyses, newest first |
| `GET /api/meta` | Service name and version |
| `GET /actuator/health` | Health check |

Cloning is bounded by `outcrop.clone` in `application.yml`: a working directory,
a Java file limit, a total size limit and a transport timeout. Clones are shallow
and the working copy is deleted as soon as the analysis finishes.

`run` is synchronous for now, so the request stays open for the length of the
clone. Stage 7 moves it onto a background job with progress over SSE.

Frontend, from `outcrop-web`:

```
npm install
npm start
```

Serves on port 4200 and proxies `/api` and `/actuator` to the backend, so the
two run same-origin during development and no CORS configuration is needed.

## Tests

```
cd outcrop-api && ./mvnw test
cd outcrop-web && npm test
```

Backend tests run against in-memory H2 in PostgreSQL mode, so they need no
running database. The trade-off is that the Flyway migrations themselves are not
exercised by the test suite; that arrives with the integration tests in stage 11.

## Roadmap

1. Project skeletons, both applications running and connected.
2. Analysis domain. Postgres, Flyway, the analysis record and its lifecycle.
3. Repository cloning with JGit, with a size guard.
4. Java source parsing. Classes, methods and annotations extracted to the database.
5. Dependency graph construction.
6. Cycle detection, layer inference and drift findings. The first useful output, and it costs no tokens.
7. Analysis moved onto background jobs with progress streamed over SSE.
8. Frontend: submit a repository, watch progress, read the findings.
9. Gemini client. Rate limiting, content-hash caching, token accounting, and narration of the computed architecture.
10. Interactive graph view and endpoint flow tracing.
11. Integration tests, load testing, containerisation and deployment.

## Why the model comes last

Stages 2 through 8 produce a working tool with no language model involved at
all. Cycles, layering violations and hotspots are computed from the syntax tree
and the commit history, so they are reproducible and verifiable. The model is
added in stage 9 to describe that output in prose. If its quota runs out or the
API is unavailable, every finding still stands.
