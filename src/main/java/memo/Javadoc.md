# Javadoc 주석 문법 정리

## 1. 개요

**Javadoc**은 Java 코드의 문서를 자동으로 생성하기 위한 주석 시스템입니다. `/** ... */` 형식으로 작성하며, 클래스, 메서드, 필드, 생성자 등에 대한 설명을 포함합니다.

Javadoc은 IDE(예: IntelliJ, Eclipse)나 `javadoc` 명령어를 통해 HTML 문서로 자동 변환할 수 있습니다.

---

## 2. 기본 문법 구조

```
/**
 * 요약 설명 (Summary sentence)
 *
 * 상세 설명 (Detailed description, optional)
 *
 * @태그명 태그내용
 */
```

### 예시

```java
/**
 * 사용자 정보를 저장하는 클래스입니다.
 * <p>
 * 사용자 이름, 이메일, 비밀번호 등의 필드를 포함합니다.
 * </p>
 *
 * @author 홍길동
 * @version 1.0
 * @since 2025-10-08
 */
public class User {
    private String name;
    private String email;
}
```

---

## 3. 주요 태그 목록

| 태그                      | 설명                    | 사용 대상      | 예시                                     |
| ----------------------- | --------------------- | ---------- | -------------------------------------- |
| `@author`               | 작성자 이름                | 클래스, 인터페이스 | `@author 홍길동`                          |
| `@version`              | 버전 정보                 | 클래스, 인터페이스 | `@version 1.2`                         |
| `@since`                | 최초 도입 버전              | 클래스, 메서드   | `@since 1.0`                           |
| `@param`                | 매개변수 설명               | 메서드, 생성자   | `@param id 사용자 ID`                     |
| `@return`               | 반환값 설명                | 메서드        | `@return 로그인 성공 여부`                    |
| `@throws`, `@exception` | 예외 설명                 | 메서드        | `@throws IOException 파일 읽기 실패 시`       |
| `@see`                  | 참고할 클래스/메서드 링크        | 전역         | `@see java.util.List`                  |
| `@deprecated`           | 더 이상 사용하지 않음을 명시      | 전역         | `@deprecated replaceWith(UserService)` |
| `@serial`               | 직렬화 관련 설명             | 필드         | `@serial 직렬화 대상 필드`                    |
| `@link`                 | 문서 내에서 특정 클래스/메서드로 링크 | 전역         | `{@link java.util.Map}`                |
| `@code`                 | 코드 블록 내 표현            | 전역         | `{@code if (x > 0) ...}`               |
| `@literal`              | HTML 엔티티를 이스케이프       | 전역         | `{@literal <b>bold</b>}`               |
| `@inheritDoc`           | 상위 클래스의 설명 상속         | 메서드        | `{@inheritDoc}`                        |

---

## 4. 메서드 주석 예시

```java
/**
 * 주어진 사용자 ID로 사용자를 조회합니다.
 *
 * @param userId 조회할 사용자의 고유 ID
 * @return 조회된 사용자 객체. 존재하지 않으면 {@code null}
 * @throws IllegalArgumentException userId가 null인 경우
 */
public User findUserById(String userId) {
    if (userId == null) {
        throw new IllegalArgumentException("userId must not be null");
    }
    return userRepository.findById(userId);
}
```

---

## 5. 클래스/필드 주석 예시

```java
/**
 * 사용자 정보를 관리하는 서비스 클래스.
 * <p>
 * 사용자 등록, 수정, 삭제 등의 기능을 제공합니다.
 * </p>
 */
public class UserService {

    /**
     * 사용자 저장소 인스턴스.
     */
    private final UserRepository userRepository;

    /**
     * UserService 생성자.
     *
     * @param userRepository 사용자 저장소
     */
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
}
```

---

## 6. HTML 태그 사용

Javadoc은 HTML 태그를 인식할 수 있습니다. 다음과 같은 태그를 자주 사용합니다.

| 태그       | 용도            |
| -------- | ------------- |
| `<p>`    | 문단 구분         |
| `<b>`    | 굵은 글씨         |
| `<i>`    | 이탤릭체          |
| `<pre>`  | 코드 블록 (공백 보존) |
| `<code>` | 인라인 코드        |

예시:

```java
/**
 * <p><b>주의:</b> 이 메서드는 쓰레드 안전하지 않습니다.</p>
 * <pre>
 * {@code
 * User user = userService.findById("A123");
 * }
 * </pre>
 */
```

---

## 7. Javadoc 생성 명령어

터미널에서 다음 명령어로 HTML 문서를 생성할 수 있습니다.

```
javadoc -d doc -sourcepath src -subpackages com.example
```

* `-d doc` : 문서가 저장될 디렉터리 지정
* `-sourcepath` : 소스 경로 지정
* `-subpackages` : 패키지 전체를 문서화

---

## 8. 작성 시 권장 규칙

1. **요약 문장은 한 줄로 간결하게 작성** (마침표로 끝내지 않음)
2. **명령형 어조 사용** (예: "사용자를 등록합니다.")
3. **태그 순서 통일** (`@param` → `@return` → `@throws` 순)
4. **HTML 태그는 최소한으로 사용**
5. **코드 예시는 {@code ...} 또는 <pre> ... </pre>로 감싸기**

---

## 9. 예시 종합

```java
/**
 * 사용자를 등록합니다.
 *
 * <p>
 * 동일한 이메일이 존재할 경우 예외를 발생시킵니다.
 * </p>
 *
 * @param user 등록할 사용자 객체
 * @return 등록된 사용자 ID
 * @throws DuplicateEmailException 이메일 중복 시
 * @see UserRepository#save(User)
 * @since 1.0
 */
public String registerUser(User user) throws DuplicateEmailException {
    // ...
}
```

---

## 10. 참고 링크

* [Oracle Javadoc Guide](https://docs.oracle.com/en/java/javase/17/docs/specs/javadoc/doc-comment-spec.html)
* [Baeldung: Writing Javadoc Comments](https://www.baeldung.com/javadoc)