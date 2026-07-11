# ====================== STAGE 1: BUILD ======================
FROM maven:3.9.9-eclipse-temurin-21-alpine AS builder

WORKDIR /app

# Copia apenas o pom.xml primeiro (para cachear as dependências)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copia o código fonte
COPY src ./src

# Compila a aplicação (skip tests para build mais rápido no Docker)
RUN mvn clean package -DskipTests

# ====================== STAGE 2: RUNTIME ======================
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Cria usuário não-root por segurança
RUN addgroup -S spring && adduser -S spring -G spring
USER spring

# Copia o JAR gerado do stage anterior
COPY --from=builder /app/target/*.jar app.jar

# Expõe a porta
EXPOSE 8080

# Variáveis de ambiente úteis
ENV JAVA_OPTS="\
    -Xms512m \
    -Xmx1024m \
    -XX:+UseG1GC \
    -Djava.security.egd=file:/dev/./urandom"

# Comando de inicialização
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]