# Spring-Boot-React


<p align="center">
  <img  src="https://github.com/TheSYNcoder/Full-Stack-Ecommerce-Spring-Boot-React/blob/https/logof.png">
</p>

This is a full stack web application made with spring boot and ReactJs in the frontend. Relevant blog articles on this are

* [Spring Boot and Docker Setup](https://medium.com/geekculture/a-full-stack-e-commerce-application-using-spring-boot-and-making-a-docker-container-eff46f6f4e14)
* [Integrating ReactJs with the backend](https://medium.com/geekculture/a-reactjs-web-application-with-a-spring-boot-backend-and-containerizing-it-using-docker-3eeaed8cb45a)

## Setting up Locally

* Install [docker](https://www.docker.com) and docker-compose on your system.
* Install `psql` for your system which is a client for the Postgresql database.
* Go the root directory of the system
* Run `docker-compose up --build`
* After the containers are up and running , run `psql -h 127.0.0.1 -d flamup -p 5432 -U postgres -c "\copy clothes FROM 'data2.csv' DELIMITER ',' CSV";`.

Now continue to https://localhost:8443, the application should be up and running.

### Caveats

* In case you are using chrome, go to chrome://flags/#allow-insecure-localhost and Enable the option that says "Allow invalid certificates for resources loaded from localhost".

## Servirtium VCR & Selaenium Integration

This repository integrates [Servirtium VCR](https://github.com/servirtium/servirtium-vcr) ([v2.0.0-alpha.3 release](https://github.com/servirtium/servirtium-vcr/releases/tag/v2.0.0-alpha.3)) alongside Selaenium browser automation.

### Architecture & Rationale

Per Paul Hammant's [Technology Compatibility Kits (TCK) and Service Virtualization rationale](https://paulhammant.com/2018/07/13/another-graphic-for-technology-compatibility-kits/), Servirtium VCR captures real HTTP wire interactions into human-readable, git-diffable Markdown tapes (`tapes/react-app-interactions.md`).

1. **Record Mode (`SelaeniumServirtiumRecordTest.java`)**:
   - Boots the live Spring Boot backend server.
   - Starts Servirtium VCR in record mode (`Vcr.record(...)`), mounting the compiled React UI (`frontend/build`).
   - Drives Selaenium (`RemoteWebDriver`) to interact with the React UI and trigger real backend endpoints (`/api/auth`, `/api/login`, `/api/register`, `/api/v1/clothes`).
   - Writes all HTTP request/response pairs into canonical Servirtium Markdown tape files.

2. **Playback Mode (`SelaeniumServirtiumPlaybackTest.java`)**:
   - Runs **without** booting Spring Boot or a database server.
   - Starts Servirtium VCR in playback mode (`Vcr.playback(...)`) serving the real compiled React UI and replaying API responses from the committed Markdown tapes.
   - Selaenium clicks through the real React UI over the Servirtium mock HTTP server, verifying complete UI compatibility deterministically and rapidly in CI.
