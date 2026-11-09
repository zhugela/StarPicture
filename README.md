# StarPicture · Picture-Sharing Backend

> A cloud picture-hosting backend — user and picture modules, Tencent COS storage, custom annotation-based authorization, AI out-painting and reverse image search.

![Java](https://img.shields.io/badge/Java-17-orange?logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-2.7-brightgreen?logo=springboot&logoColor=white)
![MySQL](https://img.shields.io/badge/MySQL-8-4479A1?logo=mysql&logoColor=white)
![Redis](https://img.shields.io/badge/COS-Tencent-0052D9)

---

## What this is

A REST backend for a public picture library: users register and sign in, upload pictures (file or URL), organise them into spaces, search and manage them, and trigger AI-driven image editing. Objects live in Tencent COS, metadata in MySQL, and every mutating endpoint is guarded by a custom authorization aspect.

Companion frontend: [star-picture-vue](https://github.com/zhugela/star-picture-vue).

## Features

| Module | Capability |
|---|---|
| User | register / login / logout, role-based access, admin CRUD |
| Picture | upload (multipart + URL), delete, update, paged query, tags & categories |
| Storage | Tencent COS client with configurable bucket, region and host |
| Authorization | `@AuthCheck` annotation + `AuthInterceptor` AOP — no repeated permission code in controllers |
| AI editing | Aliyun out-painting task creation and polling (`api/aliyunai`) |
| Image search | reverse image search facade with pluggable sub-APIs (`api/imagesearch`) |
| Async | Disruptor-based picture-edit event pipeline, dedicated async executor |
| API docs | Knife4j / OpenAPI 2 |

## Design notes

- **Authorization as an aspect.** Instead of an `if (user.getRole() != ADMIN)` in every controller, `@AuthCheck(mustRole = "admin")` declares the requirement and `AuthInterceptor` enforces it. Business code stays clean and the rule is auditable in one place.
- **Persistence split.** Binary content never touches MySQL — only the COS URL and metadata are stored. Database stays small and backups stay fast.
- **Async picture editing.** Out-painting is a long-running external job, so the request creates a task and returns immediately; a Disruptor ring buffer fans the completion event out to handlers.
- **Unified response envelope.** `BaseResponse` + `ResultUtils` + `GlobalExceptionHandler` give every endpoint the same `{code, data, message}` shape, so the frontend has exactly one error path.

## Tech stack

| Layer | Choice |
|---|---|
| Language / runtime | Java 17 |
| Framework | Spring Boot 2.7.6, Spring MVC, Spring AOP |
| Persistence | MyBatis-Plus 3.5, MySQL 8 |
| Object storage | Tencent Cloud COS SDK |
| Concurrency | LMAX Disruptor |
| Utilities | Hutool, Lombok |
| API docs | Knife4j |

## Quick start

```bash
# 1. create the schema
mysql -u root -p < sql/create_table.sql

# 2. configure
#    src/main/resources/application.yml -> MySQL, COS bucket, JWT secret
#    (credentials are read from config/environment, never committed)

# 3. run
mvn spring-boot:run
# API docs: http://localhost:8101/api/doc.html
```

## Project structure

```
src/main/java/com/yu/backend/
├── annotation/    @AuthCheck
├── aop/           AuthInterceptor
├── api/           aliyunai (out-painting) · imagesearch (reverse search)
├── config/        COS · JWT · MyBatis-Plus · Disruptor · async · transaction
├── controller/    user · picture · space
├── manager/       COS manager, upload factory, user-auth manager
├── model/         entity · dto · vo · enums
├── service/       business logic
└── common/        BaseResponse · ResultUtils · error codes
```

## Status

Course-project derived and extended for practice. The picture module is the active area of work; the user module is complete. Some documents under `docs/` are coursework artefacts, not project documentation.
