# Use the official Microsoft Playwright Java image
# This includes JDK 17 AND all the Linux OS dependencies needed to run headless browsers
FROM mcr.microsoft.com/playwright/java:v1.40.0-jammy

# Set the working directory inside the server
WORKDIR /app

# Copy your project files into the container
COPY . .

# Run the Kobweb export command safely
RUN ./gradlew clean kobwebExport

# Expose the port Kobweb runs on
EXPOSE 8080

# Tell the server how to start the app
CMD ["java", "-jar", ".kobweb/server/kobweb-server.jar"]