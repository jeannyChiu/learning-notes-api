# 📘 線上學習筆記平台 API

本專案是一個使用 Spring Boot 開發的線上學習筆記平台後端 API，  
提供使用者一個可以隨時做筆記，並且方便查詢過往筆記的服務。

---

## 🔧 功能列表

- 新增筆記（POST /notes）
- 查詢所有筆記或單筆筆記（GET /notes、GET /notes/{id}）
- 更新筆記（PUT /notes/{id}）
- 刪除筆記（DELETE /notes/{id}）

---

## 🛠 使用技術

- Java 17
- Spring Boot 3.x
- Spring Data JPA
- MySQL 8
- IntelliJ IDEA（開發工具）
- Git + GitHub（版本控制）

---

## 🚀 使用說明

1. 確保已安裝 Java 17 與 Spring Boot 開發環境
2. 安裝 MySQL，並建立資料庫 `learning_notes`
3. 修改 `application.properties`，設定資料庫帳號密碼與連線資訊
4. 啟動 `LearningNotesApiApplication.java`
5. 可透過 Postman 或其他 API 工具測試以下 CRUD API

---

## 📬 API 測試範例（使用 Postman）

### ➤ 新增筆記

- **Method**：POST
- **URL**：`http://localhost:8080/notes`
- **Header**：`Content-Type: application/json`
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

---

### ➤ 查詢特定筆記

- **Method**：GET
- **URL**：`http://localhost:8080/notes/{id}`
- **範例**：`http://localhost:8080/notes/1`

---

### ➤ 更新筆記

- **Method**：PUT
- **URL**：`http://localhost:8080/notes/1`
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

---

## ✅ 專案狀態

目前已完成 CRUD API 功能，並實作 ResponseEntity 狀態回傳機制。  
後續可擴充功能包含：使用者登入/註冊、權限驗證（Spring Security）、前端整合等。

---

## 👩‍💻 作者

Jeanny Chiu  
GitHub: [@jeannyChiu](https://github.com/jeannyChiu)
