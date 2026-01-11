# GitHub Proxy API

A modern, high-performance **Proxy Service** built with **Spring Boot 4.0.1** and **Java 25**. This application acts as an intelligent facade for the GitHub REST API, streamlining the process of fetching, filtering, and transforming user repository data.

## 🎯 Project Purpose

The service is designed to solve the problem of data fragmentation in the GitHub API. Instead of making multiple manual calls and filtering data on the client side, this **Proxy API** aggregates everything into a single, clean response.

**Core Proxy Logic:**
* **Data Aggregation:** Merges basic repository info with detailed branch and commit data in one round-trip.
* **Smart Filtering:** Automatically excludes all **fork** repositories.
* **Error Translation:** Intercepts upstream errors and maps them to a consistent, client-friendly format.
* **Compliance:** Enforces strict header requirements (e.g., `Accept: application/json`).

## 🏗 System Architecture

The application acts as a specialized middleware. Below is the simplified flow of a single request:



1. **Client** requests data via our versioned endpoint.
2. **Proxy** communicates with GitHub API to fetch repositories.
3. **Proxy** filters forks and concurrently enriches the remaining data with branch/commit details.
4. **Proxy** returns a unified, transformed JSON list to the **Client**.

## 🛠 Tech Stack

* **Java 25:** Utilizing the latest language features like Records and Pattern Matching for clean data models.
* **Spring Boot 4.0.1:** Latest generation of the Spring framework.
* **Spring RestClient:** Modern, synchronous HTTP client for robust external communication.
* **WireMock:** Full API simulation for reliable, offline integration testing.
* **JUnit 5 & AssertJ:** For fluent and expressive test assertions.

## 📋 API Specification

### Get Enriched Repositories
Returns a list of non-fork repositories with their branches and latest commit SHAs.

* **URL:** `/api/v1/github/{username}/repos`
* **Method:** `GET`
* **Example:** `http://localhost:8080/api/v1/github/pioter3214/repos`
* **Required Header:** `Accept: application/json`

#### Success Response (200 OK):
```json
[
  {
    "name": "GitRepoAPI",
    "owner": {
      "login": "pioter3214"
    },
    "branches": [
      {
        "name": "master",
        "commit": {
          "sha": "a1b2c3d4e5f6g7h8i9j0"
        }
      }
    ]
  }
]
```
## ⚠️ Error Handling
The proxy translates upstream GitHub issues and validation errors into a standardized format.
* Scenario: User Not Found (404) If the requested GitHub username does not exist:
```json
{
    "status": 404,
    "message": "User not found"
}
```
* Scenario: Not Acceptable (406) If the Accept header is missing or is not application/json:
```json
{
    "status": 406,
    "message": "Not Acceptable"
}
```

## ⚙️ Setup & Execution
### Prerequisites
* JDK 25
* Gradle

### Installation & Running
1. Clone the repo:
```bash
git clone [https://github.com/pioter3214/GitRepoAPI.git](https://github.com/pioter3214/GitRepoAPI.git)
```
2. Build the application:
```bash
./gradlew build
```
3. Run the server:
```bash
./gradlew bootRun
```
### Running Tests
The project features a robust test suite that uses WireMock to simulate GitHub's API behavior.
```bash
./gradlew test
```
