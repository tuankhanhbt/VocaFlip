# VocaFlip — Tài liệu Kiến trúc

> Ứng dụng học flashcard thông minh với tính năng theo dõi tiến trình kiểu spaced-repetition.

---

## 1. Tổng quan dự án

**VocaFlip** là một REST API backend xây dựng bằng **Spring Boot 4.0**, cho phép người dùng:

- Đăng ký / đăng nhập với JWT
- Tạo và quản lý các bộ flashcard (`FlashcardSet`)
- Thêm, sửa, xoá flashcard trong từng bộ
- Theo dõi tiến trình học theo từng thẻ (`UserCardProgress`) theo kiểu spaced-repetition
- Ghi nhận các phiên học (`StudySession`) và đánh giá từng thẻ trong phiên

### Tech Stack

| Thành phần         | Công nghệ                              |
|--------------------|----------------------------------------|
| Language           | Java 21                                |
| Framework          | Spring Boot 4.0.4                      |
| Security           | Spring Security + JWT (JJWT 0.12.7)    |
| Persistence        | Spring Data JPA / Hibernate            |
| Database           | MySQL (qua `mysql-connector-j`)        |
| Migration          | Flyway                                 |
| Validation         | Jakarta Validation                     |
| Build tool         | Maven (Maven Wrapper)                  |
| Utilities          | Lombok, Spring DevTools                |
| Testing            | Spring Boot Test, Testcontainers       |

---

## 2. Cấu trúc thư mục

```
VocaFlip/
├── src/
│   ├── main/
│   │   ├── java/com/example/vocaflip/
│   │   │   ├── VocaflipApplication.java          # Entry point
│   │   │   ├── auth/                             # Module xác thực
│   │   │   │   ├── controller/AuthController.java
│   │   │   │   ├── dto/request/{Login,Register}Request.java
│   │   │   │   ├── dto/response/AuthResponse.java
│   │   │   │   └── service/AuthService.java
│   │   │   ├── user/                             # Module người dùng
│   │   │   │   ├── controller/UserController.java
│   │   │   │   ├── dto/request/UpdateProfileRequest.java
│   │   │   │   ├── dto/response/UserResponse.java
│   │   │   │   ├── entity/{User,UserRole}.java
│   │   │   │   ├── repository/UserRepository.java
│   │   │   │   └── service/UserService.java
│   │   │   ├── flashcardset/                     # Module bộ flashcard
│   │   │   │   ├── controller/FlashcardSetController.java
│   │   │   │   ├── dto/{FlashcardSetRequest,FlashcardSetResponse}.java
│   │   │   │   ├── entity/FlashcardSet.java
│   │   │   │   ├── repository/FlashcardSetRepository.java
│   │   │   │   └── service/FlashcardSetService.java
│   │   │   ├── flashcard/                        # Module flashcard
│   │   │   │   ├── controller/FlashcardController.java
│   │   │   │   ├── dto/{FlashcardRequest,FlashcardResponse}.java
│   │   │   │   ├── entity/{Flashcard,FrontContentType}.java
│   │   │   │   ├── repository/FlashcardRepository.java
│   │   │   │   └── service/FlashcardService.java
│   │   │   ├── progress/                         # Module tiến trình học
│   │   │   │   ├── entity/{UserCardProgress,CardProgressStatus}.java
│   │   │   │   └── repository/UserCardProgressRepository.java
│   │   │   ├── studysession/                     # Module phiên học
│   │   │   │   ├── entity/{StudySession,StudySessionCard,StudyMode,
│   │   │   │   │           StudySessionStatus,ReviewRating}.java
│   │   │   │   └── repository/{StudySession,StudySessionCard}Repository.java
│   │   │   └── common/                           # Các thành phần dùng chung
│   │   │       ├── config/SecurityConfig.java
│   │   │       ├── entity/BaseEntity.java
│   │   │       ├── exception/{GlobalExceptionHandler,ResourceNotFoundException}.java
│   │   │       └── security/{CustomUserDetailsService,JwtAuthenticationFilter,JwtService}.java
│   │   └── resources/
│   │       ├── application.yaml
│   │       └── db/migration/
│   │           └── V1__make_flashcards_front_text_nullable.sql
│   └── test/
├── docs/
│   ├── architecture.md                           # (file này)
│   └── api-response-samples.json
├── AUTH_FLASHCARD_TESTING.md
├── AUTH_USER_TESTING.md
└── pom.xml
```

