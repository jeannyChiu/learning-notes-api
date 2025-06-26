# 📘 線上學習筆記平台 API

本專案是一個使用 Spring Boot 開發的線上學習筆記平台後端 API，  
提供使用者一個可以隨時做筆記，並且方便查詢過往筆記的服務。

---

## 🔧 功能列表

- 使用者註冊與登入（JWT 驗證機制）
- 新增筆記（POST /notes）
- 查詢所有筆記或單筆筆記（GET /notes、GET /notes/{id}）
- 更新筆記（PUT /notes/{id}）
- 刪除筆記（DELETE /notes/{id}）
- 請求日誌自動寫入資料庫（AOP 實作）

---

## 🛠 使用技術

- Java 17
- Spring Boot 3.x
- Spring Security
- Spring Data JPA
- Spring AOP
- JWT (JSON Web Token)
- Lombok
- MySQL 8
- IntelliJ IDEA（開發工具）
- Git + GitHub（版本控制）

---

## 🏗️ 專案架構

本專案遵循標準的 Spring Boot 專案結構，採分層設計模式，主要套件與其職責如下：

```
src/main/java/com/jeannychiu/learningnotesapi/
├── aspect         // AOP 切面，用於日誌紀錄
├── config         // 應用程式組態
├── controller     // API 端點控制器
├── dto            // 資料傳輸物件
├── exception      // 自定義例外處理
├── model          // 資料庫實體
├── repository     // 資料存取層
├── security       // Spring Security 安全性組態
├── service        // 商業邏輯層
└── LearningNotesApiApplication.java // Spring Boot 啟動類別
```

---

## 🚀 使用說明

1. **安裝與啟動**
   - 確保已安裝 Java 17 與 MySQL 8
   - 建立資料庫 `learning_notes`
   - 複製專案並修改 `application.properties` 設定資料庫連線
   - 使用 IDE 或下列指令啟動：
     ```bash
     ./mvnw spring-boot:run
     ```
   - 預設服務埠為 `http://localhost:8080/`

2. **資料表建立**
   - 若使用 JPA 的自動建表（如 `spring.jpa.hibernate.ddl-auto=update`），會自動建立所需 table
   - 若需手動建表，請參考 `/resources` 下 SQL 檔或根據實體設計自建

3. **API 測試工具**
   - 建議使用 [Postman](https://www.postman.com/) 或 curl 等 API 測試工具

---

## 📬 API 測試範例（使用 Postman）

### ➤ 使用者註冊

- **Method**：POST
- **URL**：`http://localhost:8080/register`
- **Header**：`Content-Type: application/json`
- **Body**：

    ```json
    {
      "email": "user@example.com",
      "password": "password123"
    }
    ```

---

### ➤ 使用者登入

- **Method**：POST
- **URL**：`http://localhost:8080/login`
- **Header**：`Content-Type: application/json`
- **Body**：

    ```json
    {
      "email": "user@example.com",
      "password": "password123"
    }
    ```
- **說明**：成功登入會回傳 JWT Token，請於下方 API 帶入。

---

### ➤ 使用 JWT Token 認證

除註冊與登入外，其餘 API 需在 Header 加入：
`Authorization: Bearer {登入取得的 token}`

---

### ➤ 新增筆記

- **Method**：POST
- **URL**：`http://localhost:8080/notes`
- **Header**：
  - `Content-Type: application/json`
  - `Authorization: Bearer {token}`
- **Body**：

  ```json
  {
    "title": "學習 Spring Boot",
    "content": "這是我的第一筆筆記"
  }
  ```

---

### ➤ 查詢所有筆記

- **Method**：GET
- **URL**：`http://localhost:8080/notes`
- **Header**：`Authorization: Bearer {token}`
---

### ➤ 查詢特定筆記

- **Method**：GET
- **URL**：`http://localhost:8080/notes/{id}`
- **範例**：`http://localhost:8080/notes/1`
- **Header**：`Authorization: Bearer {token}`

---

### ➤ 更新筆記

- **Method**：PUT
- **URL**：`http://localhost:8080/notes/1`
- **Header**：
    - `Content-Type: application/json`
    - `Authorization: Bearer {token}`
- **Body**：
    ```json
    {
      "title": "更新後的標題",
      "content": "更新後的內容"
    }
    ```

---

### ➤ 刪除筆記

- **Method**：DELETE
- **URL**：`http://localhost:8080/notes/1`
- **Header**：`Authorization: Bearer {token}`

---

### ➤ 常見錯誤回應
- `401 Unauthorized`：未登入或 Token 無效/過期
- `403 Forbidden`：已登入但無操作權限
- `404 Not Found`：資源不存在
- `400 Bad Request`：參數錯誤

---

### 🗒️ 日誌紀錄與查詢
- 所有 API 請求與回應（包含狀態碼、請求內容等）會自動寫入資料庫（如 api_log 表）。
- 可用資料庫查詢驗證日誌寫入，例如：
    ```sql
      SELECT * FROM api_log ORDER BY id DESC LIMIT 10;
    ```
- 若部分 response 無法序列化，log 內容會顯示 Failed to parse response body 屬正常現象。
---

## ✅ 專案狀態

目前已完成核心的筆記 CRUD API，並整合 Spring Security 實現了使用者註冊、登入及 JWT 驗證機制。專案採用分層架構，並包含統一的例外處理與日誌紀錄功能。

後續可擴充功能包含：
- 更細緻的權限控制（例如：使用者只能存取自己的筆記）
- API 文件（Swagger / OpenAPI）
- 單元測試與整合測試
- 前端整合（如 Vue/React）
- 管理員功能

---

## 👩‍💻 作者

Jeanny Chiu  
GitHub: [@jeannyChiu](https://github.com/jeannyChiu)
