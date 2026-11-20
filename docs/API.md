# API 명세

## 0. 공통규약
- Base path: `/api`
- 요청/응답 본문: JSON (UTF-8), 필드는 camelCase
- 회원 식별: 인증 토큰으로 회원을 식별한다.
- 모든 오류 코드와 상태 코드는 공통 규약에서 정의하며, 각 기능 구현 시 추가한다.
- 각 API의 실패 응답에는 오류 발생 조건과 해당 코드를 명시한다.
- 요청 본문은 명세의 필드와 자료형을 사용하며, 정의되지 않은 필드는 입력 오류로 처리한다.

### 에러 응답

- HTTP 상태 코드와 함께 아래 형식의 본문을 반환한다.

```json
{
  "code": "INVALID_REQUEST",
  "message": "요청 값이 올바르지 않습니다."
}
```

### 에러 코드

| code | 상태 | 의미 |
|---|---|---|
| `INVALID_REQUEST` | 400 | 본문·파라미터·헤더 형식 오류, 필드 검증 실패 전반 |
| `PROFANITY_DETECTED` | 400 | 상품 이름에 비속어 포함 |
| `INVALID_CREDENTIALS` | 401 | 이메일 또는 비밀번호 불일치 |
| `INVALID_TOKEN` | 401 | 토큰 누락·형식 오류·미등록·만료 |
| `ROUTE_NOT_FOUND` | 404 | 요청 경로에 해당하는 엔드포인트나 정적 리소스 없음 |
| `METHOD_NOT_ALLOWED` | 405 | 지원하지 않는 HTTP 메서드 |
| `NOT_ACCEPTABLE` | 406 | 지원하지 않는 응답 형식 |
| `EMAIL_ALREADY_EXISTS` | 409 | 이미 가입된 이메일 |
| `UNSUPPORTED_MEDIA_TYPE` | 415 | 지원하지 않는 본문 형식 |
| `INTERNAL_SERVER_ERROR` | 500 | 서버 내부 처리 오류 |
| `PROFANITY_CHECK_UNAVAILABLE` | 503 | 비속어 검사 API 오류·시간 초과·응답 해석 실패 |

## 1. 회원

### 회원가입

- `POST /api/members/register`
- 토큰 인증 불필요

#### 요청

```json
{
  "email": "yeji@example.com",
  "password": "hello123"
}
```

| 필드 | 타입 | 필수 | 검증 조건 |
|---|---|---|---|
| `email` | 문자열 | O | 앞뒤 공백 제거 후 빈 값·내부 공백 금지, 유효한 이메일 형식, 최대 254자 |
| `password` | 문자열 | O | 5~16자, 공백 금지 |

- 두 필드 모두 누락·`null`·문자열이 아닌 값은 입력 검증 실패로 처리한다.
- 이메일은 대소문자를 구분하며, 앞뒤 공백을 제거한 값으로 중복을 검사한다.

#### 성공 응답

- 상태: `201 Created`

```json
{
  "token": "발급된 인증 토큰"
}
```

| 필드 | 타입 | 설명 |
|---|---|---|
| `token` | 문자열 | 발급 시점부터 1시간 동안 유효한 인증 토큰 |

#### 실패 응답

- 잘못된 JSON, 필수 값 누락, 필드 검증 실패 시 `INVALID_REQUEST`를 반환한다.
- 이미 가입된 이메일이면 `EMAIL_ALREADY_EXISTS`를 반환한다.

#### 처리 기준

- 회원 저장과 인증 토큰 발급이 모두 완료되어야 회원가입 성공으로 처리한다.
- 처리 중 실패하면 회원 저장과 인증 토큰 발급을 모두 취소한다.

### 로그인

- `POST /api/members/login`
- 토큰 인증 불필요

#### 요청

회원가입과 동일한 `email`, `password` 필드와 입력 검증을 사용한다. 이메일 중복 검사는 수행하지 않는다.

#### 성공 응답

- 상태: `200 OK`
- 본문: `{ "token": "새로 발급된 인증 토큰" }`
- 기존 토큰은 각자의 만료 시각까지 유효하다.

#### 실패 응답

- 입력 검증 실패 시 `INVALID_REQUEST`를 반환한다.
- 미가입 이메일과 비밀번호 불일치는 모두 `INVALID_CREDENTIALS`를 반환한다.

### 토큰 사용

- 인증이 필요한 API는 `Authorization: Bearer <토큰>` 헤더를 사용한다.
- 토큰은 발급부터 1시간 동안 유효하며, 만료 시각부터 인증에 실패한다.
- 토큰 누락·형식 오류·미등록·만료 시 `INVALID_TOKEN`을 반환한다.
- `401` 응답에는 `WWW-Authenticate: Bearer` 헤더를 포함한다.

## 2. 상품

### 상품 추가

- `POST /api/products`
- 토큰 인증 불필요

#### 요청

```json
{
  "name": "머그컵",
  "price": 12000,
  "imageUrl": "https://example.com/mug.png"
}
```

| 필드 | 타입 | 필수 | 검증 조건 |
|---|---|---|---|
| `name` | 문자열 | O | 공백 포함 최대 15자, 빈 값 금지, 허용 특수 문자 `()[]+-&/_`, 비속어 금지 |
| `price` | 정수 | O | 1~2,147,483,647 |
| `imageUrl` | 문자열 | O | 호스트가 있는 http 또는 https URL |

#### 성공 응답

- 상태: `201 Created`

```json
{
  "id": 1,
  "name": "머그컵",
  "price": 12000,
  "imageUrl": "https://example.com/mug.png"
}
```

#### 실패 응답

- 입력 검증 실패 시 `INVALID_REQUEST`를 반환한다.
- 비속어 포함 시 `PROFANITY_DETECTED`를 반환한다.
- 비속어 검사 실패 시 `PROFANITY_CHECK_UNAVAILABLE`을 반환하고 상품을 저장하지 않는다.