---

## 3. Kiến trúc phân lớp (Layered Architecture)

Dự án áp dụng mô hình **Vertical Slice / Module-based** kết hợp **Layered Architecture** theo từng domain:

```
┌─────────────────────────────────────────────┐
│               Client / Frontend              │
└──────────────────────┬──────────────────────┘
                       │ HTTP (REST)
┌──────────────────────▼──────────────────────┐
│           Controller Layer (@RestController)  │
│  AuthController │ UserController │ ...       │
└──────────────────────┬──────────────────────┘
                       │
┌──────────────────────▼──────────────────────┐
│            Service Layer (@Service)          │
│  AuthService │ UserService │ FlashcardService│
└──────────────────────┬──────────────────────┘
                       │
┌──────────────────────▼──────────────────────┐
│         Repository Layer (JpaRepository)     │
│  UserRepository │ FlashcardRepository │ ...  │
└──────────────────────┬──────────────────────┘
                       │
┌──────────────────────▼──────────────────────┐
│              MySQL Database                   │
└─────────────────────────────────────────────┘
```

---

## 4. Mô hình dữ liệu (Domain Model)

### BaseEntity (lớp cha chung)

| Trường       | Kiểu             | Mô tả                           |
|--------------|------------------|---------------------------------|
| `id`         | Long (PK)        | Auto-increment                  |
| `createdAt`  | LocalDateTime    | Tự động khi tạo (Hibernate)     |
| `updatedAt`  | LocalDateTime    | Tự động khi cập nhật (Hibernate)|

### users

| Trường          | Kiểu     | Ràng buộc                  | Mô tả                         |
|-----------------|----------|----------------------------|-------------------------------|
| `id`            | Long     | PK, auto-increment         | ID người dùng                 |
| `full_name`     | VARCHAR(100) | NOT NULL              | Họ và tên                     |
| `email`         | VARCHAR(150) | NOT NULL, UNIQUE       | Email (dùng để đăng nhập)     |
| `password_hash` | VARCHAR(255) | NOT NULL              | Mật khẩu mã hoá (BCrypt)     |
| `role`          | ENUM     | NOT NULL, default: USER    | `USER` hoặc `ADMIN`           |
| `active`        | Boolean  | NOT NULL, default: true    | Trạng thái kích hoạt          |
| `daily_goal`    | Integer  | NOT NULL, default: 10      | Mục tiêu học thẻ mỗi ngày     |
| `current_streak`| Integer  | NOT NULL, default: 0       | Chuỗi ngày học liên tiếp      |

### flashcard_sets

| Trường            | Kiểu          | Ràng buộc              | Mô tả                         |
|-------------------|---------------|------------------------|-------------------------------|
| `id`              | Long          | PK                     |                               |
| `user_id`         | Long (FK)     | NOT NULL → users       | Chủ sở hữu bộ thẻ            |
| `title`           | VARCHAR(150)  | NOT NULL               | Tên bộ flashcard              |
| `description`     | TEXT          | nullable               | Mô tả                         |
| `source_language` | VARCHAR(30)   | NOT NULL, default: en  | Ngôn ngữ gốc                  |
| `target_language` | VARCHAR(30)   | NOT NULL, default: vi  | Ngôn ngữ đích                 |
| `archived`        | Boolean       | NOT NULL, default: false | Đã lưu trữ chưa             |

### flashcards

| Trường              | Kiểu          | Ràng buộc                      | Mô tả                       |
|---------------------|---------------|--------------------------------|-----------------------------|
| `id`                | Long          | PK                             |                             |
| `flashcard_set_id`  | Long (FK)     | NOT NULL → flashcard_sets      | Thuộc bộ nào                |
| `front_content_type`| ENUM          | NOT NULL, default: TEXT        | `TEXT` hoặc `IMAGE`         |
| `front_text`        | VARCHAR(500)  | nullable                       | Nội dung mặt trước (nếu TEXT)|
| `front_image_url`   | VARCHAR(500)  | nullable                       | URL ảnh mặt trước (nếu IMAGE)|
| `back_text`         | VARCHAR(500)  | NOT NULL                       | Nội dung mặt sau            |
| `example_text`      | TEXT          | nullable                       | Câu ví dụ                   |
| `note_text`         | TEXT          | nullable                       | Ghi chú                     |
| `order_index`       | Integer       | NOT NULL, default: 0           | Thứ tự trong bộ             |

