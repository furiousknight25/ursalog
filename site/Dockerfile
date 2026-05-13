# Use a standard Java environment to run the Kotlin server
FROM eclipse-temurin:17-jre

# Set the working directory inside the server
WORKDIR /app

# Copy your project files into the container
COPY . .

# Run the Kobweb export command to compile the frontend and backend for production
# We use the gradle wrapper included in your project
RUN ./gradlew kobwebExport --layout fullstack

# Expose the port Kobweb runs on
EXPOSE 8080

# Tell the server how to start the app
CMD ["java", "-jar", ".kobweb/server/kobweb-server.jar"]