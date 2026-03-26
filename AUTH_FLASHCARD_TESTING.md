# Auth + Flashcard API Testing Guide

File này dùng để test luồng hiện tại của project:

- `Auth`
- `User`
- `Flashcard Set`
- `Flashcard`

Luồng test khuyến nghị:

1. đăng ký user A
2. đăng nhập user A
3. lấy profile user A
4. tạo flashcard set cho user A
5. tạo flashcard text
6. tạo flashcard image
7. đọc danh sách set và card
8. update set và card
9. đăng ký user B
10. dùng user B thử truy cập dữ liệu của user A để test ownership

## 1. Chuẩn bị

### 1.1. Environment variables

PowerShell:

```powershell
$env:DB_HOST="localhost"
$env:DB_PORT="3306"
$env:DB_NAME="vocaflip"
$env:DB_USERNAME="root"
$env:DB_PASSWORD="123456"
$env:JWT_SECRET="this-is-a-very-long-secret-key-for-local-dev-123456789"
$env:JWT_EXPIRATION_MS="86400000"
```

### 1.2. Base URL

```text
http://localhost:8080
```

### 1.3. Chạy app

Chạy app trong IDE hoặc Maven. Nếu app lên thành công thì:

- `POST /api/auth/**` được mở
- các endpoint còn lại cần `Authorization: Bearer <token>`

## 2. Endpoint hiện có

### Auth

- `POST /api/auth/register`
- `POST /api/auth/login`

### User

- `GET /api/users/me`
- `PUT /api/users/me`

### Flashcard Set

- `GET /api/flashcard-sets`
- `GET /api/flashcard-sets/{id}`
- `POST /api/flashcard-sets`
- `PUT /api/flashcard-sets/{id}`
- `DELETE /api/flashcard-sets/{id}`

### Flashcard

- `GET /api/flashcard-sets/{setId}/flashcards`
- `GET /api/flashcard-sets/{setId}/flashcards/{id}`
- `POST /api/flashcard-sets/{setId}/flashcards`
- `PUT /api/flashcard-sets/{setId}/flashcards/{id}`
- `DELETE /api/flashcard-sets/{setId}/flashcards/{id}`

## 3. Test user A

### 3.1. Register user A

`POST /api/auth/register`

```json
{
  "fullName": "User A",
  "email": "usera@example.com",
  "password": "123456"
}
```

Kỳ vọng:

- status `201`
- response có `accessToken`
- response có object `user`

Lưu lại:

- `USER_A_TOKEN`
- `USER_A_ID`

### 3.2. Login user A

`POST /api/auth/login`

```json
{
  "email": "usera@example.com",
  "password": "123456"
}
```

Kỳ vọng:

- status `200`
- nhận `accessToken` hợp lệ

### 3.3. Get current user

`GET /api/users/me`

Header:

```text
Authorization: Bearer USER_A_TOKEN
```

Kỳ vọng:

- status `200`
- `email = usera@example.com`

### 3.4. Update current user

`PUT /api/users/me`

Header:

```text
Authorization: Bearer USER_A_TOKEN
Content-Type: application/json
```

Body:

```json
{
  "fullName": "User A Updated",
  "dailyGoal": 20
}
```

Kỳ vọng:

- status `200`
- `fullName = User A Updated`
- `dailyGoal = 20`

## 4. Test flashcard set với user A

### 4.1. Create flashcard set

`POST /api/flashcard-sets`

Header:

```text
Authorization: Bearer USER_A_TOKEN
Content-Type: application/json
```

Body:

```json
{
  "title": "Animals",
  "description": "Basic animal vocabulary",
  "sourceLanguage": "en",
  "targetLanguage": "vi"
}
```

Kỳ vọng:

- status `201`
- response có `id`
- `cardCount = 0`

Lưu lại:

- `SET_ID`

### 4.2. Get all sets

`GET /api/flashcard-sets`

Header:

```text
Authorization: Bearer USER_A_TOKEN
```

Kỳ vọng:

- status `200`
- thấy set `Animals`

### 4.3. Get set by id

`GET /api/flashcard-sets/{SET_ID}`

Header:

```text
Authorization: Bearer USER_A_TOKEN
```

Kỳ vọng:

- status `200`
- đúng dữ liệu của set vừa tạo

### 4.4. Update set

`PUT /api/flashcard-sets/{SET_ID}`

Body:

```json
{
  "title": "Animals Updated",
  "description": "Updated description",
  "sourceLanguage": "en",
  "targetLanguage": "vi"
}
```

Kỳ vọng:

- status `200`
- `title = Animals Updated`

## 5. Test flashcard với user A

### 5.1. Create text flashcard

`POST /api/flashcard-sets/{SET_ID}/flashcards`

Header:

```text
Authorization: Bearer USER_A_TOKEN
Content-Type: application/json
```

Body:

```json
{
  "frontContentType": "TEXT",
  "frontText": "cat",
  "backText": "con mèo",
  "exampleText": "The cat is sleeping.",
  "noteText": "Common noun"
}
```

Kỳ vọng:

- status `201`
- `frontContentType = TEXT`
- `frontText = cat`
- `frontImageUrl = null`

Lưu lại:

- `CARD_TEXT_ID`

### 5.2. Create image flashcard

`POST /api/flashcard-sets/{SET_ID}/flashcards`

