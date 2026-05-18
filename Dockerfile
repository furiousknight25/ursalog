# Use the official Microsoft Playwright Java image
FROM mcr.microsoft.com/playwright/java:v1.40.0-jammy

# Set the initial working directory
WORKDIR /app

# Copy your project files into the container
COPY . .

# Run the Kobweb export command safely
RUN ./gradlew clean kobwebExport

# Expose the port Kobweb runs on
EXPOSE 8080

# MOVE into the sub-folder where Kobweb actually built your project
WORKDIR /app/site

# Tell the server how to start the app
CMD ["java", "-jar", ".kobweb/server/server.jar"]