# Java Naming Conventions (명명 규칙 정리)

## 1. 프로젝트(Project)

* **형식:** 모두 소문자, 공백 대신 하이픈(`-`) 사용
* **예시:**

    * `battery-lca-platform`
    * `employee-management-system`

> 일반적으로 프로젝트 이름은 Maven/Gradle 기준 `artifactId`와 일치하도록 설정합니다.

---

## 2. 패키지(Package)

* **형식:** 모두 소문자, 점(`.`)으로 구분
* **규칙:** 조직의 도메인명을 **역순(reverse domain name)** 으로 시작
* **예시:**

    * `com.company.project`
    * `kr.go.keiti.lca.system`
    * `org.springframework.boot`

> ✅ 패키지명에는 **밑줄(_), 대문자, 숫자 시작**을 사용하지 않습니다.

---

## 3. 클래스(Class)

* **형식:** **PascalCase** (또는 UpperCamelCase)
* **규칙:** 각 단어의 첫 글자를 대문자로 시작하며, 명사 형태로 작성
* **예시:**

    * `UserService`
    * `BatteryAnalyzer`
    * `ProjectController`
    * `AppStart`

> ✅ 클래스명은 “무엇을 나타내는지” 명확해야 합니다.
> ❌ 잘못된 예: `DoService`, `MyClass`, `Test1`

---

## 4. 인터페이스(Interface)

* **형식:** **PascalCase**, 클래스와 동일한 규칙
* **규칙:** 일반적으로 **형용사형** 또는 **능력을 표현하는 단어** 사용
* **예시:**

    * `Runnable`
    * `Serializable`
    * `CommonRepository`
    * `Authenticatable`

> ✅ 접두사로 `I`를 붙이는 방식(`IUserService`)은 **Java 표준이 아닙니다.**

---

## 5. 메서드(Method)

* **형식:** **camelCase** (소문자로 시작)
* **규칙:** 동사 또는 동사구로 작성
* **예시:**

    * `getUserById()`
    * `calculateTotal()`
    * `saveProjectData()`
    * `isValid()`

> ✅ 메서드는 “무엇을 **한다(do)**”를 표현해야 합니다.
> ❌ 잘못된 예: `totalValue()` → `calculateTotalValue()` 로 수정 필요.

---

## 6. 변수(Variable)

* **형식:** **camelCase**
* **규칙:** 명사형으로 작성, 의미가 명확해야 함
* **예시:**

    * `userName`
    * `projectList`
    * `batteryCapacity`
    * `filePath`

> ✅ Boolean 변수는 일반적으로 `is`, `has`, `can` 으로 시작
> 예시: `isActive`, `hasPermission`, `canExecute`

---

## 7. 상수(Constant)

* **형식:** 모두 대문자, 단어 사이에 밑줄(`_`) 사용
* **예시:**

    * `MAX_LENGTH`
    * `DEFAULT_TIMEOUT`
    * `PI`
    * `ERROR_MESSAGE_NOT_FOUND`

> ✅ 상수는 반드시 `static final` 로 선언합니다.

---

## 8. 제네릭(Generic Type Parameter)

* **형식:** 한 글자 대문자
* **예시:**

    * `T` (Type)
    * `E` (Element)
    * `K` (Key)
    * `V` (Value)
    * `R` (Result)

> 예시:
>
> ```java
> public interface CommonRepository<T, ID> {
>     T findById(ID id);
> }
> ```

---

## 9. 열거형(Enum)

* **형식:** **PascalCase** (클래스명), **상수는 UPPER_CASE**
* **예시:**

> ```java
> public enum UserRole {
>     ADMIN,
>     MANAGER,
>     USER
> }
> ```

---

## 10. 패키지별 명명 예시 구조

```
kr.go.keiti.lca
 ├─ controller
 │    ├─ ProjectController.java
 │    ├─ UserController.java
 ├─ service
 │    ├─ ProjectService.java
 │    └─ UserService.java
 ├─ repository
 │    ├─ ProjectRepository.java
 │    └─ UserRepository.java
 ├─ model
 │    ├─ Project.java
 │    └─ User.java
 └─ dto
      ├─ ProjectDto.java
      └─ UserDto.java
```

---

## 11. 추가 권장 규칙

| 항목                         | 규칙                                                                                  |
| -------------------------- | ----------------------------------------------------------------------------------- |
| 약어                         | `URL`, `HTML` 등은 모두 대문자 사용 (`getURL()`)                                             |
| 임시 변수                      | `tmp`, `temp` 대신 의미를 명확히 표현 (`tempFilePath` 등)                                      |
| 테스트 클래스                    | `{클래스명}Test` (`UserServiceTest`)                                                    |
| 예외 클래스                     | 반드시 `Exception` 접미사 사용 (`InvalidUserException`)                                     |
| 로그 변수                      | 항상 `private static final Logger logger = LoggerFactory.getLogger(ClassName.class);` |
| DAO/Service/Controller 접미사 | 계층별로 명시 (`UserDao`, `UserService`, `UserController`)                                |