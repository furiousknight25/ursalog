# Use a standard Java Development Kit (JDK) to compile the Kotlin code
FROM eclipse-temurin:17-jdk

# Set the working directory inside the server
WORKDIR /app

# Copy your project files into the container
COPY . .

# Run the Kobweb export command to compile the frontend and backend for production
# Added "clean" for safety, and removed the invalid --layout flag!
RUN ./gradlew clean kobwebExport

# Expose the port Kobweb runs on
EXPOSE 8080

# Tell the server how to start the app
CMD ["java", "-jar", ".kobweb/server/kobweb-server.jar"]