Body:

```json
{
  "frontContentType": "IMAGE",
  "frontImageUrl": "https://example.com/images/dog.png",
  "backText": "dog"
}
```

Kỳ vọng:

- status `201`
- `frontContentType = IMAGE`
- `frontImageUrl` có giá trị
- `frontText = null`

Lưu lại:

- `CARD_IMAGE_ID`

### 5.3. Get all flashcards in set

`GET /api/flashcard-sets/{SET_ID}/flashcards`

Kỳ vọng:

- status `200`
- có ít nhất 2 card

### 5.4. Get flashcard by id

`GET /api/flashcard-sets/{SET_ID}/flashcards/{CARD_TEXT_ID}`

Kỳ vọng:

- status `200`
- trả đúng card text

### 5.5. Update flashcard

`PUT /api/flashcard-sets/{SET_ID}/flashcards/{CARD_TEXT_ID}`

Body:

```json
{
  "frontContentType": "TEXT",
  "frontText": "kitten",
  "backText": "mèo con",
  "exampleText": "The kitten is cute.",
  "noteText": "Updated note"
}
```

Kỳ vọng:

- status `200`
- `frontText = kitten`
- `backText = mèo con`

## 6. Test validation

### 6.1. Create image flashcard nhưng thiếu `frontImageUrl`

```json
{
  "frontContentType": "IMAGE",
  "backText": "dog"
}
```

Kỳ vọng:

- status `400`
- validation fail

### 6.2. Create text flashcard nhưng thiếu `frontText`

```json
{
  "frontContentType": "TEXT",
  "backText": "con mèo"
}
```

Kỳ vọng:

- status `400`

### 6.3. Tạo flashcard thiếu `backText`

```json
{
  "frontContentType": "TEXT",
  "frontText": "cat"
}
```

Kỳ vọng:

- status `400`

### 6.4. Tạo flashcard set thiếu `title`

```json
{
  "description": "No title"
}
```

Kỳ vọng:

- status `400`

## 7. Test ownership với user B

### 7.1. Register user B

`POST /api/auth/register`

```json
{
  "fullName": "User B",
  "email": "userb@example.com",
  "password": "123456"
}
```

Lưu lại:

- `USER_B_TOKEN`

### 7.2. User B đọc set của user A

`GET /api/flashcard-sets/{SET_ID}`

Header:

```text
Authorization: Bearer USER_B_TOKEN
```

Kỳ vọng:

- status `404`

### 7.3. User B đọc card của user A

`GET /api/flashcard-sets/{SET_ID}/flashcards/{CARD_TEXT_ID}`

Kỳ vọng:

- status `404`

### 7.4. User B sửa set của user A

`PUT /api/flashcard-sets/{SET_ID}`

Kỳ vọng:

- status `404`

### 7.5. User B xóa card của user A

`DELETE /api/flashcard-sets/{SET_ID}/flashcards/{CARD_TEXT_ID}`

Kỳ vọng:

- status `404`

Nếu các case này bị `200`, nghĩa là ownership check vẫn còn lỗi.

## 8. Test unauthorized

### 8.1. Không có token

Gọi:

- `GET /api/users/me`
- `GET /api/flashcard-sets`

Kỳ vọng:

- bị chặn bởi security
- không được trả dữ liệu

### 8.2. Token sai

Header:

```text
Authorization: Bearer invalid-token
```

Kỳ vọng:

- request không được authenticate

## 9. Test delete

### 9.1. Delete flashcard

`DELETE /api/flashcard-sets/{SET_ID}/flashcards/{CARD_IMAGE_ID}`

Kỳ vọng:

- status `204`

### 9.2. Delete flashcard set

`DELETE /api/flashcard-sets/{SET_ID}`

Kỳ vọng:

- status `204`

Ghi chú:

- nếu database chưa cấu hình cascade delete, xóa set khi vẫn còn card có thể fail do foreign key
- nếu gặp lỗi này, hãy xóa hết card trước rồi mới xóa set

## 10. Bộ request mẫu nhanh

### Register

```json
{
  "fullName": "User A",
  "email": "usera@example.com",
  "password": "123456"
}
```

### Login

```json
{
  "email": "usera@example.com",
  "password": "123456"
}
```

### Create set

```json
{
  "title": "Animals",
  "description": "Basic animal vocabulary",
  "sourceLanguage": "en",
  "targetLanguage": "vi"
}
```

### Create text card

```json
{
  "frontContentType": "TEXT",
  "frontText": "cat",
  "backText": "con mèo",
  "exampleText": "The cat is sleeping.",
  "noteText": "Common noun"
}
```

### Create image card

```json
{
  "frontContentType": "IMAGE",
  "frontImageUrl": "https://example.com/images/dog.png",
  "backText": "dog"
}
```

## 11. Quick checklist

Nếu bạn muốn test nhanh nhất, chỉ cần chạy 7 bước này:

1. register user A
2. login user A
3. create set
4. create text card
5. create image card
6. get all sets
7. get all flashcards in set

Nếu cả 7 bước pass thì merge hiện tại cơ bản đã ổn cho:

- auth
- user hiện tại
- ownership theo user
- flashcard set CRUD cơ bản
- flashcard CRUD cơ bản
- front type `TEXT/IMAGE`
