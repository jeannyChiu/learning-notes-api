# ====== Build stage: 用 Maven 打包 Jar，並跳過測試 ======
FROM maven:3.9.8-eclipse-temurin-17 AS build
WORKDIR /workspace

# 先只拷貝 pom，加速相依快取
COPY pom.xml .
RUN mvn -B -q -DskipTests dependency:go-offline

# 再拷貝原始碼並打包（跳過測試與測試編譯）
COPY src ./src
RUN mvn -B -q clean package -Dmaven.test.skip=true

# ====== Run stage: 用精簡 JRE 跑起來 ======
FROM eclipse-temurin:17-jre
WORKDIR /app

# 複製剛剛 build 出來的 Jar（萬用字元以防版本/檔名不同）
COPY --from=build /workspace/target/*.jar /app/app.jar

# 預設 prod，可依需要改
ENV SPRING_PROFILES_ACTIVE=prod

# Cloud Run 會給 $PORT；本地預設 8080
CMD ["sh","-c","java -Dserver.port=${PORT:-8080} -jar /app/app.jar"]
