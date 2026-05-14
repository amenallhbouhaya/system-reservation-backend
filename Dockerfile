# Stage 1: Build (nasta3mlou maven m3a amazoncorretto)
FROM maven:3.8.6-amazoncorretto-17 AS build
COPY . .
RUN mvn clean package -DskipTests

# Stage 2: Run (nasta3mlou amazoncorretto alpine bech tkoun sghira w khfifa)
FROM amazoncorretto:17-alpine
COPY --from=build /target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app.jar"]