### user_card_progress

| Trường            | Kiểu          | Ràng buộc                             | Mô tả                          |
|-------------------|---------------|---------------------------------------|--------------------------------|
| `id`              | Long          | PK                                    |                                |
| `user_id`         | Long (FK)     | NOT NULL, UNIQUE(user, flashcard)     | Người dùng                     |
| `flashcard_id`    | Long (FK)     | NOT NULL, UNIQUE(user, flashcard)     | Thẻ học                        |
| `status`          | ENUM          | default: NEW                          | `NEW`, `LEARNING`, `MASTERED`  |
| `last_reviewed_at`| LocalDateTime | nullable                              | Lần ôn tập gần nhất            |
| `next_review_at`  | LocalDateTime | nullable                              | Lịch ôn tập tiếp theo (SRS)   |
| `review_count`    | Integer       | default: 0                            | Số lần đã ôn                   |
| `correct_streak`  | Integer       | default: 0                            | Chuỗi trả lời đúng liên tiếp  |
| `interval_days`   | Integer       | default: 0                            | Khoảng cách ôn (ngày) - SRS   |
| `easiness_factor` | DECIMAL(4,2)  | default: 2.50                         | Hệ số dễ - SRS (SM-2 style)   |
| `lapse_count`     | Integer       | default: 0                            | Số lần quên sau khi đã học     |

### study_sessions

| Trường            | Kiểu          | Ràng buộc              | Mô tả                        |
|-------------------|---------------|------------------------|------------------------------|
| `id`              | Long          | PK                     |                              |
| `user_id`         | Long (FK)     | NOT NULL → users       |                              |
| `flashcard_set_id`| Long (FK)     | NOT NULL               | Bộ thẻ đang học              |
| `mode`            | ENUM          | NOT NULL               | `FLASHCARD`, `QUIZ`, ...     |
| `status`          | ENUM          | default: IN_PROGRESS   | `IN_PROGRESS`, `COMPLETED`   |
| `started_at`      | LocalDateTime | NOT NULL               | Thời điểm bắt đầu phiên      |
| `ended_at`        | LocalDateTime | nullable               | Thời điểm kết thúc phiên     |
| `total_cards`     | Integer       | default: 0             | Tổng số thẻ trong phiên      |
| `reviewed_cards`  | Integer       | default: 0             | Đã ôn bao nhiêu thẻ          |
| `again_count`     | Integer       | default: 0             | Số thẻ đánh giá AGAIN        |
| `hard_count`      | Integer       | default: 0             | Số thẻ đánh giá HARD         |
| `good_count`      | Integer       | default: 0             | Số thẻ đánh giá GOOD         |
| `easy_count`      | Integer       | default: 0             | Số thẻ đánh giá EASY         |

### study_session_cards

| Trường              | Kiểu          | Ràng buộc                     | Mô tả                     |
|---------------------|---------------|-------------------------------|---------------------------|
| `id`                | Long          | PK                            |                           |
| `study_session_id`  | Long (FK)     | NOT NULL → study_sessions     |                           |
| `flashcard_id`      | Long (FK)     | NOT NULL → flashcards         |                           |
| `order_index`       | Integer       | default: 0                    | Thứ tự trong phiên        |
| `rating`            | ENUM          | NOT NULL                      | `AGAIN`, `HARD`, `GOOD`, `EASY` |
| `reviewed_at`       | LocalDateTime | NOT NULL                      | Thời điểm ôn thẻ này      |

---

## 5. Sơ đồ quan hệ thực thể (ERD)

```
users
 ├──< flashcard_sets (user_id)
 │      └──< flashcards (flashcard_set_id)
 ├──< study_sessions (user_id)
 │      ├── flashcard_sets (flashcard_set_id)
 │      └──< study_session_cards (study_session_id)
 │               └── flashcards (flashcard_id)
 └──< user_card_progress (user_id)
          └── flashcards (flashcard_id)
```

---

## 6. API Endpoints

### 6.1 Xác thực — `/api/auth` (public)

| Method | Endpoint              | Body                                    | Response       | Mô tả          |
|--------|-----------------------|-----------------------------------------|----------------|----------------|
| POST   | `/api/auth/register`  | `{fullName, email, password}`           | `AuthResponse` | Đăng ký tài khoản |
| POST   | `/api/auth/login`     | `{email, password}`                     | `AuthResponse` | Đăng nhập       |

