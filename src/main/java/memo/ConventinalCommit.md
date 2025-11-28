# Conventional Commits 규칙 정리

## 1. 개요

**Conventional Commits**는 커밋 메시지를 일정한 형식으로 작성하도록 정한 명명 규칙입니다. 이 규칙은 커밋 로그의 일관성을 높이고, 자동화된 버전 관리(예: semantic-release)나 변경 로그(changelog) 생성을 용이하게 합니다.

---

## 2. 기본 구조

```
<type>(<scope>): <subject>
```

### 구성 요소 설명

| 구성요소        | 설명                                       |
| ----------- | ---------------------------------------- |
| **type**    | 커밋의 목적(변경 유형)을 나타냅니다. 필수 항목입니다.          |
| **scope**   | 변경이 적용된 범위(모듈, 컴포넌트 등)를 나타냅니다. 선택 항목입니다. |
| **subject** | 짧고 간결한 변경 요약(명령형 어조로 작성)                 |

예시:

```
feat(user): add user login feature
fix(api): correct null pointer issue in response
```

---

## 3. type 종류

| Type         | 의미                     | 예시                                               |
| ------------ | ---------------------- | ------------------------------------------------ |
| **feat**     | 새로운 기능 추가              | `feat(auth): add OAuth2 login`                   |
| **fix**      | 버그 수정                  | `fix(user): correct typo in username validation` |
| **docs**     | 문서 수정                  | `docs(readme): update installation guide`        |
| **style**    | 코드 스타일 변경 (공백, 세미콜론 등) | `style(lint): apply prettier formatting`         |
| **refactor** | 기능 변경 없이 코드 구조 개선      | `refactor(service): simplify data flow`          |
| **perf**     | 성능 개선                  | `perf(api): improve query performance`           |
| **test**     | 테스트 코드 추가 또는 수정        | `test(utils): add unit test for string parser`   |
| **build**    | 빌드 시스템 관련 변경           | `build(gradle): update dependency version`       |
| **ci**       | CI/CD 설정 변경            | `ci(github): add build workflow`                 |
| **chore**    | 기타 유지보수 작업             | `chore(deps): remove unused libraries`           |
| **revert**   | 이전 커밋 되돌리기             | `revert: feat(user): add user login feature`     |

---

## 4. subject 작성 규칙

* **명령어**로 작성합니다. (예: "Add" 대신 "Adds" 또는 "Added"를 사용하지 않음)
* 문장 끝에 마침표(`.`)를 붙이지 않습니다.
* 50자 이내로 간결하게 작성합니다.

예시:

```
fix(api): handle null input in user service
```

---

## 5. body (본문)

```
<type>(<scope>): <subject>

<body>
```

* 본문은 선택 사항입니다.
* 변경 이유, 해결한 문제, 추가 정보 등을 서술합니다.
* 각 줄은 72자 이내로 작성합니다.

예시:

```
feat(user): add password reset function

This commit adds the password reset API endpoint.
It includes email token validation and expiration handling.
```

---

## 6. footer (하단)

```
<type>(<scope>): <subject>

<body>

<footer>
```

* 관련된 이슈, 브레이킹 체인지 등을 표시합니다.

### 예시 1: 이슈 참조

```
fix(login): correct token refresh error

Closes #321
```

### 예시 2: Breaking Change 명시

```
feat(api): change authentication method

BREAKING CHANGE: old token endpoints have been removed.
```

---

## 7. 예시 모음

```
feat(auth): implement JWT authentication
fix(api): correct response object key naming
refactor(core): optimize service dependency injection
docs(README): update API usage examples
test(user): add missing integration tests
chore(package): update eslint configuration
```

---

## 8. Conventional Commits와 Semantic Versioning 관계

Conventional Commits는 **Semantic Versioning(유의적 버전 관리)** 과 함께 사용됩니다.

| 커밋 유형               | 버전 영향                       |
| ------------------- | --------------------------- |
| **fix**             | 패치 버전 증가 (`1.0.0 → 1.0.1`)  |
| **feat**            | 마이너 버전 증가 (`1.0.0 → 1.1.0`) |
| **BREAKING CHANGE** | 메이저 버전 증가 (`1.0.0 → 2.0.0`) |

---

## 9. 참고 링크

* [Conventional Commits 공식 문서](https://www.conventionalcommits.org/)
* [Semantic Versioning 공식 문서](https://semver.org/)