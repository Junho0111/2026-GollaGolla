# GollaGolla API 명세서

| 항목 | 내용 |
|---|---|
| Base URL | `http://{host}/api/v1` |
| 인증 방식 | JWT Bearer Token |
| 공통 인증 헤더 | `Authorization: Bearer {accessToken}` |
| 최종 수정일 | 2026-09-07 |

---

<br>

## 목차

1. [공통 에러 응답](#1-공통-에러-응답)
2. [인증 (Auth)](#2-인증-auth)
3. [장소 (POI)](#3-장소-poi)
4. [지역 (Region)](#4-지역-region)
5. [리뷰 (Review)](#5-리뷰-review)
6. [찜 (Wishlist)](#6-찜-wishlist)
7. [일정 (Itinerary)](#7-일정-itinerary)
8. [화면 단위 API 조합](#8-화면-단위-api-조합)

---

<br>

## 1. 공통 에러 응답

모든 에러 응답은 아래 JSON 구조를 따른다.

```json
{
  "code": "에러코드 문자열",
  "message": "사용자 노출 메시지"
}
```

<br>

### 1.1 공통 에러 코드

| HTTP 상태 | code | message |
|:---:|---|---|
| 400 | `COMMON_001` | 잘못된 입력 값입니다. (validation 실패 시 필드별 메시지로 대체) |
| 400 | `BAD_REQUEST` | 필수 파라미터 `{파라미터명}`가 누락되었습니다. |
| 401 | `AUTH_007` | Bearer 토큰이 존재하지 않습니다. |
| 401 | `AUTH_009` | 유효하지 않은 토큰입니다. |
| 500 | `SYS_500` | 서버 내부 오류가 발생했습니다. |

<br>

## 2. 인증 (Auth)

<br>

### 2.1 회원가입

`POST /auth/signup`

**인증** 불필요

**Request Body**

| 필드 | 타입 | 필수 | 설명 |
|---|:---:|:---:|---|
| `email` | string | ✅ | 이메일 형식 |
| `password` | string | ✅ | 최소 8자 이상 |
| `nickname` | string | ✅ | 2자 이상 20자 이하 |

```json
{
  "email": "user@example.com",
  "password": "password123",
  "nickname": "골라골라유저"
}
```

**Response** `201 Created`

```json
{
  "memberId": 1,
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiJ9..."
}
```

**에러 응답**

| 상태 | code | message |
|:---:|---|---|
| 400 | `COMMON_001` | `email: 올바른 이메일 형식이 아닙니다.`, `password: 비밀번호는 최소 8자 이상이어야 합니다.` (validation 실패 필드별) |
| 409 | `AUTH_001` | 이미 사용 중인 이메일입니다. |
| 409 | `AUTH_002` | 이미 사용 중인 닉네임입니다. |

<br><br>

### 2.2 로그인

`POST /auth/login`

**인증** 불필요

**Request Body**

| 필드 | 타입 | 필수 | 설명 |
|---|:---:|:---:|---|
| `email` | string | ✅ | 이메일 형식 |
| `password` | string | ✅ | — |

```json
{
  "email": "user@example.com",
  "password": "password123"
}
```

**Response** `200 OK`

```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiJ9..."
}
```

**에러 응답**

| 상태 | code | message |
|:---:|---|---|
| 400 | `AUTH_004` | 소셜 계정으로 가입된 이메일입니다. OAuth 로그인을 이용해 주세요. |
| 400 | `COMMON_001` | validation 실패 메시지 |
| 401 | `AUTH_003` | 이메일 또는 비밀번호가 올바르지 않습니다. |

<br><br>

### 2.3 OAuth 소셜 로그인

`POST /auth/oauth/{provider}`

**인증** 불필요
**Path Variable** `provider` — `kakao` \| `naver` \| `google` (대소문자 무관)

**Request Body**

| 필드 | 타입 | 필수 | 설명 |
|---|:---:|:---:|---|
| `authorizationCode` | string | ✅ | OAuth 인가 코드 |

```json
{
  "authorizationCode": "abc123xyz"
}
```

**Response** `200 OK`

```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiJ9...",
  "isNewUser": true
}
```

> `isNewUser`: 최초 OAuth 가입 시 `true`, 기존 회원 로그인 시 `false`

**에러 응답**

| 상태 | code | message |
|:---:|---|---|
| 400 | `AUTH_006` | 지원하지 않는 OAuth Provider입니다. |
| 400 | `AUTH_008` | OAuth Provider 이름이 비어있습니다. |
| 409 | `AUTH_005` | 이미 다른 방식으로 가입된 이메일입니다. |

<br><br>

### 2.4 토큰 재발급

`POST /auth/refresh`

**인증** 불필요 (refreshToken 자체가 인증 수단)

**Request Body**

| 필드 | 타입 | 필수 | 설명 |
|---|:---:|:---:|---|
| `refreshToken` | string | ✅ | 로그인/회원가입 시 발급받은 Refresh Token |

```json
{
  "refreshToken": "eyJhbGciOiJIUzI1NiJ9..."
}
```

**Response** `200 OK`

```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...(새 토큰)",
  "refreshToken": "eyJhbGciOiJIUzI1NiJ9...(새 토큰)"
}
```

> **Refresh Token Rotation** 적용 — 재발급 시 기존 refreshToken은 즉시 폐기되고 새 토큰 쌍이 발급된다.
> 프론트엔드는 응답받은 새 refreshToken을 저장소에 업데이트해야 한다.

**에러 응답**

| 상태 | code | message |
|:---:|---|---|
| 400 | `COMMON_001` | `refreshToken: 리프레시 토큰은 필수입니다.` |
| 401 | `AUTH_011` | 유효하지 않거나 만료된 리프레시 토큰입니다. |
| 404 | `AUTH_010` | 존재하지 않는 회원입니다. |

<br><br>

### 2.5 로그아웃

`DELETE /auth/logout`

**인증** ✅ 필요

**Request Body** 없음

**Response** `204 No Content`

**에러 응답**

| 상태 | code | message |
|:---:|---|---|
| 401 | `AUTH_007` | Bearer 토큰이 존재하지 않습니다. |
| 401 | `AUTH_009` | 유효하지 않은 토큰입니다. |

<br><br>

### 2.6 회원 탈퇴

`DELETE /auth/withdraw`

**인증** ✅ 필요

**Request Body** 없음

**Response** `204 No Content`

**에러 응답**

| 상태 | code | message |
|:---:|---|---|
| 401 | `AUTH_007` | Bearer 토큰이 존재하지 않습니다. |
| 404 | `AUTH_010` | 존재하지 않는 회원입니다. |

<br><br>

### 2.7 내 정보 조회

`GET /members/me`

**인증** ✅ 필요

**Request Body** 없음

**Response** `200 OK`

```json
{
  "memberId": 1,
  "email": "user@example.com",
  "nickname": "고라니",
  "provider": "LOCAL"
}
```

**에러 응답**

| 상태 | code | message |
|:---:|---|---|
| 401 | `AUTH_007` | Bearer 토큰이 존재하지 않습니다. |
| 401 | `AUTH_009` | 유효하지 않은 토큰입니다. |
| 404 | `AUTH_010` | 존재하지 않는 회원입니다. |

<br>

## 3. 장소 (POI)

> `GET /pois/**`, `GET /search/**` 는 비인증 접근을 허용한다.
> 로그인 상태에서 조회 시 `wished` / `isWished` 필드가 정확히 반영되며, 비로그인 시 `false`로 고정된다.

<br>

### 3.1 POI 피드 목록 조회

`GET /pois`

**인증** 선택 (로그인 시 `wished` 반영)

**Query Parameters**

| 파라미터 | 타입 | 필수 | 기본값 | 설명 |
|---|:---:|:---:|:---:|---|
| `regionId` | Long | ❌ | — | 지역 ID로 필터링 |
| `category` | string | ❌ | — | `HOTEL` \| `ATTRACTION` \| `ACTIVITY` \| `RESTAURANT` \| `FESTIVAL` |
| `page` | int | ❌ | `0` | 페이지 번호 (0 이상) |
| `size` | int | ❌ | `20` | 페이지 크기 (1~100) |

**Response** `200 OK`

```json
{
  "content": [
    {
      "poiId": 1,
      "name": "경복궁",
      "thumbnailUrl": "https://cdn.example.com/poi/1.jpg",
      "rating": 4.5,
      "reviewCount": 128,
      "wishCount": 45,
      "popularityScore": 92.3,
      "wished": false
    }
  ],
  "totalPages": 5,
  "hasNext": true
}
```

**에러 응답**

| 상태 | code | message |
|:---:|---|---|
| 400 | `COMMON_001` | `page: page는 0 이상이어야 합니다.` |
| 400 | `COMMON_001` | `size: size는 1 이상이어야 합니다.` / `size는 100 이하이어야 합니다.` |

<br><br>

### 3.2 POI 키워드 검색

`GET /search?q={keyword}&type=poi`

**인증** 불필요

**Query Parameters**

| 파라미터 | 타입 | 필수 | 기본값 | 설명 |
|---|:---:|:---:|:---:|---|
| `q` | string | ✅ | — | 검색 키워드 |
| `type` | string | ❌ | `poi` | 검색 타입 (현재 `poi`만 지원) |

**Response** `200 OK`

```json
{
  "results": [
    {
      "poiId": 1,
      "name": "경복궁",
      "category": "ATTRACTION",
      "regionName": "서울"
    }
  ]
}
```

**에러 응답**

| 상태 | code | message |
|:---:|---|---|
| 400 | `POI_002` | 지원하지 않는 검색 타입입니다. 현재 지원: poi |

<br><br>

### 3.3 POI 상세 조회

`GET /pois/{poiId}`

**인증** 선택 (로그인 시 `isWished` 반영)

**Path Variable**

| 파라미터 | 타입 | 설명 |
|---|:---:|---|
| `poiId` | Long | POI ID |

**Response** `200 OK`

```json
{
  "poiId": 1,
  "name": "경복궁",
  "rating": 4.5,
  "lat": 37.5796,
  "lng": 126.9770,
  "thumbnailUrl": "https://cdn.example.com/poi/1.jpg",
  "imageUrls": [
    "https://cdn.example.com/poi/1_1.jpg",
    "https://cdn.example.com/poi/1_2.jpg"
  ],
  "openHours": {
    "월": "09:00~18:00",
    "화": "09:00~18:00"
  },
  "breakTime": {
    "월": "12:00~13:00"
  },
  "closedDays": "매주 화요일",
  "naverMapUrl": "https://map.naver.com/...",
  "category": "ATTRACTION",
  "description": "조선시대 대표 궁궐...",
  "isWished": false
}
```

> `openHours`, `breakTime`: key=요일 문자열, value=시간 범위 문자열. 정보 없으면 `{}`

**에러 응답**

| 상태 | code | message |
|:---:|---|---|
| 404 | `POI_001` | 존재하지 않는 장소입니다. |

<br>

## 4. 지역 (Region)

<br>

### 4.1 지역 목록 조회

`GET /regions`

**인증** 불필요

**Query Parameters**

| 파라미터 | 타입 | 필수 | 설명 |
|---|:---:|:---:|---|
| `parentId` | Long | ❌ | 부모 지역 ID로 하위 지역 필터링 |
| `depth` | Integer | ❌ | 지역 계층 depth로 필터링 |

**Response** `200 OK`

```json
[
  { "regionId": 1, "name": "서울", "depth": 1 },
  { "regionId": 2, "name": "강남구", "depth": 2 }
]
```

> 조건에 맞는 결과가 없으면 빈 배열 `[]` 반환

<br>

## 5. 리뷰 (Review)

<br>

### 5.1 리뷰 목록 조회

`GET /pois/{poiId}/reviews`

**인증** 불필요

**Path Variable**

| 파라미터 | 타입 | 설명 |
|---|:---:|---|
| `poiId` | Long | POI ID |

**Query Parameters**

| 파라미터 | 타입 | 필수 | 기본값 | 설명 |
|---|:---:|:---:|:---:|---|
| `page` | int | ❌ | `0` | 페이지 번호 (0 이상) |
| `size` | int | ❌ | `20` | 페이지 크기 (1~100) |

**Response** `200 OK`

```json
{
  "content": [
    {
      "reviewId": 1,
      "memberId": 42,
      "nickname": "골라골라유저",
      "rating": 5,
      "content": "정말 좋았어요!",
      "createdAt": "2026-09-01T14:30:00"
    }
  ],
  "hasNext": false
}
```

> Slice 기반 페이징 — `totalPages` 없이 `hasNext`만 제공

**에러 응답**

| 상태 | code | message |
|:---:|---|---|
| 400 | `COMMON_001` | `page: page는 0 이상이어야 합니다.` 등 |

<br><br>

### 5.2 리뷰 작성

`POST /pois/{poiId}/reviews`

**인증** ✅ 필요

**Path Variable**

| 파라미터 | 타입 | 설명 |
|---|:---:|---|
| `poiId` | Long | POI ID |

**Request Body**

| 필드 | 타입 | 필수 | 설명 |
|---|:---:|:---:|---|
| `rating` | Integer | ✅ | 1~5 정수 |
| `content` | string | ✅ | 리뷰 내용 (빈 문자열 불가) |

```json
{
  "rating": 4,
  "content": "경치가 정말 좋았어요!"
}
```

**Response** `201 Created`

```json
{
  "reviewId": 15,
  "poiRatingUpdated": 4.3
}
```

> `poiRatingUpdated`: 리뷰 등록 후 갱신된 POI 평균 평점 (BigDecimal)

**에러 응답**

| 상태 | code | message |
|:---:|---|---|
| 400 | `COMMON_001` | `rating: 평점은 최소 1점이어야 합니다.` 등 |
| 401 | `AUTH_007` | Bearer 토큰이 존재하지 않습니다. |
| 404 | `POI_001` | 존재하지 않는 장소입니다. |
| 409 | `REVIEW_001` | 이미 리뷰를 작성한 장소입니다. |

<br>

## 6. 찜 (Wishlist)

<br>

### 6.1 내 찜 목록 조회

`GET /wishlist`

**인증** ✅ 필요

**Response** `200 OK`

```json
[
  {
    "poiId": 1,
    "name": "경복궁",
    "isPublic": true,
    "thumbnailUrl": "https://cdn.example.com/poi/1.jpg",
    "rating": 4.5,
    "address": "서울 종로구 사직로 161",
    "category": "ATTRACTION"
  }
]
```

**에러 응답**

| 상태 | code | message |
|:---:|---|---|
| 401 | `AUTH_007` | Bearer 토큰이 존재하지 않습니다. |

<br><br>

### 6.2 찜 추가

`POST /wishlist`

**인증** ✅ 필요

**Request Body**

| 필드 | 타입 | 필수 | 설명 |
|---|:---:|:---:|---|
| `poiId` | Long | ✅ | POI ID |
| `isPublic` | Boolean | ✅ | 찜 공개 여부 |

```json
{
  "poiId": 1,
  "isPublic": true
}
```

**Response** `201 Created` (Body 없음)

**에러 응답**

| 상태 | code | message |
|:---:|---|---|
| 400 | `COMMON_001` | validation 실패 메시지 |
| 401 | `AUTH_007` | Bearer 토큰이 존재하지 않습니다. |
| 404 | `POI_001` | 존재하지 않는 장소입니다. |
| 409 | `WISH_001` | 이미 찜한 장소입니다. |

<br><br>

### 6.3 찜 공개여부 수정

`PATCH /wishlist/{poiId}`

**인증** ✅ 필요

**Path Variable**

| 파라미터 | 타입 | 설명 |
|---|:---:|---|
| `poiId` | Long | POI ID |

**Request Body**

| 필드 | 타입 | 필수 | 설명 |
|---|:---:|:---:|---|
| `isPublic` | Boolean | ✅ | 변경할 공개 여부 |

```json
{
  "isPublic": false
}
```

**Response** `200 OK` (Body 없음)

**에러 응답**

| 상태 | code | message |
|:---:|---|---|
| 401 | `AUTH_007` | Bearer 토큰이 존재하지 않습니다. |
| 404 | `WISH_002` | 찜 내역을 찾을 수 없습니다. |

<br><br>

### 6.4 찜 삭제

`DELETE /wishlist/{poiId}`

**인증** ✅ 필요

**Path Variable**

| 파라미터 | 타입 | 설명 |
|---|:---:|---|
| `poiId` | Long | POI ID |

**Response** `204 No Content`

**에러 응답**

| 상태 | code | message |
|:---:|---|---|
| 401 | `AUTH_007` | Bearer 토큰이 존재하지 않습니다. |
| 404 | `WISH_002` | 찜 내역을 찾을 수 없습니다. |

<br>

## 7. 일정 (Itinerary)

<br>

### 7.1 일정 생성

`POST /itineraries`

**인증** ✅ 필요

**Request Body**

| 필드 | 타입 | 필수 | 설명 |
|---|:---:|:---:|---|
| `title` | string | ✅ | 일정 제목 (최대 100자) |
| `regionId` | Long | ✅ | 지역 ID |
| `startDate` | string (LocalDate) | ✅ | 여행 시작일 (`yyyy-MM-dd`) |
| `endDate` | string (LocalDate) | ✅ | 여행 종료일 (`yyyy-MM-dd`) |
| `transportMode` | string | ✅ | `CAR` \| `WALK` \| `TRANSIT` |

```json
{
  "title": "서울 3박 4일",
  "regionId": 1,
  "startDate": "2026-10-01",
  "endDate": "2026-10-04",
  "transportMode": "TRANSIT"
}
```

**Response** `201 Created`

```json
{
  "itineraryId": 10
}
```

**에러 응답**

| 상태 | code | message |
|:---:|---|---|
| 400 | `COMMON_001` | validation 실패 메시지 |
| 400 | `DOMAIN_001` | 시작일은 종료일보다 빨라야 합니다. |
| 401 | `AUTH_007` | Bearer 토큰이 존재하지 않습니다. |

<br><br>

### 7.2 AI 일정 생성

`POST /itineraries/ai`

**인증** ✅ 필요

**Request Body**

| 필드 | 타입 | 필수 | 설명 |
|---|:---:|:---:|---|
| `title` | string | ✅ | 일정 제목 |
| `regionId` | Long | ✅ | 지역 ID |
| `startDate` | string (LocalDate) | ✅ | 여행 시작일 (`yyyy-MM-dd`) |
| `endDate` | string (LocalDate) | ✅ | 여행 종료일 (`yyyy-MM-dd`) |
| `transportMode` | string | ❌ | `CAR` \| `WALK` \| `TRANSIT` |
| `poiIds` | Long[] | ✅ | AI가 배치할 POI ID 목록 (1개 이상) |

```json
{
  "title": "AI가 만들어준 서울 여행",
  "regionId": 1,
  "startDate": "2026-10-01",
  "endDate": "2026-10-03",
  "transportMode": "CAR",
  "poiIds": [1, 2, 5, 9]
}
```

**Response** `201 Created`

```json
{
  "itinerary": {
    "itineraryId": 11,
    "title": "AI가 만들어준 서울 여행",
    "days": [
      {
        "dayNo": 1,
        "items": [
          {
            "itemId": 101,
            "poiId": 1,
            "seq": 0,
            "isAnchor": false,
            "startTime": "10:00:00",
            "endTime": "12:00:00",
            "memo": null
          }
        ]
      }
    ]
  },
  "aiExplanation": "1일차에는 경복궁을 방문한 뒤..."
}
```

**에러 응답**

| 상태 | code | message |
|:---:|---|---|
| 400 | `COMMON_001` | validation 실패 메시지 |
| 401 | `AUTH_007` | Bearer 토큰이 존재하지 않습니다. |
| 404 | `POI_001` | 존재하지 않는 장소입니다. |

<br><br>

### 7.3 내 일정 목록 조회

`GET /itineraries`

**인증** ✅ 필요

**Response** `200 OK`

```json
[
  {
    "itineraryId": 10,
    "title": "서울 3박 4일",
    "regionId": 1,
    "startDate": "2026-10-01",
    "endDate": "2026-10-04",
    "transportMode": "TRANSIT",
    "genType": "MANUAL"
  },
  {
    "itineraryId": 11,
    "title": "AI가 만들어준 서울 여행",
    "regionId": 1,
    "startDate": "2026-10-01",
    "endDate": "2026-10-03",
    "transportMode": "CAR",
    "genType": "AI"
  }
]
```

> `genType`: `MANUAL`(직접 생성) \| `AI`(AI 생성). 최신순(id 내림차순) 정렬

**에러 응답**

| 상태 | code | message |
|:---:|---|---|
| 401 | `AUTH_007` | Bearer 토큰이 존재하지 않습니다. |

<br><br>

### 7.4 일정 상세 조회

`GET /itineraries/{id}`

**인증** ✅ 필요

**Path Variable**

| 파라미터 | 타입 | 설명 |
|---|:---:|---|
| `id` | Long | 일정 ID |

**Response** `200 OK`

```json
{
  "itineraryId": 10,
  "title": "서울 3박 4일",
  "days": [
    {
      "dayNo": 1,
      "items": [
        {
          "itemId": 101,
          "poiId": 1,
          "poiName": "남산서울타워",
          "poiThumbnailUrl": "https://example.com/image.jpg",
          "seq": 0,
          "isAnchor": false,
          "startTime": "10:00:00",
          "endTime": "12:00:00",
          "memo": null
        }
      ]
    }
  ]
}
```

> `days`는 dayNo 오름차순, `items`는 seq 오름차순 정렬.
> 아이템이 없는 일차는 응답에 포함되지 않는다.

**에러 응답**

| 상태 | code | message |
|:---:|---|---|
| 401 | `AUTH_007` | Bearer 토큰이 존재하지 않습니다. |
| 403 | `ITINERARY_003` | 해당 일정에 접근할 권한이 없습니다. |
| 404 | `ITINERARY_001` | 일정을 찾을 수 없습니다. |

<br><br>

### 7.5 일정 아이템 추가 (단건)

`POST /itineraries/{id}/items`

**인증** ✅ 필요

**Path Variable**

| 파라미터 | 타입 | 설명 |
|---|:---:|---|
| `id` | Long | 일정 ID |

**Request Body**

| 필드 | 타입 | 필수 | 설명 |
|---|:---:|:---:|---|
| `poiId` | Long | ✅ | 추가할 POI ID |
| `dayNo` | Integer | ✅ | 일차 (1 이상) |
| `seq` | Integer | ✅ | 해당 일차 내 순서 (0 이상) |

```json
{
  "poiId": 5,
  "dayNo": 1,
  "seq": 2
}
```

**Response** `201 Created` (Body 없음)

**에러 응답**

| 상태 | code | message |
|:---:|---|---|
| 400 | `COMMON_001` | validation 실패 메시지 |
| 401 | `AUTH_007` | Bearer 토큰이 존재하지 않습니다. |
| 403 | `ITINERARY_003` | 해당 일정에 접근할 권한이 없습니다. |
| 404 | `ITINERARY_001` | 일정을 찾을 수 없습니다. |

<br><br>

### 7.6 일정 아이템 일괄 추가 (Bulk)

`POST /itineraries/{id}/items/bulk`

**인증** ✅ 필요

**Path Variable**

| 파라미터 | 타입 | 설명 |
|---|:---:|---|
| `id` | Long | 일정 ID |

**Request Body**

| 필드 | 타입 | 필수 | 설명 |
|---|---|:---:|---|
| `items` | object[] | ✅ | 아이템 목록 (1개 이상) |
| `items[].poiId` | Long | ✅ | POI ID |
| `items[].dayNo` | Integer | ✅ | 일차 (1 이상) |
| `items[].seq` | Integer | ✅ | 순서 (0 이상) |
| `items[].startTime` | string (LocalTime) | ❌ | 시작 시간 (`HH:mm:ss`) |
| `items[].isAnchor` | Boolean | ❌ | AI가 자동 배치한 장소 여부 (AI 생성 시 `true`, 수동 추가 시 `false`). 프론트엔드 UI 표시 구분용이며 서버에서 수정을 막지 않음 |

```json
{
  "items": [
    { "poiId": 1, "dayNo": 1, "seq": 0, "startTime": "10:00:00", "isAnchor": true },
    { "poiId": 2, "dayNo": 1, "seq": 1, "startTime": null, "isAnchor": false }
  ]
}
```

**Response** `201 Created` (Body 없음)

**에러 응답**

| 상태 | code | message |
|:---:|---|---|
| 400 | `COMMON_001` | validation 실패 메시지 |
| 401 | `AUTH_007` | Bearer 토큰이 존재하지 않습니다. |
| 403 | `ITINERARY_003` | 해당 일정에 접근할 권한이 없습니다. |
| 404 | `ITINERARY_001` | 일정을 찾을 수 없습니다. |

<br><br>

### 7.7 일정 아이템 수정

`PATCH /itineraries/{id}/items/{itemId}`

**인증** ✅ 필요

**Path Variable**

| 파라미터 | 타입 | 설명 |
|---|:---:|---|
| `id` | Long | 일정 ID |
| `itemId` | Long | 아이템 ID |

**Request Body**

| 필드 | 타입 | 필수 | 설명 |
|---|:---:|:---:|---|
| `dayNo` | Integer | ✅ | 일차 (1 이상) |
| `seq` | Integer | ✅ | 순서 (0 이상) |
| `startTime` | string (LocalTime) | ❌ | 시작 시간 (`HH:mm:ss`) |
| `endTime` | string (LocalTime) | ❌ | 종료 시간 (`HH:mm:ss`) |
| `isAnchor` | Boolean | ❌ | AI가 자동 배치한 장소 여부 (AI 생성 시 `true`, 수동 추가 시 `false`). 프론트엔드 UI 표시 구분용이며 서버에서 수정을 막지 않음 |

```json
{
  "dayNo": 2,
  "seq": 0,
  "startTime": "09:00:00",
  "endTime": "11:00:00",
  "isAnchor": false
}
```

**Response** `200 OK` (Body 없음)

**에러 응답**

| 상태 | code | message |
|:---:|---|---|
| 400 | `COMMON_001` | validation 실패 메시지 |
| 401 | `AUTH_007` | Bearer 토큰이 존재하지 않습니다. |
| 403 | `ITINERARY_003` | 해당 일정에 접근할 권한이 없습니다. |
| 404 | `ITINERARY_001` | 일정을 찾을 수 없습니다. |
| 404 | `ITINERARY_002` | 일정 항목을 찾을 수 없습니다. |

<br><br>

### 7.8 일정 아이템 메모 수정

`PATCH /itineraries/{id}/items/{itemId}/memo`

**인증** ✅ 필요

**Path Variable**

| 파라미터 | 타입 | 설명 |
|---|:---:|---|
| `id` | Long | 일정 ID |
| `itemId` | Long | 아이템 ID |

**Request Body**

| 필드 | 타입 | 필수 | 설명 |
|---|:---:|:---:|---|
| `memo` | string | ❌ | 장소별 사용자 메모 (`null` 전송 시 메모 삭제) |

```json
{
  "memo": "점심은 여기서 먹기로!"
}
```

> 메모를 삭제하려면 `"memo": null` 로 전송한다.

**Response** `200 OK` (Body 없음)

**에러 응답**

| 상태 | code | message |
|:---:|---|---|
| 401 | `AUTH_007` | Bearer 토큰이 존재하지 않습니다. |
| 403 | `ITINERARY_003` | 해당 일정에 접근할 권한이 없습니다. |
| 404 | `ITINERARY_001` | 일정을 찾을 수 없습니다. |
| 404 | `ITINERARY_002` | 일정 항목을 찾을 수 없습니다. |

<br><br>

### 7.9 일정 아이템 삭제

`DELETE /itineraries/{id}/items/{itemId}`

**인증** ✅ 필요

**Path Variable**

| 파라미터 | 타입 | 설명 |
|---|:---:|---|
| `id` | Long | 일정 ID |
| `itemId` | Long | 아이템 ID |

**Response** `204 No Content`

**에러 응답**

| 상태 | code | message |
|:---:|---|---|
| 401 | `AUTH_007` | Bearer 토큰이 존재하지 않습니다. |
| 403 | `ITINERARY_003` | 해당 일정에 접근할 권한이 없습니다. |
| 404 | `ITINERARY_001` | 일정을 찾을 수 없습니다. |
| 404 | `ITINERARY_002` | 일정 항목을 찾을 수 없습니다. |

<br><br>

### 7.10 일정 삭제

`DELETE /itineraries/{id}`

**인증** ✅ 필요

**Path Variable**

| 파라미터 | 타입 | 설명 |
|---|:---:|---|
| `id` | Long | 일정 ID |

**Response** `204 No Content`

**에러 응답**

| 상태 | code | message |
|:---:|---|---|
| 401 | `AUTH_007` | Bearer 토큰이 존재하지 않습니다. |
| 403 | `ITINERARY_003` | 해당 일정에 접근할 권한이 없습니다. |
| 404 | `ITINERARY_001` | 일정을 찾을 수 없습니다. |

<br><br>

### 7.11 일정 공유 링크 생성

`POST /itineraries/{id}/share`

**인증** ✅ 필요

**Path Variable**

| 파라미터 | 타입 | 설명 |
|---|:---:|---|
| `id` | Long | 일정 ID |

**Response** `201 Created`

```json
{
  "shareToken": "a1b2c3d4e5f60000",
  "url": "https://gollagolla.com/share/a1b2c3d4e5f60000"
}
```

> 이미 공유 토큰이 있으면 기존 토큰을 그대로 반환한다.
> `shareToken`은 16자리 랜덤 영숫자 문자열이다.
> `url` = 환경변수 `app.share-base-url` + `shareToken`

**에러 응답**

| 상태 | code | message |
|:---:|---|---|
| 401 | `AUTH_007` | Bearer 토큰이 존재하지 않습니다. |
| 403 | `ITINERARY_003` | 해당 일정에 접근할 권한이 없습니다. |
| 404 | `ITINERARY_001` | 일정을 찾을 수 없습니다. |
| 500 | `SYS_500` | 서버 내부 오류가 발생했습니다. (토큰 생성 3회 충돌 시) |

<br><br>

### 7.12 공유 일정 조회 (비인증)

`GET /share/{token}`

**인증** 불필요

**Path Variable**

| 파라미터 | 타입 | 설명 |
|---|:---:|---|
| `token` | string | 공유 토큰 (16자) |

**Response** `200 OK` — 일정 상세 조회(7.4)와 동일한 구조

```json
{
  "itineraryId": 10,
  "title": "서울 3박 4일",
  "days": [
    {
      "dayNo": 1,
      "items": [
        {
          "itemId": 101,
          "poiId": 1,
          "poiName": "남산서울타워",
          "poiThumbnailUrl": "https://example.com/image.jpg",
          "seq": 0,
          "isAnchor": false,
          "startTime": "10:00:00",
          "endTime": "12:00:00",
          "memo": null
        }
      ]
    }
  ]
}
```

**에러 응답**

| 상태 | code | message |
|:---:|---|---|
| 404 | `ITINERARY_004` | 유효하지 않거나 만료된 공유 링크입니다. |

<br>

## 8. 화면 단위 API 조합

### 🔐 로그인 / 회원가입 화면

| 용도 | API |
|---|---|
| 이메일 회원가입 | `POST /auth/signup` |
| 이메일 로그인 | `POST /auth/login` |
| 소셜 로그인 | `POST /auth/oauth/{provider}` |
| 토큰 재발급 (401 발생 시) | `POST /auth/refresh` |

<br>

### 🏠 홈 / POI 피드 화면

| 용도 | API |
|---|---|
| 지역 목록 로드 (필터용) | `GET /regions` |
| POI 피드 조회 | `GET /pois?regionId=&category=&page=&size=` |
| POI 키워드 검색 | `GET /search?q=&type=poi` |

<br>

### 📍 POI 상세 화면

| 용도 | API |
|---|---|
| POI 상세 정보 | `GET /pois/{poiId}` |
| 리뷰 목록 조회 | `GET /pois/{poiId}/reviews` |
| 리뷰 작성 | `POST /pois/{poiId}/reviews` |
| 찜 추가 | `POST /wishlist` |
| 찜 삭제 | `DELETE /wishlist/{poiId}` |

<br>

### ❤️ 찜 목록 화면

| 용도 | API |
|---|---|
| 찜 목록 조회 | `GET /wishlist` |
| 찜 공개여부 변경 | `PATCH /wishlist/{poiId}` |
| 찜 삭제 | `DELETE /wishlist/{poiId}` |

<br>

### 🗓️ 일정 목록 화면

| 용도 | API |
|---|---|
| 내 일정 목록 | `GET /itineraries` |
| 일정 삭제 | `DELETE /itineraries/{id}` |

<br>

### ✏️ 일정 생성 화면

| 용도 | API |
|---|---|
| 지역 선택 목록 | `GET /regions` |
| 일정 생성 (직접) | `POST /itineraries` |
| AI 일정 생성 | `POST /itineraries/ai` |

<br>

### 🗺️ 일정 편집 화면

| 용도 | API |
|---|---|
| 일정 상세 조회 | `GET /itineraries/{id}` |
| POI 검색 (추가용) | `GET /search?q=&type=poi` |
| 아이템 단건 추가 | `POST /itineraries/{id}/items` |
| 아이템 일괄 추가 | `POST /itineraries/{id}/items/bulk` |
| 아이템 수정 (이동/시간) | `PATCH /itineraries/{id}/items/{itemId}` |
| 아이템 메모 수정 | `PATCH /itineraries/{id}/items/{itemId}/memo` |
| 아이템 삭제 | `DELETE /itineraries/{id}/items/{itemId}` |
| 공유 링크 생성 | `POST /itineraries/{id}/share` |

<br>

### 🔗 공유 일정 조회 화면

| 용도 | API |
|---|---|
| 공유 일정 조회 (비인증) | `GET /share/{token}` |

<br>

### ⚙️ 마이페이지

| 용도 | API |
|---|---|
| 내 정보 조회 | `GET /members/me` |
| 로그아웃 | `DELETE /auth/logout` |
| 회원 탈퇴 | `DELETE /auth/withdraw` |