**AuthResponse**: `{ token: string, ... }`

---

### 6.2 Người dùng — `/api/users` (cần JWT)

| Method | Endpoint        | Body                  | Response       | Mô tả                      |
|--------|-----------------|-----------------------|----------------|-----------------------------|
| GET    | `/api/users/me` | —                     | `UserResponse` | Lấy thông tin bản thân      |
| PUT    | `/api/users/me` | `UpdateProfileRequest`| `UserResponse` | Cập nhật hồ sơ cá nhân     |

---

### 6.3 Bộ Flashcard — `/api/flashcard-sets` (cần JWT)

| Method | Endpoint                    | Body                    | Response                  | Mô tả                          |
|--------|-----------------------------|-------------------------|---------------------------|--------------------------------|
| GET    | `/api/flashcard-sets`       | —                       | `List<FlashcardSetResponse>` | Lấy tất cả bộ thẻ của tôi   |
| GET    | `/api/flashcard-sets/{id}`  | —                       | `FlashcardSetResponse`    | Lấy một bộ thẻ cụ thể         |
| POST   | `/api/flashcard-sets`       | `FlashcardSetRequest`   | `FlashcardSetResponse`    | Tạo bộ thẻ mới                |
| PUT    | `/api/flashcard-sets/{id}`  | `FlashcardSetRequest`   | `FlashcardSetResponse`    | Cập nhật bộ thẻ               |
| DELETE | `/api/flashcard-sets/{id}`  | —                       | 204 No Content            | Xoá bộ thẻ                    |

---

### 6.4 Flashcard — `/api/flashcard-sets/{setId}/flashcards` (cần JWT)

| Method | Endpoint                                           | Body               | Response                  | Mô tả                   |
|--------|----------------------------------------------------|--------------------|---------------------------|-------------------------|
| GET    | `/api/flashcard-sets/{setId}/flashcards`           | —                  | `List<FlashcardResponse>` | Lấy tất cả thẻ trong bộ |
| GET    | `/api/flashcard-sets/{setId}/flashcards/{id}`      | —                  | `FlashcardResponse`       | Lấy một thẻ             |
| POST   | `/api/flashcard-sets/{setId}/flashcards`           | `FlashcardRequest` | `FlashcardResponse`       | Tạo thẻ mới             |
| PUT    | `/api/flashcard-sets/{setId}/flashcards/{id}`      | `FlashcardRequest` | `FlashcardResponse`       | Cập nhật thẻ            |
| DELETE | `/api/flashcard-sets/{setId}/flashcards/{id}`      | —                  | 204 No Content            | Xoá thẻ                 |

**FlashcardRequest validation:**
- `frontContentType`: `TEXT` (default) hoặc `IMAGE`
- Nếu TEXT → bắt buộc `frontText`
- Nếu IMAGE → bắt buộc `frontImageUrl`
- `backText`: bắt buộc

---

## 7. Cơ chế Bảo mật

### JWT Authentication Flow

```
Client                          Server
  │                               │
  │── POST /api/auth/login ───────►│
  │   {email, password}           │ 1. Xác thực credentials
  │                               │ 2. Tạo JWT (HS256, 24h)
  │◄── {token} ──────────────────│
  │                               │
  │── GET /api/... ───────────────►│
  │   Authorization: Bearer <JWT>  │ 3. JwtAuthenticationFilter
  │                               │    - Parse & validate JWT
  │                               │    - Load UserDetails
  │                               │    - Set SecurityContext
  │◄── Response ─────────────────│
```

### SecurityConfig

- **CSRF**: Disabled (Stateless API)
- **Session**: STATELESS
- **Public routes**: `POST /api/auth/**`
- **Protected routes**: Tất cả còn lại (yêu cầu JWT hợp lệ)
- **Password Encoder**: BCrypt
- **CORS**: Cho phép `http://localhost:3000` (có thể cấu hình qua env)

### JWT Config (application.yaml)

```yaml
app:
  security:
    jwt-secret: ${JWT_SECRET}     # Cấu hình qua biến môi trường
    jwt-expiration-ms: 86400000   # 24 giờ (mặc định)
```

---

## 8. Database Migration (Flyway)

