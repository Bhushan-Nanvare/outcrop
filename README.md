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

Stage 1 of 11. Both applications build, start, and talk to each other. No
analysis is implemented yet.

## Stack

| Layer | Choice |
| --- | --- |
| Backend | Java 21, Spring Boot 4.1 |
| Frontend | Angular 22, TypeScript 6 |
| Database | PostgreSQL (stage 2) |
| Parsing | JavaParser (stage 4) |
| Graph algorithms | JGraphT (stage 5) |
| Narration | Gemini (stage 9) |

## Running locally

Requires JDK 21 and Node 20 or newer.

Backend, from `outcrop-api`:

```
./mvnw spring-boot:run
```

Listens on port 8080. `GET /api/meta` returns the service name and version, and
`GET /actuator/health` reports health.

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
