# Learning Notes Platform

一個簡單的線上學習筆記平台，展示 **Java / Spring Boot 開發能力**，並具備雲端部署與安全考量。  
此專案為面試作品，聚焦於 **登入驗證、筆記 CRUD、API 日誌、雲端部署** 等功能。

---

## Live Demo

[Demo 網址](https://learning-notes-app-926274779343.asia-east1.run.app)

測試帳號：
- Email: `demo@example-test.com`
- Password: `Demo!2025`

> 建議使用測試帳號直接登入，免自行註冊。  
> 前端為簡易 React 介面（主要由 AI 輔助生成，僅作為後端功能展示）
> 若遇到 401 Unauthorized，請重新登入（JWT Token 有效期 1 小時）

---

## 功能特點

- 使用者註冊 / 登入（支援 Google OAuth2）
- JWT 驗證機制，無狀態 API 請求
- 筆記 CRUD（支援分頁、標籤與關鍵字搜尋）
- API 呼叫紀錄（AOP 寫入 api_log table）
- 雲端部署：**Cloud Run + Cloud SQL**
- 秘密管理：**GOOGLE_CLIENT_SECRET、JWT_SECRET 透過 Secret Manager 注入**

---

## 技術棧

- **語言**：Java 17
- **後端**：Spring Boot 3.4.4, Spring Security, Spring Data JPA
- **驗證**：JWT, OAuth2 Google Login
- **資料庫**：MySQL 8 (Cloud SQL)
- **雲端**：Google Cloud Run, Secret Manager
- **其他**：Spring AOP, API Logging, GitHub 版本控管

---

## 安全實務

- JWT 秘鑰由 **Secret Manager** 管理，Cloud Run 啟動時自動注入 `JWT_SECRET`
- 前端登入 Token 儲存於 `localStorage`（Demo 用途）

---

## API 文件

- 雲端環境：[https://learning-notes-app-926274779343.asia-east1.run.app/swagger-ui.html](https://learning-notes-app-926274779343.asia-east1.run.app/swagger-ui.html)

---

## 專案亮點

- 採用 Spring Boot 架構，整合 Spring Security 與 OAuth2 Google 登入
- 雲端部署於 Google Cloud Run，資料庫使用 Cloud SQL
- 秘密管理採用 Secret Manager，避免敏感資訊硬編碼
- 提供完整的 JWT 驗證流程與 API 日誌功能
- 系統具備筆記 CRUD、分頁與標籤及關鍵字搜尋等常見應用場景
- Demo 網站與測試帳號可直接體驗