| Version | File                                            | Nội dung                                        |
|---------|-------------------------------------------------|-------------------------------------------------|
| V1      | `V1__make_flashcards_front_text_nullable.sql`   | Chuyển cột `front_text` sang nullable (hỗ trợ IMAGE card) |

Flyway được cấu hình:
- `baseline-on-migrate: true` — hỗ trợ DB đã tồn tại trước
- `baseline-version: 0`
- Location: `classpath:db/migration`

---

## 9. Cấu hình ứng dụng (`application.yaml`)

```yaml
spring:
  datasource:
    url: jdbc:mysql://${DB_HOST:localhost}:${DB_PORT:3306}/${DB_NAME:vocaflip}
    username: ${DB_USERNAME:root}
    password: ${DB_PASSWORD:123456}
  jpa:
    hibernate.ddl-auto: update
    show-sql: true
  flyway:
    enabled: true

app:
  cors:
    allowed-origins: http://localhost:3000
  security:
    jwt-secret: ${JWT_SECRET:change-this-secret-key-...}
    jwt-expiration-ms: ${JWT_EXPIRATION_MS:86400000}
```

### Biến môi trường (Production)

| Biến               | Mô tả                    | Mặc định            |
|--------------------|--------------------------|---------------------|
| `DB_HOST`          | Host MySQL               | `localhost`         |
| `DB_PORT`          | Port MySQL               | `3306`              |
| `DB_NAME`          | Tên database             | `vocaflip`          |
| `DB_USERNAME`      | Username DB              | `root`              |
| `DB_PASSWORD`      | Password DB              | `123456`            |
| `JWT_SECRET`       | Khóa ký JWT              | *(nên thay đổi)*    |
| `JWT_EXPIRATION_MS`| Thời gian hết hạn JWT    | `86400000` (24h)    |

---

## 10. Spaced Repetition System (SRS)

`UserCardProgress` lưu trạng thái học theo từng thẻ, thiết kế theo thuật toán **SM-2**:

| Trường            | Ý nghĩa SRS                                |
|-------------------|--------------------------------------------|
| `status`          | `NEW` → `LEARNING` → `MASTERED`            |
| `interval_days`   | Số ngày trước lần ôn tiếp theo             |
| `easiness_factor` | Hệ số điều chỉnh interval (mặc định 2.5)  |
| `next_review_at`  | Timestamp lần ôn tiếp theo                 |
| `correct_streak`  | Chuỗi đúng liên tiếp (tăng interval)       |
| `lapse_count`     | Số lần quên (reset về LEARNING)            |

Đánh giá trong `StudySession`:
- **AGAIN** — Quên hoàn toàn, reset interval
- **HARD** — Khó, tăng interval chậm
- **GOOD** — Ôn tốt, tăng interval bình thường
- **EASY** — Rất dễ, tăng interval nhanh

---

## 11. Hướng dẫn chạy dự án

### Yêu cầu
- Java 21+
- Maven (hoặc dùng `./mvnw`)
- MySQL 8.0+

### Bước 1: Chuẩn bị Database

```sql
CREATE DATABASE vocaflip;
```

### Bước 2: Cấu hình biến môi trường

```bash
# Windows PowerShell
$env:DB_USERNAME = "root"
$env:DB_PASSWORD = "your_password"
$env:JWT_SECRET = "your-very-long-secret-key-at-least-32-chars"
```

### Bước 3: Khởi chạy

```bash
./mvnw spring-boot:run
```

Hoặc:

```bash
./mvnw clean package -DskipTests
java -jar target/vocaflip-0.0.1-SNAPSHOT.jar
```

Server chạy tại: `http://localhost:8080`

### Bước 4: Kiểm tra

```bash
# Đăng ký
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"fullName":"Test User","email":"test@example.com","password":"password123"}'

# Đăng nhập
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"password123"}'
```

---

## 12. Module chưa có Controller (Work in Progress)

Các module sau đã có Entity và Repository nhưng **chưa có Controller/Service**:

| Module           | Files hiện có                                       | Thiếu                        |
|------------------|-----------------------------------------------------|------------------------------|
| `progress`       | `UserCardProgress`, `UserCardProgressRepository`    | Service, Controller, DTOs    |
| `studysession`   | `StudySession`, `StudySessionCard`, Repositories    | Service, Controller, DTOs    |

Đây là các tính năng dự kiến triển khai tiếp theo để hoàn thiện luồng học SRS.
