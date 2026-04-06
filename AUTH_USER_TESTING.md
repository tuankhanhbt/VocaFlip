# Auth + User Testing Guide

File này dùng để test nhanh phần `Auth` và `User` của project `vocaflip`.

## 1. Phạm vi hiện tại

Các API đã có:

- `POST /api/auth/register`
- `POST /api/auth/login`
- `GET /api/users/me`
- `PUT /api/users/me`

Luồng test chuẩn:

1. đăng ký user mới
2. đăng nhập lấy JWT
3. gọi `/api/users/me`
4. cập nhật profile bằng `/api/users/me`
5. đăng nhập lại nếu muốn kiểm tra dữ liệu đã đổi

## 2. Chuẩn bị trước khi test

### 2.1. Kiểm tra MySQL

Đảm bảo MySQL đang chạy và app trỏ đúng DB.

`src/main/resources/application.yaml` đang đọc:

- `DB_HOST`
- `DB_PORT`
- `DB_NAME`
- `DB_USERNAME`
- `DB_PASSWORD`
- `JWT_SECRET`
- `JWT_EXPIRATION_MS`

Ví dụ set env bằng PowerShell:

```powershell
$env:DB_HOST="localhost"
$env:DB_PORT="3306"
$env:DB_NAME="vocaflip"
$env:DB_USERNAME="root"
$env:DB_PASSWORD="123456"
$env:JWT_SECRET="this-is-a-very-long-secret-key-for-local-dev-123456789"
$env:JWT_EXPIRATION_MS="86400000"
```

Ghi chú:

- `JWT_SECRET` nên dài ít nhất khoảng 32 ký tự để ký token ổn định
- app mặc định chạy ở `http://localhost:8080`

### 2.2. Chạy ứng dụng

Chạy app bằng IDE hoặc Maven.

Khi app lên thành công:

- bảng `users` sẽ được tạo nếu chưa có
- security sẽ mở `POST /api/auth/**`
- các endpoint khác cần `Bearer token`

## 3. Cấu trúc response hiện tại

### 3.1. Auth response

`register` và `login` trả về:

```json
{
  "accessToken": "jwt-token-here",
  "tokenType": "Bearer",
  "user": {
    "id": 1,
    "fullName": "Nguyen Van A",
    "email": "a@example.com",
    "role": "USER",
    "active": true,
    "dailyGoal": 10,
    "currentStreak": 0,
    "createdAt": "2026-03-26T20:00:00",
    "updatedAt": "2026-03-26T20:00:00"
  }
}
```

### 3.2. User response

`GET /api/users/me` và `PUT /api/users/me` trả về:

```json
{
  "id": 1,
  "fullName": "Nguyen Van A",
  "email": "a@example.com",
  "role": "USER",
  "active": true,
  "dailyGoal": 10,
  "currentStreak": 0,
  "createdAt": "2026-03-26T20:00:00",
  "updatedAt": "2026-03-26T20:05:00"
}
```

## 4. Test bằng Postman

### 4.1. Register

Method: `POST`

URL:

```text
http://localhost:8080/api/auth/register
```

Body:

```json
{
  "fullName": "Nguyen Van A",
  "email": "a@example.com",
  "password": "123456"
}
```

Kỳ vọng:

- status `201 Created`
- response có `accessToken`
- `user.email` được convert về lowercase
- password không xuất hiện trong response

### 4.2. Login

Method: `POST`

URL:

```text
http://localhost:8080/api/auth/login
```

Body:

```json
{
  "email": "a@example.com",
  "password": "123456"
}
```

Kỳ vọng:

- status `200 OK`
- response có `accessToken`
- copy `accessToken` để dùng cho các request tiếp theo

### 4.3. Get current user

Method: `GET`

URL:

```text
http://localhost:8080/api/users/me
```

Header:

```text
Authorization: Bearer <accessToken>
```

Kỳ vọng:

- status `200 OK`
- trả về đúng user vừa login

### 4.4. Update current user

Method: `PUT`

URL:

```text
http://localhost:8080/api/users/me
```

Header:

```text
Authorization: Bearer <accessToken>
Content-Type: application/json
```

Body:

