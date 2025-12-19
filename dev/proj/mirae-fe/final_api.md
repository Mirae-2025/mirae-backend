# Mirae Backend API Specification

Complete API documentation for frontend integration with the Mirae backend server.

---

## Table of Contents

1. [Server Information](#1-server-information)
2. [Authentication](#2-authentication)
3. [Common Response Formats](#3-common-response-formats)
4. [API Endpoints](#4-api-endpoints)
   - [4.1 Authentication APIs](#41-authentication-apis)
   - [4.2 Speedrun Game APIs](#42-speedrun-game-apis)
   - [4.3 Recommendation APIs](#43-recommendation-apis)
   - [4.4 Analysis APIs](#44-analysis-apis)
   - [4.5 Growth Analysis APIs](#45-growth-analysis-apis)
   - [4.6 AI Speedrun APIs](#46-ai-speedrun-apis)
   - [4.7 Health Check APIs](#47-health-check-apis)
5. [Data Models](#5-data-models)
6. [Error Handling](#6-error-handling)
7. [WebSocket Endpoints](#7-websocket-endpoints)
8. [Quick Reference](#8-quick-reference)

---

## 1. Server Information

| Item | Value |
|------|-------|
| **Base URL** | `http://localhost:8080` |
| **Framework** | Spring Boot 3.5.7 |
| **Protocol** | HTTP/REST |
| **Content-Type** | `application/json` |
| **Timezone** | `Asia/Seoul` |
| **Swagger UI** | `http://localhost:8080/practice-ui.html` |
| **OpenAPI JSON** | `http://localhost:8080/api-docs/json` |

### Required Services

| Service | Host | Port |
|---------|------|------|
| Backend Server | localhost | 8080 |
| MySQL Database | localhost | 3306 |
| Redis | localhost | 6379 |
| AI Server (optional) | localhost | 8000 |

---

## 2. Authentication

### Authentication Method

This API uses **JWT (JSON Web Token)** based authentication.

### Token Types

| Token Type | Validity | Usage |
|------------|----------|-------|
| Access Token | 20 minutes (1,200,000 ms) | API requests |
| Refresh Token | 7 days (604,800,000 ms) | Obtain new access token |

### How to Authenticate

1. **Login** to obtain tokens via `POST /api/auth/login`
2. **Include the access token** in the `Authorization` header:
   ```
   Authorization: Bearer <access_token>
   ```
3. **For speedrun endpoints**, include the user ID in the `X-User-Id` header:
   ```
   X-User-Id: <user_id>
   ```

### Token Refresh Flow

When the access token expires:
1. Call `POST /api/auth/refresh` with the refresh token
2. Receive a new access token
3. Continue using the new access token

---

## 3. Common Response Formats

### Success Response

```json
{
  "field1": "value1",
  "field2": "value2"
}
```

### Error Response

```json
{
  "code": "ERROR_CODE",
  "message": "Human readable error message",
  "timestamp": "2024-12-19T10:30:00"
}
```

### Validation Error Response

```json
{
  "code": "VALIDATION_ERROR",
  "message": "Validation failed",
  "errors": {
    "fieldName": "Error message for this field"
  },
  "timestamp": "2024-12-19T10:30:00"
}
```

---

## 4. API Endpoints

---

### 4.1 Authentication APIs

Base Path: `/api/auth`

#### 4.1.1 User Signup

Register a new user.

```
POST /api/auth/signup
```

**Request Headers:**
| Header | Required | Description |
|--------|----------|-------------|
| Content-Type | Yes | `application/json` |

**Request Body:**
```json
{
  "email": "user@example.com",
  "password": "password123",
  "baekjoonId": "baekjoon_handle"
}
```

| Field | Type | Required | Validation | Description |
|-------|------|----------|------------|-------------|
| email | string | Yes | Valid email format | User email |
| password | string | Yes | Min 8 characters | User password |
| baekjoonId | string | Yes | Not blank | Baekjoon Online Judge handle |

**Response:** `201 Created`
```json
{
  "uuid": "550e8400-e29b-41d4-a716-446655440000",
  "email": "user@example.com",
  "message": "User registered successfully"
}
```

**Error Responses:**
| Status | Code | Description |
|--------|------|-------------|
| 400 | VALIDATION_ERROR | Invalid input |
| 409 | EMAIL_EXISTS | Email already registered |

---

#### 4.1.2 User Login

Authenticate user and obtain JWT tokens.

```
POST /api/auth/login
```

**Request Body:**
```json
{
  "email": "user@example.com",
  "password": "password123"
}
```

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| email | string | Yes | User email |
| password | string | Yes | User password |

**Response:** `200 OK`
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer",
  "expiresIn": 1200000,
  "uuid": "550e8400-e29b-41d4-a716-446655440000",
  "email": "user@example.com"
}
```

| Field | Type | Description |
|-------|------|-------------|
| accessToken | string | JWT access token for API requests |
| refreshToken | string | JWT refresh token for obtaining new access tokens |
| tokenType | string | Always "Bearer" |
| expiresIn | number | Access token validity in milliseconds |
| uuid | string | User's unique identifier |
| email | string | User's email |

**Error Responses:**
| Status | Code | Description |
|--------|------|-------------|
| 401 | INVALID_CREDENTIALS | Wrong email or password |
| 404 | USER_NOT_FOUND | User not found |

---

#### 4.1.3 Refresh Token

Get a new access token using refresh token.

```
POST /api/auth/refresh
```

**Request Body:**
```json
{
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**Response:** `200 OK`
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer",
  "expiresIn": 1200000
}
```

**Error Responses:**
| Status | Code | Description |
|--------|------|-------------|
| 401 | INVALID_TOKEN | Invalid or expired refresh token |

---

### 4.2 Speedrun Game APIs

Base Path: `/api/speedrun`

These APIs manage the multiplayer speedrun game sessions with Redis-based real-time features.

#### Common Headers for Speedrun APIs

| Header | Required | Description |
|--------|----------|-------------|
| X-User-Id | Yes | User's numeric ID (Long) |
| Content-Type | Yes (POST) | `application/json` |

---

#### 4.2.1 Create Classic Mode Session

Create a new Classic mode speedrun session.

```
POST /api/speedrun/classic/session
```

**Request Headers:**
```
X-User-Id: 1
Content-Type: application/json
```

**Request Body:**
```json
{
  "duration": 30,
  "maxPlayers": 10,
  "difficultyRange": "silver",
  "tags": null
}
```

| Field | Type | Required | Default | Validation | Description |
|-------|------|----------|---------|------------|-------------|
| duration | integer | Yes | - | 5-120 | Game duration in minutes |
| maxPlayers | integer | No | 10 | 1-100 | Maximum players allowed |
| difficultyRange | string | No | null | bronze/silver/gold/platinum/all | Problem difficulty filter |
| tags | string[] | No | null | - | Algorithm tags (for TagFocus mode) |

**Response:** `201 Created`
```json
{
  "sessionId": "550e8400-e29b-41d4-a716-446655440000",
  "mode": "CLASSIC",
  "state": "WAITING",
  "duration": 30,
  "maxPlayers": 10,
  "currentPlayers": 1,
  "createdBy": 1,
  "startedAt": null,
  "endedAt": null,
  "difficultyRange": "silver",
  "tags": null
}
```

---

#### 4.2.2 Create TagFocus Mode Session

```
POST /api/speedrun/tagfocus/session
```

Same as Classic mode, but `tags` field is used for filtering problems.

**Request Body Example:**
```json
{
  "duration": 30,
  "maxPlayers": 5,
  "difficultyRange": "gold",
  "tags": ["dp", "graph", "greedy"]
}
```

---

#### 4.2.3 Create Retry Mode Session

```
POST /api/speedrun/retry/session
```

Same as Classic mode. Retry mode includes streak bonus multipliers.

---

#### 4.2.4 Get Session Info

Get session details.

```
GET /api/speedrun/session/{sessionId}
```

**Path Parameters:**
| Parameter | Type | Description |
|-----------|------|-------------|
| sessionId | string | Session UUID |

**Response:** `200 OK`
```json
{
  "sessionId": "550e8400-e29b-41d4-a716-446655440000",
  "mode": "CLASSIC",
  "state": "WAITING",
  "duration": 30,
  "maxPlayers": 10,
  "currentPlayers": 3,
  "createdBy": 1,
  "startedAt": null,
  "endedAt": null,
  "difficultyRange": "silver",
  "tags": null
}
```

| Field | Type | Description |
|-------|------|-------------|
| sessionId | string | Session UUID |
| mode | string | CLASSIC, TAGFOCUS, or RETRY |
| state | string | WAITING, RUNNING, or ENDED |
| duration | integer | Game duration in minutes |
| maxPlayers | integer | Maximum players allowed |
| currentPlayers | integer | Current player count |
| createdBy | number | User ID of session creator |
| startedAt | number | Start timestamp (epoch ms), null if not started |
| endedAt | number | End timestamp (epoch ms), null if not ended |
| difficultyRange | string | Difficulty filter |
| tags | string[] | Algorithm tags filter |

---

#### 4.2.5 Get Session State

Get current session state with remaining time.

```
GET /api/speedrun/session/{sessionId}/state
```

**Request Headers:**
```
X-User-Id: 1
```

**Response:** `200 OK`
```json
{
  "sessionId": "550e8400-e29b-41d4-a716-446655440000",
  "state": "RUNNING",
  "remainingTimeMs": 1500000,
  "timestamp": 1703001234567
}
```

---

#### 4.2.6 Join Session

Join an existing session.

```
POST /api/speedrun/session/{sessionId}/join
```

**Request Headers:**
```
X-User-Id: 2
```

**Response:** `200 OK` - Returns SessionResponse

**Error Responses:**
| Status | Code | Description |
|--------|------|-------------|
| 404 | SESSION_NOT_FOUND | Session doesn't exist |
| 400 | SESSION_NOT_RUNNING | Session is not in WAITING state |
| 403 | SESSION_FULL | Session has reached max players |
| 409 | ALREADY_JOINED | User already in session |

---

#### 4.2.7 Start Session

Start the session (owner only).

```
POST /api/speedrun/session/{sessionId}/start
```

**Request Headers:**
```
X-User-Id: 1
```

**Response:** `200 OK` - Returns SessionResponse with `state: "RUNNING"`

**Error Responses:**
| Status | Code | Description |
|--------|------|-------------|
| 403 | NOT_SESSION_OWNER | Only session owner can start |
| 400 | SESSION_NOT_RUNNING | Session is not in WAITING state |

---

#### 4.2.8 Leave Session

Leave the current session.

```
POST /api/speedrun/session/{sessionId}/leave
```

**Request Headers:**
```
X-User-Id: 2
```

**Response:** `200 OK` - Returns SessionResponse

---

#### 4.2.9 Submit Problem

Submit a solved problem.

```
POST /api/speedrun/session/{sessionId}/submit
```

**Request Headers:**
```
X-User-Id: 1
Content-Type: application/json
```

**Request Body:**
```json
{
  "problemId": 1000,
  "tier": 5
}
```

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| problemId | number | Yes | Baekjoon problem ID |
| tier | integer | No | solved.ac tier (0-30) for score calculation |

**Response:** `200 OK`
```json
{
  "success": true,
  "problemId": 1000,
  "scoreEarned": 25,
  "totalScore": 125,
  "currentRank": 2,
  "solvedCount": 5,
  "streak": 3,
  "message": "Problem submitted successfully"
}
```

**Error Responses:**
| Status | Code | Description |
|--------|------|-------------|
| 400 | SESSION_NOT_RUNNING | Session is not running |
| 400 | USER_NOT_IN_SESSION | User not in this session |
| 400 | SESSION_EXPIRED | Session time has ended |
| 409 | DUPLICATE_SUBMIT | Problem already submitted |

---

#### 4.2.10 Get My Status

Get current user's status in session.

```
GET /api/speedrun/session/{sessionId}/my-status
```

**Request Headers:**
```
X-User-Id: 1
```

**Response:** `200 OK`
```json
{
  "userId": 1,
  "score": 125,
  "solvedCount": 5,
  "wrongCount": 1,
  "streak": 3,
  "currentRank": 2,
  "lastSubmitAt": 1703001234567,
  "solvedProblems": [1000, 1001, 1002, 1003, 1004]
}
```

---

#### 4.2.11 Get Session Leaderboard

Get session leaderboard.

```
GET /api/speedrun/session/{sessionId}/leaderboard
```

**Query Parameters:**
| Parameter | Type | Required | Default | Description |
|-----------|------|----------|---------|-------------|
| limit | integer | No | 10 | Number of entries to return |

**Response:** `200 OK`
```json
[
  {
    "rank": 1,
    "userId": 3,
    "username": "player3",
    "score": 200,
    "solvedCount": 8,
    "streak": 5
  },
  {
    "rank": 2,
    "userId": 1,
    "username": "player1",
    "score": 125,
    "solvedCount": 5,
    "streak": 3
  }
]
```

---

#### 4.2.12 Get Global Ranking

```
GET /api/speedrun/ranking/global
```

**Query Parameters:**
| Parameter | Type | Required | Default | Description |
|-----------|------|----------|---------|-------------|
| limit | integer | No | 100 | Number of entries |

**Response:** `200 OK`
```json
{
  "type": "global",
  "rankings": [
    {
      "rank": 1,
      "userId": 5,
      "username": "topPlayer",
      "score": 15000,
      "solvedCount": 500,
      "streak": null
    }
  ],
  "totalParticipants": 1234
}
```

---

#### 4.2.13 Get Weekly Ranking

```
GET /api/speedrun/ranking/weekly
```

Same response format as global ranking with `"type": "weekly"`.

---

#### 4.2.14 Get Monthly Ranking

```
GET /api/speedrun/ranking/monthly
```

Same response format as global ranking with `"type": "monthly"`.

---

#### 4.2.15 Get My Ranking

```
GET /api/speedrun/ranking/my
```

**Request Headers:**
```
X-User-Id: 1
```

**Query Parameters:**
| Parameter | Type | Required | Default | Description |
|-----------|------|----------|---------|-------------|
| type | string | No | "global" | global, weekly, or monthly |

**Response:** `200 OK`
```json
{
  "rank": 42,
  "userId": 1,
  "username": null,
  "score": 5000,
  "solvedCount": null,
  "streak": null
}
```

---

#### 4.2.16 Get Game History

Get user's game history.

```
GET /api/speedrun/history
```

**Request Headers:**
```
X-User-Id: 1
```

**Query Parameters:**
| Parameter | Type | Required | Default | Description |
|-----------|------|----------|---------|-------------|
| page | integer | No | 0 | Page number (0-indexed) |
| size | integer | No | 20 | Page size |

**Response:** `200 OK`
```json
{
  "content": [
    {
      "id": 1,
      "sessionId": "550e8400-e29b-41d4-a716-446655440000",
      "userId": 1,
      "finalScore": 250,
      "solvedCount": 10,
      "wrongCount": 2,
      "finalRank": 1,
      "maxStreak": 5,
      "solvedProblems": "1000,1001,1002",
      "createdAt": "2024-12-19T10:30:00"
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 20
  },
  "totalElements": 50,
  "totalPages": 3
}
```

---

#### 4.2.17 Get User Stats

Get user's overall statistics.

```
GET /api/speedrun/stats
```

**Request Headers:**
```
X-User-Id: 1
```

**Response:** `200 OK`
```json
{
  "userId": 1,
  "totalGames": 50,
  "averageScore": 185.5,
  "totalProblemsSolved": 423,
  "maxStreak": 12
}
```

---

### 4.3 Recommendation APIs

Base Path: `/api/recommend`

AI-powered problem recommendation APIs (requires AI server running on port 8000).

---

#### 4.3.1 Get TF-IDF Recommendations

Default content-based filtering recommendations.

```
GET /api/recommend
```

**Query Parameters:**
| Parameter | Type | Required | Default | Description |
|-----------|------|----------|---------|-------------|
| user_id | string | Yes | - | User ID |
| k | integer | No | 5 | Number of recommendations (1-50) |
| exclude_solved | boolean | No | true | Exclude already solved problems |
| difficulty_min | string | No | null | Min difficulty: bronze/silver/gold/platinum/diamond/ruby |
| difficulty_max | string | No | null | Max difficulty filter |

**Example:**
```
GET /api/recommend?user_id=1&k=5&difficulty_min=silver&difficulty_max=gold
```

**Response:** `200 OK`
```json
{
  "user_id": "1",
  "k": 5,
  "items": [
    {
      "problem_id": 1000,
      "title": "A+B",
      "difficulty": "bronze",
      "accuracy": 0.85,
      "score": 0.8523,
      "reason": "이전 풀이와 유사한 키워드: math, implementation"
    },
    {
      "problem_id": 1001,
      "title": "A-B",
      "difficulty": "bronze",
      "accuracy": 0.88,
      "score": 0.8234,
      "reason": "이전 풀이와 유사한 키워드: math"
    }
  ]
}
```

---

#### 4.3.2 Get Popularity Recommendations

Recommendations based on problem accuracy/popularity.

```
GET /api/recommend/popularity
```

Same parameters and response format as TF-IDF recommendations.

---

#### 4.3.3 Get Random Recommendations

Random problem selection (A/B testing baseline).

```
GET /api/recommend/random
```

Same parameters and response format as TF-IDF recommendations.

---

#### 4.3.4 Get Hybrid Recommendations

Combined TF-IDF (70%) + Popularity (30%) recommendations.

```
GET /api/recommend/hybrid
```

Same parameters and response format as TF-IDF recommendations.

---

#### 4.3.5 Get Weakness-based Recommendations

Recommendations targeting user's weak areas.

```
GET /api/recommend/weakness
```

Same parameters and response format as TF-IDF recommendations.

---

#### 4.3.6 Batch Recommendations

Get recommendations for multiple users at once.

```
POST /api/recommend/batch
```

**Query Parameters:**
| Parameter | Type | Required | Default | Description |
|-----------|------|----------|---------|-------------|
| strategy | string | No | "tfidf" | tfidf, popularity, random, hybrid, weakness |

**Request Body:**
```json
{
  "user_ids": ["1", "2", "3"],
  "k": 5,
  "exclude_solved": true,
  "difficulty_min": "silver",
  "difficulty_max": "gold"
}
```

**Response:** `200 OK`
```json
{
  "results": {
    "1": [
      {
        "problem_id": 1000,
        "title": "A+B",
        "difficulty": "bronze",
        "accuracy": 0.85,
        "score": 0.8523,
        "reason": "..."
      }
    ],
    "2": [...],
    "3": [...]
  },
  "total_users": 3,
  "strategy": "tfidf"
}
```

---

### 4.4 Analysis APIs

Base Path: `/api/analysis`

---

#### 4.4.1 Weakness Analysis

Analyze user's weak tags and failure patterns.

```
GET /api/analysis/weakness
```

**Query Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| user_id | string | Yes | User ID |

**Response:** `200 OK`
```json
{
  "user_id": "1",
  "total_attempts": 50,
  "total_failures": 15,
  "weak_tags": [
    {
      "tag": "dp",
      "success_rate": 40.0,
      "failures": 6,
      "total_attempts": 10
    },
    {
      "tag": "greedy",
      "success_rate": 50.0,
      "failures": 3,
      "total_attempts": 6
    }
  ],
  "tag_stats": {
    "dp": {
      "total_attempts": 10,
      "successes": 4,
      "failures": 6,
      "success_rate": 40.0
    },
    "implementation": {
      "total_attempts": 20,
      "successes": 18,
      "failures": 2,
      "success_rate": 90.0
    }
  },
  "recent_failures": [
    {
      "problem_id": 1234,
      "title": "Problem Title",
      "tags": ["dp", "greedy"],
      "difficulty": "gold",
      "verdict": "WA"
    }
  ]
}
```

---

### 4.5 Growth Analysis APIs

Base Path: `/api/growth`

---

#### 4.5.1 Growth Report

Comprehensive growth analysis report.

```
GET /api/growth/report
```

**Query Parameters:**
| Parameter | Type | Required | Default | Description |
|-----------|------|----------|---------|-------------|
| user_id | string | Yes | - | User ID |
| days | integer | No | 30 | Analysis period (7-365) |

**Response:** `200 OK`
```json
{
  "user_id": "1",
  "period_days": 30,
  "total_attempts": 100,
  "total_solved": 75,
  "overall_accuracy": 75.0,
  "weak_tags": [
    {
      "tag": "dp",
      "total": 10,
      "solved": 4,
      "accuracy": 40.0
    }
  ],
  "strong_tags": [
    {
      "tag": "implementation",
      "total": 20,
      "solved": 19,
      "accuracy": 95.0
    }
  ],
  "difficulty_stats": [
    {
      "difficulty": "bronze",
      "total": 30,
      "solved": 28,
      "accuracy": 93.3
    },
    {
      "difficulty": "silver",
      "total": 40,
      "solved": 30,
      "accuracy": 75.0
    }
  ],
  "weekly_progress": [
    {
      "week": "2024-W01",
      "total_attempts": 25,
      "total_solved": 18,
      "accuracy": 72.0
    },
    {
      "week": "2024-W02",
      "total_attempts": 30,
      "total_solved": 25,
      "accuracy": 83.3
    }
  ]
}
```

---

#### 4.5.2 Accuracy Trend

Weekly accuracy change analysis.

```
GET /api/growth/accuracy-trend
```

**Query Parameters:**
| Parameter | Type | Required | Default | Description |
|-----------|------|----------|---------|-------------|
| user_id | string | Yes | - | User ID |
| days | integer | No | 30 | Analysis period (7-365) |

**Response:** `200 OK`
```json
{
  "user_id": "1",
  "period_days": 30,
  "weekly_trend": [
    {
      "week": "2024-W01",
      "total_attempts": 25,
      "total_solved": 18,
      "accuracy": 72.0
    },
    {
      "week": "2024-W02",
      "total_attempts": 30,
      "total_solved": 25,
      "accuracy": 83.3
    }
  ],
  "improvement": 11.3
}
```

---

#### 4.5.3 Tag Analysis

Detailed tag-based analysis.

```
GET /api/growth/tags
```

**Query Parameters:**
| Parameter | Type | Required | Default | Description |
|-----------|------|----------|---------|-------------|
| user_id | string | Yes | - | User ID |
| days | integer | No | 30 | Analysis period (7-365) |

**Response:** `200 OK`
```json
{
  "user_id": "1",
  "total_tags": 15,
  "tag_stats": [
    {
      "tag": "dp",
      "total": 10,
      "solved": 4,
      "accuracy": 40.0
    }
  ],
  "most_attempted": [
    {
      "tag": "implementation",
      "total": 25,
      "solved": 20,
      "accuracy": 80.0
    }
  ],
  "needs_improvement": [
    {
      "tag": "dp",
      "total": 10,
      "solved": 4,
      "accuracy": 40.0
    }
  ]
}
```

---

### 4.6 AI Speedrun APIs

Base Path: `/api/ai-speedrun`

AI server-managed speedrun sessions (requires AI server on port 8000).

---

#### 4.6.1 Create AI Speedrun Session

Create a new AI-managed speedrun session.

> **Note:** Only available at :00 or :30 minutes (e.g., 14:00, 14:30)

```
POST /api/ai-speedrun/create
```

**Request Body:**
```json
{
  "user_id": "1",
  "mode": "30min",
  "difficulty": "bronze"
}
```

| Field | Type | Required | Validation | Description |
|-------|------|----------|------------|-------------|
| user_id | string | Yes | Not blank | User ID |
| mode | string | Yes | 30min/60min/90min/120min | Game duration mode |
| difficulty | string | Yes | bronze/silver/gold/platinum | Problem difficulty |

**Mode Details:**
| Mode | Duration | Problems | Target |
|------|----------|----------|--------|
| 30min | 30 minutes | 5 | Beginner |
| 60min | 60 minutes | 10 | Beginner |
| 90min | 90 minutes | 4 | Advanced |
| 120min | 120 minutes | 6 | Advanced |

**Response:** `201 Created`
```json
{
  "session_id": "uuid-xxxx-xxxx",
  "user_id": "1",
  "mode": "30min",
  "difficulty": "bronze",
  "start_time": "2024-12-19T14:00:00",
  "end_time": "2024-12-19T14:30:00",
  "remaining_seconds": 1800,
  "problem_set_id": "bronze_30min_01",
  "problems": [1000, 1001, 10998, 1008, 2557],
  "problem_scores": null,
  "solved_problems": [],
  "solved_count": 0,
  "total_count": 5,
  "status": "active",
  "score": 0
}
```

**Error Responses:**
| Status | Message |
|--------|---------|
| 400 | 스피드런은 정각 또는 30분에만 시작할 수 있습니다 |
| 400 | bronze 30min 문제 세트가 없습니다 |

---

#### 4.6.2 Get AI Speedrun Session

```
GET /api/ai-speedrun/session/{sessionId}
```

**Path Parameters:**
| Parameter | Type | Description |
|-----------|------|-------------|
| sessionId | string | Session UUID |

**Response:** Same format as create response.

---

#### 4.6.3 Submit Problem to AI Speedrun

```
POST /api/ai-speedrun/submit
```

**Request Body:**
```json
{
  "session_id": "uuid-xxxx-xxxx",
  "problem_id": 1000,
  "verdict": "AC"
}
```

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| session_id | string | Yes | Session UUID |
| problem_id | integer | Yes | Problem ID |
| verdict | string | Yes | AC, WA, TLE, MLE, RE, CE |

**Response:** `200 OK`
```json
{
  "success": true,
  "score": 150,
  "solved_count": 1,
  "remaining": [1001, 10998, 1008, 2557],
  "error": null
}
```

---

#### 4.6.4 Get AI Speedrun Result

```
GET /api/ai-speedrun/result/{sessionId}
```

**Response:** `200 OK`
```json
{
  "session_id": "uuid-xxxx-xxxx",
  "user_id": "1",
  "mode": "30min",
  "total_time": 1200,
  "solved_count": 4,
  "total_count": 5,
  "final_score": 520,
  "problems_detail": [
    {"problem_id": 1000, "solved": true},
    {"problem_id": 1001, "solved": true},
    {"problem_id": 10998, "solved": true},
    {"problem_id": 1008, "solved": true},
    {"problem_id": 2557, "solved": false}
  ],
  "rank": 5,
  "status": "completed"
}
```

---

#### 4.6.5 Get AI Speedrun Leaderboard

```
GET /api/ai-speedrun/leaderboard
```

**Query Parameters:**
| Parameter | Type | Required | Default | Description |
|-----------|------|----------|---------|-------------|
| mode | string | No | "30min" | 30min/60min/90min/120min |
| limit | integer | No | 10 | Number of entries (1-100) |

**Response:** `200 OK`
```json
{
  "mode": "30min",
  "entries": [
    {
      "user_id": "1",
      "score": 750,
      "solved_count": 5,
      "total_count": 5,
      "completed_at": "2024-12-19T14:25:00"
    }
  ]
}
```

---

#### 4.6.6 Get Active AI Speedrun Session

Check if user has an active session.

```
GET /api/ai-speedrun/active/{userId}
```

**Response:** `200 OK` - Returns session if active, `404` if no active session.

---

### 4.7 Health Check APIs

---

#### 4.7.1 AI Server Health Check

Check AI server connection status.

```
GET /api/ai/health
```

**Response:** `200 OK` (healthy) or `503 Service Unavailable`
```json
{
  "ai_server_status": "ok",
  "backend_status": "ok"
}
```

---

## 5. Data Models

### Enums

#### GameMode
```
CLASSIC    - Standard time-attack mode
TAGFOCUS   - Focus on specific algorithm tags
RETRY      - Re-attempt failed problems with streak bonus
```

#### SessionState
```
WAITING    - Session created, waiting for players
RUNNING    - Game in progress
ENDED      - Game completed
```

#### Difficulty
```
bronze < silver < gold < platinum < diamond < ruby
```

---

## 6. Error Handling

### HTTP Status Codes

| Code | Description |
|------|-------------|
| 200 | Success |
| 201 | Created (for POST creating new resources) |
| 400 | Bad Request (validation error, invalid state) |
| 401 | Unauthorized (invalid/expired token) |
| 403 | Forbidden (not owner, session full) |
| 404 | Not Found (resource doesn't exist) |
| 409 | Conflict (duplicate action) |
| 503 | Service Unavailable (AI server down) |

### Error Codes

| Module | Code | Description |
|--------|------|-------------|
| Auth | INVALID_CREDENTIALS | Wrong email/password |
| Auth | INVALID_TOKEN | Invalid/expired JWT token |
| Auth | USER_NOT_FOUND | User doesn't exist |
| Auth | EMAIL_EXISTS | Email already registered |
| Speedrun | SESSION_NOT_FOUND | Session doesn't exist |
| Speedrun | SESSION_NOT_RUNNING | Session not in expected state |
| Speedrun | SESSION_FULL | Max players reached |
| Speedrun | SESSION_EXPIRED | Session time ended |
| Speedrun | ALREADY_JOINED | User already in session |
| Speedrun | NOT_SESSION_OWNER | Only owner can perform action |
| Speedrun | USER_NOT_IN_SESSION | User not in this session |
| Speedrun | DUPLICATE_SUBMIT | Problem already submitted |
| AI | AI_SERVER_UNAVAILABLE | Cannot connect to AI server |
| AI | AI_SERVER_ERROR | AI server returned error |

---

## 7. WebSocket Endpoints

### Connection

```
Endpoint: /ws/speedrun
Protocol: STOMP over SockJS
```

### Subscribe Topics

| Topic | Description |
|-------|-------------|
| `/topic/session/{sessionId}/leaderboard` | Real-time leaderboard updates |
| `/topic/session/{sessionId}/state` | Session state changes |
| `/topic/session/{sessionId}/submit` | Problem submission notifications |

### Message Formats

**Leaderboard Update:**
```json
[
  {
    "rank": 1,
    "userId": 1,
    "score": 200,
    "solvedCount": 8,
    "streak": 5
  }
]
```

**State Change:**
```json
{
  "state": "RUNNING",
  "timestamp": 1703001234567
}
```

**Submit Notification:**
```json
{
  "userId": 1,
  "score": 25,
  "rank": 2,
  "timestamp": 1703001234567
}
```

---

## 8. Quick Reference

### Authentication Flow

```
1. POST /api/auth/signup     → Register new user
2. POST /api/auth/login      → Get access & refresh tokens
3. Use Authorization: Bearer <token> for protected endpoints
4. POST /api/auth/refresh    → Get new access token when expired
```

### Speedrun Game Flow

```
1. POST /api/speedrun/{mode}/session    → Create session
2. POST /session/{id}/join              → Others join
3. POST /session/{id}/start             → Owner starts game
4. POST /session/{id}/submit            → Submit problems
5. GET  /session/{id}/leaderboard       → Check standings
6. Session auto-ends when time expires
```

### AI Feature Flow

```
1. GET /api/ai/health                   → Check AI server status
2. GET /api/recommend?user_id=1         → Get recommendations
3. GET /api/analysis/weakness?user_id=1 → Analyze weaknesses
4. GET /api/growth/report?user_id=1     → View growth metrics
```

### Common Headers

| Header | Usage |
|--------|-------|
| `Authorization: Bearer <token>` | JWT authentication |
| `X-User-Id: <id>` | User identification for speedrun |
| `Content-Type: application/json` | Request body format |

### Base URLs Summary

| Environment | URL |
|-------------|-----|
| Backend Server | `http://localhost:8080` |
| AI Server | `http://localhost:8000` |
| Swagger UI | `http://localhost:8080/practice-ui.html` |
| WebSocket | `ws://localhost:8080/ws/speedrun` |
