# Intern Hub PM Service

## Overview

PM Service la backend quan ly du an, module, task va thanh vien trong he thong Intern Hub.
Service tap trung vao nghiep vu quan ly cong viec, giao viec, nop ket qua va duyet ket qua.

## Main Features

- Quan ly project, module, task
- Gan thanh vien vao project va module
- Theo doi trang thai cong viec
- PIN verification cho thao tac nhay cam
- JWT/Spring Security bao ve API
- Redis ho tro OTP/PIN workflow

## Technology Stack

- Java 21
- Spring Boot
- Spring Security
- Spring Data JPA
- PostgreSQL
- Redis
- Maven

## Project Structure

```text
src/main/java/com/intern/hub/pm
├── configs
├── controllers
├── dtos
├── enums
├── exceptions
├── generator
├── models
├── repositorys
├── services
├── specification
└── utils
```

## Run Locally

```bash
./mvnw spring-boot:run
```