```json
{
  "fullName": "Nguyen Van B",
  "dailyGoal": 20
}
```

Kỳ vọng:

- status `200 OK`
- `fullName` đổi thành `Nguyen Van B`
- `dailyGoal` đổi thành `20`

## 5. Test bằng curl

### 5.1. Register

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d "{\"fullName\":\"Nguyen Van A\",\"email\":\"a@example.com\",\"password\":\"123456\"}"
```

### 5.2. Login

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d "{\"email\":\"a@example.com\",\"password\":\"123456\"}"
```

Sau khi login, copy `accessToken`.

### 5.3. Get me

```bash
curl http://localhost:8080/api/users/me \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"
```

### 5.4. Update me

```bash
curl -X PUT http://localhost:8080/api/users/me \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d "{\"fullName\":\"Nguyen Van B\",\"dailyGoal\":20}"
```

## 6. Các case nên test thêm

### 6.1. Register email trùng

Request:

```json
{
  "fullName": "Another User",
  "email": "a@example.com",
  "password": "123456"
}
```

Kỳ vọng:

- status `409 Conflict`
- message: `Email already exists`

### 6.2. Login sai password

Request:

```json
{
  "email": "a@example.com",
  "password": "wrong-password"
}
```

Kỳ vọng:

- status `401 Unauthorized`
- message: `Invalid email or password`

### 6.3. Gọi `/api/users/me` không có token

Kỳ vọng:

- bị chặn bởi Spring Security
- status thường là `403` hoặc response security mặc định tùy runtime config hiện tại

### 6.4. Update profile với `dailyGoal` không hợp lệ

Request:

```json
{
  "dailyGoal": 0
}
```

Kỳ vọng:

- validation fail
- status `400 Bad Request`

### 6.5. Update profile với `fullName` rỗng

Request:

```json
{
  "fullName": "   "
}
```

Kỳ vọng:

- service sẽ chặn
- status `400 Bad Request`
- message: `Full name must not be blank`

## 7. Dữ liệu được lưu như thế nào

### 7.1. Password

Password không lưu plain text.

App đang dùng `BCryptPasswordEncoder`, nên trong DB cột `password_hash` sẽ là chuỗi đã hash.

### 7.2. Email

Email được normalize:

- `trim()`
- `toLowerCase()`

Ví dụ:

`"  A@Example.com  "` sẽ được lưu thành:

```text
a@example.com
```

## 8. Nếu gặp lỗi

### 8.1. `401 Unauthorized` khi gọi `/api/users/me`

Kiểm tra:

- header có đúng `Authorization: Bearer <token>` không
- token lấy từ `register/login` mới nhất không
- `JWT_SECRET` có bị đổi sau khi đã login không

### 8.2. App không lên được vì DB

Kiểm tra:

- MySQL có đang chạy không
- username/password đúng không
- database `vocaflip` có tồn tại không
- port `3306` có đúng không

### 8.3. Token lỗi ngay khi login thành công

Kiểm tra:

- `JWT_SECRET` đủ dài chưa
- không dùng secret quá ngắn

Ví dụ secret local ổn:

```text
this-is-a-very-long-secret-key-for-local-dev-123456789
```

## 9. Gợi ý test nhanh nhất

Nếu chỉ muốn kiểm tra auth đã hoạt động chưa, test đúng 3 request này:

1. `POST /api/auth/register`
2. `POST /api/auth/login`
3. `GET /api/users/me`

Nếu 3 bước này pass thì:

- password encode đang ổn
- JWT generate đang ổn
- JWT filter đang ổn
- security config đang ổn
- current user lookup đang ổn

## 10. Endpoint summary

### `POST /api/auth/register`

Request:

```json
{
  "fullName": "Nguyen Van A",
  "email": "a@example.com",
  "password": "123456"
}
```

### `POST /api/auth/login`

Request:

```json
{
  "email": "a@example.com",
  "password": "123456"
}
```

### `GET /api/users/me`

Header:

```text
Authorization: Bearer <token>
```

### `PUT /api/users/me`

Header:

```text
Authorization: Bearer <token>
```

Request:

```json
{
  "fullName": "Nguyen Van B",
  "dailyGoal": 20
}
```
