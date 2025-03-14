# Rates Service

## Overview
This project is a Spring Boot application written in Kotlin that fetches and compares exchange rates from different APIs.

## Features
- Fetches exchange rates from the Czech National Bank (CNB) and Frankfurter API.
- Compares exchange rates and provides the difference.
- Exposes REST endpoints to retrieve supported currency pairs and comparison rates.
- Provides API documentation using OpenAPI.
- Supports both JSON and XML responses.
- Integrates with Google Authentication for secure access.
- Provides build in health check endpoint.

## Requirements
- Java 17 or higher
- Kotlin 1.5 or higher
- Gradle 6.8 or higher

## Setup
1. Clone the repository:
   ```sh
   git clone https://github.com/pepaproch/rates.git
   cd rates
   ```

2. Build the project:
   ```sh
   ./gradlew build
   ```

3. Run the application:
   ```sh
   ./gradlew bootRun
   ```

## Endpoints
- **Get Supported Pairs**
    - **URL:** `/api/pairs`
    - **Method:** GET
    - **Produces:** `application/json`, `application/xml`
    - **Description:** Returns a list of supported currency pairs.

- **Get Comparison Rate**
    - **URL:** `/api/FRANKFURTER/{pair}`
    - **Method:** GET
    - **Produces:** `application/json`, `application/xml`
    - **Description:** Returns the comparison rate for the specified currency pair.


- **Get Health**
    - **URL:** `/actuator/health`
    - **Method:** GET
    - **Produces:** `application/json`
    - **Description:** Returns the health status of the application.


## API Documentation
API documentation and SwaggerUi is available at `/doc`.

## License
This project is licensed under the MIT License.