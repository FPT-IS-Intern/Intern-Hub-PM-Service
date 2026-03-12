# Intern Hub PM Service

## Overview

PM Service la backend quan ly du an, task va thanh vien trong he thong Intern Hub.
Service tap trung vao nghiep vu giao viec, nop ket qua, duyet task, gia han va ket thuc du an.

## Main Features

- Quan ly project va task
- Gan thanh vien vao project
- Theo doi trang thai cong viec
- Upload tai lieu va bai nop qua DMS
- Lay thong tin nguoi dung tu HRM internal
- Security thong qua security starter

## Technology Stack

- Java 21
- Spring Boot
- Spring Security
- Spring Data JPA
- Spring Cloud OpenFeign
- PostgreSQL
- Maven

## Project Structure

```text
src/main/java/com/intern/hub/pm
├── config
├── controller
├── dto
├── enums
├── feign
├── generator
├── model
├── repository
├── service
└── utils
```

## Run Locally

```bash
./mvnw spring-boot:run
```
