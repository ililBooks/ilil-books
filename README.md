# ililBooks

![Image](https://github.com/user-attachments/assets/76d7037b-eb21-4d7f-8734-3f018df8ebde)

<br>

## 목차
- [1. 프로젝트 소개](#프로젝트-소개-)
- [2. 주요 기능](#주요-기능-)
- [3. 트러블 슈팅 & 성능 개선](#트러블-슈팅--성능-개선)
- [4. 기술적 의사 결정 과정](#기술적-의사-결정-과정)
- [5. 인프라 아키텍처 & 기술 스택](#인프라-아키텍처--적용-기술)
    - [🏰 인프라 아키텍처](#-인프라-아키텍처)
    - [💎 기술 스택](#-기술-스택)
- [6. 성과 및 회고](#성과-및-회고)
    - [👍🏻 Keep](#-Keep)
    - [🗓️ Try](#-Try)
- [7. 역할 분담 및 협업 방식](#역할-분담-및-협업-방식)
    - [🧑🏻‍🧑🏻‍ 역할 분담](#-역할-분담)
    - [🌱 Ground Rule](#-ground-rule)

<br>

## 프로젝트 소개 

**[일반 도서 구매 및 한정판 도서 예약 서비스 애플리케이션]**
> ## "책을 넘어서 하나 하나의 경험을 선물하는 곳, ililBooks 입니다."
>
> 좋아하는 책을 발견하고, 남들보다 먼저 한정판 도서를 예약하고, 나의 독서 경험을 공유하며 더 많은 이들과 이야기를 나누는 곳.
>
> 이곳은 당신의 독서 여정이 시작되고, 확장되며, 다시 누군가에게 영감을 주는 공간입니다.
>
한정 수량의 인기 도서를 예약하고, 다양한 출판사의 이벤트를 한눈에 확인하며, 원하는 책을 쉽고 빠르게 검색하고 주문해보세요.
일일북스는 여러분이 원하는 책과 경험을 더 빠르고 정확하게 연결해주는 통합 도서 플랫폼입니다.

<br>


## 주요 기능 

<details> <summary>🔐 소셜 로그인 및 보안 인증 기능</summary>

- 카카오, 네이버, 구글 OAuth 2.0 로그인 연동

- 자체 회원가입 / 로그인 기능 별도 제공

- JWT + Refresh Token 기반 인증 시스템

- 사용자 역할 기반 권한 제어 (USER, PUBLISHER, ADMIN)

</details>

<details> <summary>📚 한정판 도서 이벤트 및 예약 시스템</summary>

- 출판사 전용 한정판 도서 이벤트 등록/수정/삭제

- 이벤트 상태 자동 전환: INACTIVE → ACTIVE → ENDED

- 예약 도중, 재고 초과 시 자동 대기열 등록

- 예약 상태 흐름: SUCCESS, WAITING, CANCELED

- 예약 만료 시 자동 취소 + 대기자 자동 승급 처리

- Redis ZSet 기반

- Redisson 기반 분산 락 적용으로 동시성 제어
</details>

<details> <summary>🛒주문 및 결제 처리</summary>

- 장바구니에서 상품 추가/제거

- 예약 성공 시 자동 주문 생성

- 주문 상태: PENDING → ORDERED → COMPLETE (CANCELLED 시 종료)

- 결제 상태: PENDING → PAID (FAIL, CANCELLED 등 실패 상태 포함)

- 배달 상태: READY → IN_TRANSIT → DELIVERED

- Portone 결제 API 연동

- 결제 성공 시 상태 업데이트, 실패 시 주문 보류

- 

</details>

<details> <summary>📝도서 및 리뷰 관리 기능</summary>

- 출판사: 도서 등록, 수정, 삭제

- 유저: 리뷰 등록, 수정, 삭제

- 리뷰 이미지 업로드/삭제 (S3 연동)

- 도서 상세 조회, 베스트셀러 노출
</details>


<details> <summary>🔎  Elasticsearch 기반 도서 검색</summary>

- 도서 제목, 저자, 출판사, 카테고리 항목별 가중치 적용 검색

- 한국어 형태소 분석기(Nori) 적용

- 키워드 검색 결과 유사성 보장
</details>

<details> <summary>🧩 AOP 기반 로그 수집</summary>

- 로그인 / 예약 / 주문 / 결제 주요 API 요청·응답 로깅 

- 로그 필드: traceId, method, url, userId, timestamp, body

- LogCollector 인터페이스 기반 확장 구조

- ConsoleLogCollector, DatabaseLogCollector 구현 완료

- AWS CloudWatch, S3, Kinesis 연동 준비 중

- 개발 단계에선 Console 기반 → 본격 운영 시 전환 예정
</details>

<details> <summary>🔔 알림(이메일) 발송 기능</summary>

- 주문 완료 / 프로모션 등의 이메일 알림 기능

- AWS SES 이메일 연동으로 실제 메일 발송 처리

- 비동기 알림 처리 지원 (@Async)
</details>

<br>


## 트러블 슈팅 & 성능 개선

<details> <summary>[🎯<strong> 트러블 슈팅] 엔티티가 영속화 되지 않아 낙관락이 적용되지 않음</strong></summary>

### 문제 정의
* 낙관락 구현 후 테스트 코드로 확인하려고 했으나, 낙관락이 제대로 구현되지 않아 테스트가 실패하는 현상 발생
```java
// 👎 해결 전 코드
@Transactional
public void decreaseStock(Book book, int quantity) {
    int remainingStock = book.decreaseStock(quantity);

    if (remainingStock < 0) {
        throw new BadRequestException(OUT_OF_STOCK.getMessage());
    }
}
```
* 검증이 완료된 book 엔티티를 매개변수로 가져와 수정 및 값을 가져오는 방식으로 코드 작성
* 문제: Book을 해당 메서드에서 조회하지 않는 것
  * Book 인스턴스는 이미 비영속 상태 (다른 트랜잭션에서 조회된 상태) ➡︎ `Version`체크가 동작하지 않음
  * JPA는 영속 상태(EntityManager가 관리하는 상태)일 때만 `@Version` 값을 체크 ➡︎ 비영속 상태의 엔티티로 낙관적 락 충돌을 감지할 수 없다.


<br>

### 해결과정
```java
@Transactional
public void decreaseStock(Long bookId, int quantity) {
    Book book = bookService.findBookByIdOrElseThrow(bookId);
    book.decreaseStock(quantity);
}
```
```java
public void decreaseStock(int quantity) {
    if (stock < quantity) {
        throw new BadRequestException(OUT_OF_STOCK.getMessage());
    }
    if (stock == quantity) {
        this.saleStatus = SaleStatus.SOLD_OUT;
    }
    this.stock -= quantity;
}
```
* 메서드의 파라미터로 bookId를 전달한 뒤 해당 트랜잭셔널 안에서 Book을 조회 (영속상태)
* 그 뒤 재고를 수정해야 낙관락 `Version`을 적용할 수 있다.
* 또한, 더티체킹 및 변경감지가 작동하도록 `decreaseStock`를 수정

<br>

### 결과
<img src = "https://img1.daumcdn.net/thumb/R1280x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FqoKe2%2FbtsNFpyUwEk%2FiKAeOMB6bZqMV3MWUo5bBk%2Fimg.png"  width="500">

* 낙관락 적용 확인 가능
#### JPA가 낙관적 락을 적용하는 조건
1. `@Version` 필드를 가진 엔티티가 **영속 상태**
2. 엔티티의 변경이 감지(dirty checking)
3. 트랜잭션 커밋 또는 flush 시점에 업데이트 쿼리가 실행되어야 함 (`Version`감지 가능)

</details>


<details>
<summary>[🎯 <strong>트러블슈팅 ]  CloudWatch 로그 접근 권한 오류 해결</strong></summary>

###  문제 정의
CloudWatch에 로그를 전송하는 과정에서 `AccessDeniedException` 오류가 발생하였습니다.

- 로컬 Console 에서는 로그 출력이 정상 작동되지만, AWS 환경에서는 로그 수집이 실패함
- 에러 메시지를 통해 IAM 권한 부족이 원인임을 추측할 수 있었습니다

<br>

확인 결과, CloudWatch에 로그를 전송하려면 IAM 사용자에게 아래와 같은 권한 부여가 필요했습니다.

- `logs:CreateLogGroup`
- `logs:CreateLogStream`
- `logs:PutLogEvents`

하지만 당시 프로젝트에 사용 중인 IAM 사용자(`ililbooks`)는 해당 권한이 없는 상태였습니다.

<br>

### 해결 방법
- AWS IAM 콘솔에서 사용자 `ililbooks`에 **AWS 관리 정책** `CloudWatchLogsFullAccess` 를 직접 부여

이 정책은 CloudWatch 로그 그룹/스트림 생성과 로그 전송에 필요한 모든 액션을 포함하고 있어 문제를 빠르게 해결할 수 있었습니다.

<br>

### 결과
- CloudWatch로 로그가 정상 전송되었고, 콘솔에서 실시간 로그 확인 가능
- AOP 기반 JSON 로깅이 CloudWatch에 성공적으로 저장되어 **운영 로그 수집 및 장애 분석 기반 확보**

[✔] 애플리케이션 로그 → AOP 로깅 → CloudWatch 저장 완료

<br>

### 📌 정리: CloudWatch 로그 전송을 위해 필요한 조건

| 조건 | 설명 |
|------|------|
| ✅ IAM 권한 | `CloudWatchLogsFullAccess` 또는 `logs:*` 관련 정책 |
| ✅ 네트워크 | 인터넷/VPC 환경에서 로그 전송이 가능한지 확인 |
| ✅ 스트림 구성 | 로그 그룹 및 로그 스트림 사전 구성 또는 자동 생성 가능 권한 필요 |

</details>

<details> <summary>[<strong>🎯 트러블 슈팅] 트랜잭션이 플러시가 되지 않아 분산락이 적용되지 않은 문제</strong></summary>

### 문제 정의
* 분산락 구현 후 테스트 코드로 확인하려고 했으나, 분산락 제대로 구현되지 않아 테스트가 실패하는 현상 발생
```java
// 👎 해결 전 코드
@Transactional
public void decreaseStockWithDistributedLock(Long bookId, int quantity) {
    String lockKey = "bookStockLock:" + bookId;
  
    redissonLockClient.runWithLockOrElse(lockKey, () -> {
      Book book = bookService.findBookByIdOrElseThrow(bookId);
      book.decreaseStock(quantity);
    }, () -> {
      throw new BadRequestException("현재 주문량이 많아 재고 확인에 실패했습니다.");
    });
}
```
* 트랜잭션 커밋 이전에는 DB에 반영되지 않음
  * `@Transactional`이 적용되어 있지만, 실제 DB 반영은 트랜잭션 커밋 시점까지 지연됨
  * Redisson 락을 사용하는 동안 재고는 메모리(영속성 컨텍스트) 상에서만 감소한 상태
  * 다른 스레드는 기존 재고 값을 가져가게됨
* `saveAndFlush()` 미사용으로 인한 지연 반영
  * `book.decreaseStock(quantity)`는 객체의 필드만 수정하며, 이를 DB에 즉시 반영하려면 `saveAndFlush()`가 필요
  * `flush()`가 호출되지 않았기 때문에 DB에 변화 없음

* 락을 획득한 트랜잭션이 변경 사항을 DB에 즉시 반영하지 않아 동시성 문제가 발생

<br>

### 해결과정
```java
public void decreaseStockWithDistributedLock(Long bookId, int quantity) {
  String lockKey = "bookStockLock:" + bookId;

  redissonLockClient.runWithLockOrElse(lockKey, () -> {
    Book book = bookService.findBookByIdOrElseThrow(bookId);
    book.decreaseStock(quantity);
    bookRepository.saveAndFlush(book);
  }, () -> {
    throw new BadRequestException("현재 주문량이 많아 재고 확인에 실패했습니다.");
  });
}
```
* `@Transactional`사용대신 `saveAndFlush()`으로 명시적으로 DB에 반영
* 락이 해제되기 전에 DB 상태가 반영되어, 다른 스레드는 정확한 재고로 재고 감소 수행

<br>

### 결과
* 분산락 적용 확인 가능
* `@Transactional`: 트랜잭션 반영을 커밋시점까지 지연
* `saveAndFlush()`: 변경사항을 즉시 DB에 반영
* 락을 사용하더라도, 락 내에서 DB 반영이 즉시 이루어지지 않으면 동시성 제어는 실패

</details>


<details> <summary>[🎯 <strong> 트러블 슈팅] H2에서 비관락 테스트 중 조기 타임아웃 예외 발생 </strong> </summary>

### 문제 정의

* 비관적 락을 테스트하기 위해 멀티스레드 테스트 코드를 작성했음
* 한 스레드는 락을 잡고 5초간 대기하고, 다른 스레드는 해당 자원을 접근하여 락을 획득하지 못하고 예외가 발생하도록 하였으나 아래와 같은 예외들이 발생
```java
org.h2.jdbc.JdbcSQLTimeoutException: Timeout trying to lock table "BOOKS"; SQL statement:

could not prepare statement [Table "USERS" not found (this database is empty)]
```

* `javax.persistence.lock.timeout` 설정이 H2에서는 완전히 반영되지 않음
* 예상과 달리 5초간 기다리지 않고 즉시 타임아웃 예외 발생

H2 공식 문서:
> H2 uses table-level locking in some modes and might throw timeout exceptions earlier than expected in pessimistic scenarios.

<br>
왜 H2를 썼는가? <br>
CI 환경에서는 외부 DB 연결 없이 테스트가 가능하도록 H2를 주로 사용함 <br>
✔️ 설치 불필요   MySQL 등 별도 설치 없이 동작 <br>
✔️ 테스트 격리   매 테스트마다 새 인스턴스(create-drop) 생성 가능 <br>
✔️ 빠른 속도    인메모리 DB이기 때문에 매우 빠름 <br>
✔️ CI 친화적   GitHub Actions, GitLab CI 등에서 쉽게 활용 가능 <br>

하지만, 동시성 테스트(특히 비관적 락)에서는 적절하지 않을 수 있음

<br>

### 해결 과정
* application-test.yml 설정을 조정해 타임아웃 예외를 조절하고, 테스트 환경을 통제함
```yml
spring:
  datasource:
    url: jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE;LOCK_TIMEOUT=10000
    driver-class-name: org.h2.Driver
    username: sa
    password:
  jpa:
    hibernate:
      ddl-auto: create-drop
    show-sql: true
    database-platform: org.hibernate.dialect.H2Dialect
```
* `DB_CLOSE_DELAY=-1`: 커넥션 종료 후에도 DB 유지
* `DB_CLOSE_ON_EXIT=FALSE`: JVM 종료 시 DB 강제 삭제 방지
* `LOCK_TIMEOUT=10000`: 비관적 락 획득 대기 시간 5초 이상 설정

<br>

### 결과
* 분산락 적용 확인 가능
* `LOCK_TIMEOUT=10000` 설정으로 H2에서도 일정 시간 동안 락을 대기함
* 그러나 동시성 테스트는 H2보다는 실제 운영 DB에 가까운 환경(MySQL 등)에서 수행하는 것이 더 안정적

</details>


<details> <summary> [🎯<strong> 트러블 슈팅] 애플리케이션과 Elasticsearch 연동 </strong> </summary>

### 문제 정의

Elasticsearch 를 사용하는 도서 검색 기능에서  
도서 정보를 담고 있는 `BookDocument`를 저장할 **Index (`books`)가 존재하지 않으면**,  
Spring Data Elasticsearch가 제공하는 `ElasticsearchRepository`의 **Bean 생성 중 오류가 발생**함.

```
BeanCreationException: Error creating bean with name 'bookSearchRepository'
```

- `@Document(indexName = "books")`로 선언된 `BookDocument`의 index가 Elasticsearch에 존재하지 않음

---

### 해결 과정

1. `BookDocument` 클래스에는 다음과 같이 설정됨

   ```java
   @Document(indexName = "books")
   public class BookDocument {
       // ...
   }
   ```

2. Spring 애플리케이션 구동 시, 해당 index(`books`)가 Elasticsearch에 존재하지 않으면  
   Spring Data Elasticsearch는 repository를 초기화하지 못하고 예외를 발생시킴.

3. **Kibana Dev Tools**에서 `books`라는 이름의 index를 직접 생성함:

   ```http
   PUT /books
   {
     "settings": {
       "analysis": {
         // 분석기 설정
       }
     },
     "mappings": {
       "properties": {
         // 필드 매핑 정보
       }
     }
   }
   ```

---

## 결과

- `books` index가 Elasticsearch에 정상적으로 생성된 후  
  Spring Boot 애플리케이션이 `BookSearchRepository`를 포함한  
  모든 Elasticsearch 관련 Bean을 **정상적으로 초기화**함

- ElasticsearchRepository 기반의 **도서 검색 기능 구현 가능**

</details>

<br>

## 기술적 의사 결정 과정

<details> <summary>[💡 <strong>기술적 의사결정] Refresh Token 관리를 MySQL과 Redis 중 어디서 하는게 좋을까?</strong></summary>

### 구현 기능
<img src="https://flaxen-swan-41e.notion.site/image/attachment%3A2b4b884e-2c4c-48d9-b0dd-296ad4b7fc40%3Aimage.png?table=block&id=1d6b649e-bbbd-804f-962f-eb203ccfe1a4&spaceId=765aecbd-0d96-4102-a877-7bb784aaedf1&width=1420&userId=&cache=v2">

* Refresh Token을 만료시간에 따라 관리하는 기능이 필요
* 수동으로 토큰을 관리하고 토큰이 계속 쌓일 경우, 조회로 인한 성능 저하 및 보안 리스크
* 기존 DB로 Refresh Token을 관리하는 방식의 대안 필요


<br>

### 비교 기술
| 항목        | 🐋 MySQL 관리 방식              | 🔴 Redis 관리 방식                   |
|-----------|-----------------------------|----------------------------------|
| ✅ 성능      | 디스크 기반이므로 처리 속도가 상대적으로 느림   | 인메모리 기반이라 읽기/쓰기 속도가 매우 빠름        |
| ✅ 관리 편의성  | SQL로 관리, 만료 토큰을 계속 관리해 주어야함 | TTL로 자동 만료 관리 가능, 만료 처리 로직 필요 없음 |
| ✅ 데이터 영속성 | 영속적 저장으로 데이터 손실 위험이 적음      | 서버 재시작 또는 장애 시 데이터 유실 가능성 존재     |
| ✅ 보안      | 만료된 토큰이 쌓이면 보안 리스크 발생 가능    | TTL로 만료되면 자동 삭제되어 보안에 유리         |
| ✅ 확장성     | 데이터량 증가 시 쿼리 성능 저하 가능       | 키-값 기반 구조로 수평 확장에 유리             |
| ✅ 비용      | 대부분 기존 시스템에 포함, 추가 비용 없음    | 별도 인메모리 저장소 운영 필요 → 비용 증가 가능성    |
| 👉 결론     | 변경이 적고 장기 저장이 필요한 토큰에 적합    | Refresh Token 처럼 휘발되어도 되는 데이터    |


<br>


### 의사결정 및 이유
* MySQL 관리 방식과 Redis관리 방식의 성능 비교
  * 기존 DB 저장 방식 - 로그인: 151ms, 토큰 재발급: 60ms
    <img src="https://flaxen-swan-41e.notion.site/image/attachment%3Ad98e6734-46a0-40c7-bfff-635bbea025a6%3Aimage.png?table=block&id=1d6b649e-bbbd-8083-9506-ea4570adac2c&spaceId=765aecbd-0d96-4102-a877-7bb784aaedf1&width=1420&userId=&cache=v2">
    <img src="https://flaxen-swan-41e.notion.site/image/attachment%3Aa3d0f481-72f4-4170-a278-8c869849be3b%3Aimage.png?table=block&id=1d6b649e-bbbd-803e-bdd8-d11c808c509d&spaceId=765aecbd-0d96-4102-a877-7bb784aaedf1&width=1420&userId=&cache=v2">
  * Redis 저장 방식 - 로그인: 135ms, 토큰 재발급: 21ms
    <img src="https://flaxen-swan-41e.notion.site/image/attachment%3Ace541ffe-1f82-4f52-8448-32fe9eecfc82%3Aimage.png?table=block&id=1d6b649e-bbbd-80d2-a372-f37763cb9ff9&spaceId=765aecbd-0d96-4102-a877-7bb784aaedf1&width=1420&userId=&cache=v2">
    <img src="https://flaxen-swan-41e.notion.site/image/attachment%3A911edbb2-f3ac-4109-8e6d-c7ce11a73181%3Aimage.png?table=block&id=1d6b649e-bbbd-80e0-88a5-f43c894fc457&spaceId=765aecbd-0d96-4102-a877-7bb784aaedf1&width=1420&userId=&cache=v2">

|  | **기존 DB 저장 방식** | **Redis 저장 방식** | 성능 향상 |
| --- | --- | --- | --- |
| **로그인** | **151ms** | **135ms** | 약 **10.6% 향상** |
| **토큰 재발급** | **60ms** | **21ms** | 약 **65% 향상** |

<br>

### 결론
* Redis 구현의 단점이 Refresh Token 구현에 크게 치명적이지 않다고 판단하여 Refresh Token 데이터의 성격에 따라 **DB보다는 Redis에서 구현하는 것으로 의사 결정**
  * TTL을 사용하여 토큰 관리 및 보안에 용이
* 조회 성능 또한 MySQL를 통한 관리보다 Redis를 통한 관리가 더 좋음

</details>


<details> <summary>[💡 <strong>기술적 의사결정] Elastic Beanstalk vs ECS 의사결정 과정</strong></summary>

### 구현 기능
+ Elastic Beanstalk 구현 핵심 기능
  + CI/CD 파이프라인: GitHub Actions → Docker 이미지 빌드 후 → ECR Push → Beanstalk 배포
  + EB, CloudWatch 통합을 통해 인스턴스 메트릭, 로그 수집, 알람 설정이 기본 제공되어 운영 중에도 안정적인 모니터링이 가능

<br>

### 비교 기술
+ ECS(Fargate) vs Elastic Beanstalk
  + ECS는 AWS의 최신 기술로서 무서버리스 구조와 자동 확장성이 매우 뛰어나지만, 구성요소가 많고 설정이 복잡해 팀원들이 적응하기 어려웠습니다.
  + Elastic Beanstalk은 EC2 기반이기 때문에 기존의 EC2/ELB 경험이 있는 팀원들이 쉽게 적응할 수 있었고, 배포 또한 CLI와 콘솔을 통해 직관적으로 처리할 수 있었습니다.
  + 모니터링 측면에서도 ECS는 로그 그룹, 컨테이너 메트릭, 이벤트 등을 개별적으로 설정해야 했던 반면, Beanstalk은 기본적으로 CloudWatch와 통합되어 있어서 헬스 상태, 로그, 알람 등을 콘솔 한 곳에서 쉽게 확인할 수 있었습니다

<br>

### 의사결정 및 이유
+ Elastic Beanstalk으로 전환하게 된 가장 큰 이유는
  + **운영 효율성**과 **팀의 이해도**
+ ECS는 설계상 훌륭했지만, 실제 운영에서 많은 복잡성이 드러났습니다.
  + Task 정의, 서비스 연결, IAM 구성, ALB 설정 등 배포를 위해 준비해야 할 리소스가 많았고, 설정 실수도 자주 발생했습니다.
  + 무엇보다 팀원들이 ECS에 익숙하지 않아 신규 기능을 도입할 때마다 학습 시간이 오래 걸리고, 실 운영에서 문제를 신속히 파악하기 어려웠습니다.
  + Beanstalk은 EC2 기반이기 때문에 이러한 요구사항을 해결할 수 있었고, SSH 접속을 통해 커널 파라미터를 조정하거나 로그를 실시간으로 추적하는 등 운영의 유연성도 높았습니다.

<br>

### 결론
Elastic Beanstalk은 우리 팀에게 있어 "기술적으로 완벽한 선택"이라기보다는, "**현실적으로 가장 적합한 선택**"이었습니다<br>
운영의 단순화, 빠른 배포, 친숙한 EC2 기반 구조, 그리고 뛰어난 모니터링 기능 덕분에 팀 전체의 생산성과 안정성이 향상되었습니다<br>
특히 인프라 구성에 시간과 리소스를 쏟기보다, 빠르게 운영 환경을 구성하고 관리에 집중할 수 있었다는 점에서 Beanstalk의 장점이 매우 크게 느껴졌습니다<br>
결국 기술 선택에서 중요한 것은 특정 기술의 최신성이나 스펙이 아니라, 해당 기술이 현재 팀의 상황과 요구에 얼마나 부합하느냐라는 점임을 깨달았습니다

</details>


<details> <summary> [💡<strong> 기술적 의사결정] OPEN API 호출, 어떤 클라이언트를 선택할까? (RestTemplate vs WebClient vs RestClient) </strong></summary>

### 구현 기능

- 국립 중앙 도서관 OPEN API 연동을 통한 **도서 정보 추출**
- 소셜 로그인(구글, 네이버, 카카오)  API 연동을 통한 **간편 로그인 기능 구현**
<br>

### 비교 기술

✅ **세 기술의 요약 비교**

| 항목     | **RestTemplate** | **RestClient** | **WebClient** |
|--------|------------------| --- | --- |
| 동기/비동기 | 	동기              |	동기|	비동기|
| 체이닝	   | ❌	               | ✅	               | ✅                |
|WebFlux 의존성|	❌|	❌|	✅|
|현재 상태|	유지보수만 진행|	최신 기술|	최신 기술|

<br>

### 의사결정 및 이유

🟠 **RestTemplate**

```java
 ResponseEntity<String> responseEntity = restTemplate.getForEntity(uri, String.class);
```
- 헤더 요청 등과 같이 **파라미터나 설정이 많아질수록 코드가 장황해진다.**
- **요청 흐름을 한눈에 파악할 수 없다.**

➡️ **위의 이유들로 제외** 

🟠 **RestClient**

```java
ResponseEntity<String> responseEntity = restClient.get()
                .uri(uri)
                .retrieve()
                .toEntity(String.class);
```
- **요청 흐름을 한눈에 파악하기 좋다.**
- 동기 방식으로 비동기 상황에는 부적합하다. 

➡️ 추후 **복잡한 상황 발생 가능성이 존재** 선택 ❌

🟠 **webClient**

```java
ResponseEntity<String> responseEntity = webClient.get()
                .uri(uri)
                .retrieve()
                .bodyToMono(String.class);
```
- 요청 흐름이 명확하고 비동기 처리에 유리

- WebFlux 의존성 필요

- 논블로킹 방식이나, 일반적인 처리 속도를 무조건 높여주진 않음
  → 상황에 따라 더 많은 자원이 소모될 수도 있음

➡️ 위와 같은 단점이 있지만 **확장성을 고려하여 WebClient로 결정**

<br>

### 결론
현재는 비동기 상황이 없지만 향후 복잡한 API 연동과 확장성을 고려해 WebClient 사용 결정

> 🔔 단, 단순한 동기 호출이라면 RestClient도 좋은 선택지가 될 수 있다. 

</details>


<details> <summary>[💡<strong> 기술적 의사결정] 주문 시 재고 감소는 낙관락, 비관락, 분산락 중 무엇이 적합할까?</strong></summary>

### 구현 기능
<img src="https://flaxen-swan-41e.notion.site/image/attachment%3A2788b72d-0517-41a9-afda-da4e47f83fe9%3APasted_Graphic.png?table=block&id=1e4b649e-bbbd-8097-a6e6-da72c13f739b&spaceId=765aecbd-0d96-4102-a877-7bb784aaedf1&width=1420&userId=&cache=v2">

* 유저가 동시에 재고를 감소시키면 동시성 제어 문제 발생 (데이터 정합성 문제)

<br>

### 비교 기술
1. **낙관적 락 (Optimistic Lock)**
- 충돌이 거의 없을 것이라고 가정하고, 데이터 수정 시점에서 충돌을 검사
- 버전을 사용해 업데이트 전후 값을 비교하는 방식으로 충돌 감지
- 장점: 락을 실제로 걸지 않아 성능 이점, 병목 현상 없음
- 단점: 충돌이 발생하면 예외 발생 후 재시도 필요

2. **비관적 락 (Pessimistic Lock)**

- 충돌이 날 수 있다고 가정하고, 데이터를 읽을 때부터 락을 걸어 다른 트랜잭션 접근 차단
- 장점: 충돌을 원천적으로 방지하여 데이터 정합성 보장에 강함
- 단점: 트랜잭션이 길면 데드락 위험이 있고, 락을 걸어 성능 저하가 발생할 수 있음

3. **분산 락 (Distributed Lock)**

- 여러 서버나 인스턴스에서 공유 리소스 접근을 조율하기 위한 락
- Redis, Zookeeper 등 여러가지 방법이 있음
- 장점: 분산 환경에서도 정합성 유지 가능, 확장성 확보
- 단점: 설정이 복잡하고 네트워크 장애 시 일관성 문제, 성능 문제가 있음

<br>

### 의사결정 및 이유
| **전략** | **재고 변화** | **평균 응답시간(ms)** | **처리량(TPS)** | **오류율(%)** | **99% 응답시간(ms)** | **데이터 정합성** | **분산환경 지원** |
| --- | --- | --- | --- | --- | --- | --- | --- |
| ❌ 락 미구현 | 1000 → 921 | 431 | 55.9 | 0% | 942 | ❌ (79건 누락) | ❌ |
| ⛔ 낙관적 락 (재시도 없음) | 1000 → 986 | 591 | 78.2 | 86% | 1198 | ❌ (14건만 성공) | ❌ |
| 🔁 낙관적 락 + 재시도 | 1000 → 901 | 332 | 52.7 | 1% | 783 | △ (99건 성공) | ❌ |
| ⏱ 낙관적 락 + 백오프 | 1000 → 900 | 347 | 32.2 | 0% | 778 | ✅ (100건 성공) | ❌ |
| ✅ 비관적 락 | 1000 → 900 | 696 | 56.9 | 0% | 1126 | ✅ (100건 성공) | △ |
| 🌐 분산 락 (Redisson) | 1000 → 900 | 911 | 49.6 | 0% | 1628 | ✅ (100건 성공) | ✅ |

* jmeter를 이용하여 테스트를 한 결과
  * 낙관적 락이 성능이 좋다. 하지만 오류률이 높은데, 이는 재시도를 하면서 개선 가능
  * 비관적락의 경우 데이터 정합성은 좋으나 성능이 떨어짐
  * 분산락 또한 비관적락보다 더 성능이 떨어지지만 분산환경에서 이점

* 주문 후 재고 감소는 트래픽이 몰리지 않아 쓰기가 많이 생기지 않을 것이라고 판단, **낙관락 + 재시도** 가 도메인 특성에 적합

<br>

### 결론
- 데이터 정합성과 성능 사이는 트레이드 오프
- 도메인 특성에 맞는 선택의 중요성
  - 일반 상품: 낙관적 락+백오프(347ms, 100% 정합성)
  - 고수요 상품: 비관적 락(696ms, 안정적 정합성)
  - 분산 환경: Redisson 분산락(911ms, 확장성)

</details>


<details> <summary>[💡<strong> 기술적 의사결정] Refresh Token 관리를 MySQL과 Redis 중 어디서 하는게 좋을까?</strong></summary>

### 구현 기능
<img src="https://flaxen-swan-41e.notion.site/image/attachment%3A2b4b884e-2c4c-48d9-b0dd-296ad4b7fc40%3Aimage.png?table=block&id=1d6b649e-bbbd-804f-962f-eb203ccfe1a4&spaceId=765aecbd-0d96-4102-a877-7bb784aaedf1&width=1420&userId=&cache=v2">

* Refresh Token을 만료시간에 따라 관리하는 기능이 필요
* 수동으로 토큰을 관리하고 토큰이 계속 쌓일 경우, 조회로 인한 성능 저하 및 보안 리스크
* 기존 DB로 Refresh Token을 관리하는 방식의 대안 필요


<br>

### 비교 기술
| 항목        | 🐋 MySQL 관리 방식              | 🔴 Redis 관리 방식                   |
|-----------|-----------------------------|----------------------------------|
| ✅ 성능      | 디스크 기반이므로 처리 속도가 상대적으로 느림   | 인메모리 기반이라 읽기/쓰기 속도가 매우 빠름        |
| ✅ 관리 편의성  | SQL로 관리, 만료 토큰을 계속 관리해 주어야함 | TTL로 자동 만료 관리 가능, 만료 처리 로직 필요 없음 |
| ✅ 데이터 영속성 | 영속적 저장으로 데이터 손실 위험이 적음      | 서버 재시작 또는 장애 시 데이터 유실 가능성 존재     |
| ✅ 보안      | 만료된 토큰이 쌓이면 보안 리스크 발생 가능    | TTL로 만료되면 자동 삭제되어 보안에 유리         |
| ✅ 확장성     | 데이터량 증가 시 쿼리 성능 저하 가능       | 키-값 기반 구조로 수평 확장에 유리             |
| ✅ 비용      | 대부분 기존 시스템에 포함, 추가 비용 없음    | 별도 인메모리 저장소 운영 필요 → 비용 증가 가능성    |
| 👉 결론     | 변경이 적고 장기 저장이 필요한 토큰에 적합    | Refresh Token 처럼 휘발되어도 되는 데이터    |


<br>


### 의사결정 및 이유
* MySQL 관리 방식과 Redis관리 방식의 성능 비교
  * 기존 DB 저장 방식 - 로그인: 151ms, 토큰 재발급: 60ms
    <img src="https://flaxen-swan-41e.notion.site/image/attachment%3Ad98e6734-46a0-40c7-bfff-635bbea025a6%3Aimage.png?table=block&id=1d6b649e-bbbd-8083-9506-ea4570adac2c&spaceId=765aecbd-0d96-4102-a877-7bb784aaedf1&width=1420&userId=&cache=v2">
    <img src="https://flaxen-swan-41e.notion.site/image/attachment%3Aa3d0f481-72f4-4170-a278-8c869849be3b%3Aimage.png?table=block&id=1d6b649e-bbbd-803e-bdd8-d11c808c509d&spaceId=765aecbd-0d96-4102-a877-7bb784aaedf1&width=1420&userId=&cache=v2">
  * Redis 저장 방식 - 로그인: 135ms, 토큰 재발급: 21ms
    <img src="https://flaxen-swan-41e.notion.site/image/attachment%3Ace541ffe-1f82-4f52-8448-32fe9eecfc82%3Aimage.png?table=block&id=1d6b649e-bbbd-80d2-a372-f37763cb9ff9&spaceId=765aecbd-0d96-4102-a877-7bb784aaedf1&width=1420&userId=&cache=v2">
    <img src="https://flaxen-swan-41e.notion.site/image/attachment%3A911edbb2-f3ac-4109-8e6d-c7ce11a73181%3Aimage.png?table=block&id=1d6b649e-bbbd-80e0-88a5-f43c894fc457&spaceId=765aecbd-0d96-4102-a877-7bb784aaedf1&width=1420&userId=&cache=v2">

|  | **기존 DB 저장 방식** | **Redis 저장 방식** | 성능 향상 |
| --- | --- | --- | --- |
| **로그인** | **151ms** | **135ms** | 약 **10.6% 향상** |
| **토큰 재발급** | **60ms** | **21ms** | 약 **65% 향상** |

<br>

### 결론
* Redis 구현의 단점이 Refresh Token 구현에 크게 치명적이지 않다고 판단하여 Refresh Token 데이터의 성격에 따라 **DB보다는 Redis에서 구현하는 것으로 의사 결정**
  * TTL을 사용하여 토큰 관리 및 보안에 용이
* 조회 성능 또한 MySQL를 통한 관리보다 Redis를 통한 관리가 더 좋음

</details>

<details> <summary>[💡 <strong>기술적 의사결정] 메일 전송 프로토콜, Gmail SMTP vs SES 무엇을 사용할까? </strong></summary>

### 구현 기능

- 주문 완료 시 알림 전송
- 프로모션 알림 전송

<br>

### 비교 기술

**Gmail SMTP 🆚 AWS SES**

| 항목 | Gmail SMTP      | AWS SES |
| --- |-----------------| --- |
| **사용 용이성** | 쉬움              | 다소 복잡 |
| **일일 발송 제한** | ⭕ (500건 내외)     | ❌ (Production 전환 시 수만 건 가능) |
| **적합한 용도** | 테스트 / 소량의 메일 전송 | 대량 발송 / 프로모션 메일 |
| **보안 설정** | 2단계 인증 및 앱 비밀번호 | IAM 사용자 + 키 관리 필요 |

<br>

### 의사결정 및 이유

- **AWS SES를 활용해 대량 메일 발송 시나리오를 테스트 진행**
- 반복적인 테스트가 필요한 상황 발생
- Gmail SMTP는 발송 제한으로 인해 한계가 있음

<br>

### 결론

- 서비스 사용자 수가 증가하면 **대량 메일 발송이 필요**
- 따라서 **AWS SES가 확장성과 실용성 측면에서 적합**

</details>


<details> <summary>[💡<strong> 기술적 의사결정] 비동기 처리 기술, @Async vs RabbitMQ 뭐가 더 나을까?</strong> </summary>

### 구현 기능

- 주문 완료 시 알림 전송

- 프로모션 알림 전송 

<br>

### 비교 기술

> #### 비동기 처리 필요성

동기 방식으로 메일 전송 시 **스레드 점유 문제** ➡︎ 메일 전송 처리동안 다른 작업 불가
<br>

**[@Async 비동기 처리 적용 후]**

- 요청 처리 시간: 16 min ➡︎ 1.7s로 대략 <ins>565배 개선</ins>
- 메일 전송 처리 시간: 16 min ➡︎ 2min으로 대략 <ins>8배 개선</ins>


➡ **비동기 처리의 필요성 인식**

<br>

> #### 순차적 처리 비교 

<div style="display: flex; justify-content: flex-start; gap: 20px;">
  <div style="text-align: center;">
    <img src="https://github.com/user-attachments/assets/3c3583d4-8ea9-47b6-a155-4cf73fbddc58" width="400" height="400">
  </div>
  <div style="text-align: center;">
    <img src="https://github.com/user-attachments/assets/4f284b3f-7001-40f2-90cd-e9c58371ec83" width="400" height="400">
  </div>
</div>

- Async: 랜덤 스레드 할당으로 **<ins>순서 미보장**
- RabbitMQ: Queue에 담긴 메세지가 요청된 **<ins>순서대로 처리** 

**본 기능에서 순서 보장은 고려 대상 ❌**

<br>

> #### 안정성

<details> <summary> <strong>RabbitMQ의 Queue 안정성</strong></summary>

**1️⃣ 100개의 메일 전송 요청 후, 앱 중단**

![Image](https://github.com/user-attachments/assets/2d696647-4df9-477e-ae3f-4a004895749b)

2️⃣ **메시지 상태 확인**

100개의 메세지가 publish(노란색)가 되고 consumer(초록색)가 10개의 메세지를 처리한 것을 보여준다.

![Image](https://github.com/user-attachments/assets/95101b2b-c89d-4264-a12c-7c162d4aabf7)


**3️⃣ Queue 상태 확인**

10개 요청 및 처리가 완료되고 Queue에 90개의 메세지가 남아있는 것을 확인

![Image](https://github.com/user-attachments/assets/38e40eb7-1ea0-4e1d-b1e2-b9bbfb2de468)


4️⃣ **메세지 확인**

Queue에 담긴 메세지 확인 (11 ~ 100까지 담겨있는 것을 확인)

![Image](https://github.com/user-attachments/assets/84950156-655f-4729-b26f-4d729581671b)

![Image](https://github.com/user-attachments/assets/2912226f-b0ee-40e5-be48-1524547ce05e)

**5️⃣ 앱 재실행 후**

순차적으로 메세지 처리 진행

![Image](https://github.com/user-attachments/assets/3a234fad-5fc9-47ad-86bb-73445203deb1)

6️⃣ 모든 처리 완료

Queue가 비워진 것을 확인

![Image](https://github.com/user-attachments/assets/26c6c0bd-5527-44b4-84c2-a961e7791af6)

![Image](https://github.com/user-attachments/assets/2d41ba76-0824-4964-a85c-3e89a026379b)

![Image](https://github.com/user-attachments/assets/207aa5fc-1248-4f30-9797-bbf048276f05)
</details>



<br>

> #### 처리 시간
**시나리오: 5분간 메일 발송**

**메일 서버(메일 전송)를 별도로 운영한다고 가정**하고 테스트했습니다.

- **@Async 방식**

  요청 시점부터 **메일 전송 완료**까지 모두 처리

- **RabbitMQ 방식**

  Producer가 **메시지를 브로커에 게시**하는 시점만 처리


➡️ **두 방식의 처리 범위가 달라** **정확한 처리 시간 비교가 어려울 수 있다는 점을 감안하여** 테스트를 진행하였습니다.


<br>

| 방식       |  메일 발송량 |
|----------|--- |
| Async    | 4840 | 
| RabbitMQ | 2784166 |

<br>

- `@Async 방식`은 Thread.sleep으로 지연을 주고, 이메일 전송까지 직접 처리
 
  ➡ 메시지만 큐에 담는 RabbitMQ 방식보다 느릴 수밖에 없음


- `RabbitMQ` 방식`은 이메일 전송이나 다른 무거운 작업을 본 서버에서 분리했을 때(멀티서버환경)
  **본 서버의 부담을 매우 줄여줄 수 있음**

  
<br>

### 의사결정 및 이유

- RabbitMQ 는 멀티서버 환경일 때 가장 잘 활용할 수 있음 (성능, 안정성)


- 본 프로젝트와 같이 `별도의 메일 전송 서버를 운영하지 않는 환경`에서는 RabbitMQ와 같은 **<ins>분산 메시징 시스템을 도입하는 것 오히려 과도**하다고 판단


- `@Async 방식`만으로도 주문 및 프로모션 메일을 사용자에게 전송하는 데 있어 **<ins>처리 시간 측면에서 큰 문제가 없다고 판단**


- **알림 전송은 데이터 유실이 발생하더라도 사용자에게 치명적인 영향을 주지 않기 때문에** @Async 방식을 사용해도 문제가 되지 않을 것이라고 판단

<br>

### 결론

**현재 환경에선 @Async 방식이 적절**한 선택

👉 추후 독립적인 메일 서버 구성 시, **RabbitMQ 도입 고려 가능**



</details>

<details> <summary> [💡<strong> 기술적 의사 결정] Elasticsearch vs RDBMS </strong></summary>

### 구현 기능
사용자가 원하는 도서를 찾기 위해 입력한 검색 키워드에 기반한 도서 검색 기능


<br>

### 비교 기술
- RDBMS
  - 특정 키워드가 포함된 데이터를 조회하기 위해 와일드 카드(%) 사용
  - 수직적인 확장 구조
  - 검색결과에 키워드 유사성을 반영하기 어려움


- Elasticsearch
  - 역색인 구조로 특정 문자열이 포함된 데이터 빠르게 조회
  - 수평적인 확장 구조
  - 키워드 유사성을 활용한 검색 결과 도출
  - 필드별 가중치 설정 가능

<br>

### 의사결정 및 이유
검색 기능 구현에서 기술 결정의 요소는 다음과 같이 정리됨
1. 검색 성능
2. 확장성
3. 키워드 유사성
4. 다중 필드 가중치

<br>

#### 검색 성능
RDBMS 의 경우 특정 키워드가 포함된 데이터를 조회하기 위해 와일드 카드(%)를 사용함

예시)
```java
select * from books where title like '%연구 개발%' and is_deleted = false;
```

키워드를 '포함한' 데이터를 조회하려면 키워드 양 옆에 와일드 카드를 사용해야하는데
이는 모든 데이터를 탐색하는 풀스캔이 동반됨

---
반면 Elasticsearch 는 역색인 구조로 빠르게 검색할 수 있음

<div>
    <img src="https://cdn.gamma.app/1r9zmh8f8xmhk2w/0cd621c930094c1699b799750fcf461d/original/image.png" alt="RDBMS Book Information Document" width="600" height="400">
</div>

---

> 책 데이터 약 22만개를 기준으로 검색 성능 비교

<div style="display: flex; justify-content: flex-start; gap: 20px;">
  <div style="text-align: center;">
    <img src="https://cdn.gamma.app/1r9zmh8f8xmhk2w/e9abe0eea57a44989ed7c420a2b95d5e/original/image.png" alt="RDBMS Book Information Document" width="300" height="400">
  </div>
  <div style="text-align: center;">
    <img src="https://cdn.gamma.app/1r9zmh8f8xmhk2w/ce2e0bcc48404ec781ce55b5067355cb/original/image.png" alt="Elasticsearch Book Information Document" width="300" height="400">
  </div>
</div>

- **정리 : Elasticsearch 가 성능적으로 더 우세. 데이터 수가 갈 수록 늘어날 것을 고려하면 차이가 더 크게 벌어질 것으로 예상됨**

---

<br>

#### 확장성
시간이 흐름에 따라 출간되는 도서는 계속해서 늘어나게됨

그에 따라 온라인 서점 서비스의 도서 데이터도 증가할 것이고 확장성을 고려해야함

- **정리 : 수직적 확장을 기반으로한 RDBMS 보다 수평적 확장이 용이한 Elasticsearch 가 적절**

---

<br>

#### 키워드 유사성

사용자가 특정 키워드로 검색했을 때 해당 키워드를 포함한 데이터가 없다면

비슷한 값을 가진 데이터라도 조회 가능하게 하는 것이 사용자 경험 측면에서 도움될 것

```java
select * from books b 
         where b.title like '%부모의 사랑%' or 
               b.author like '%부모의 사랑%' or 
               b.publisher like '%부모의 사랑%' or
               b.category like '%부모의 사랑%' and
               is_deleted = false
```

위 SQL 로 조회 시 일치하는 데이터를 찾을 수 없었음

<div>
    <img src="https://cdn.gamma.app/1r9zmh8f8xmhk2w/193c3acaef88470891e56727f461c5a3/original/image.png" alt="RDBMS Book Information Document" width="500" height="300">
</div>


Elasticsearch 의 형태소 분석기를 통한 키워드 유사성을 반영하여 도서 검색 결과

<div style="display: flex; justify-content: flex-start; gap: 20px;">
  <div style="text-align: center;">
    <img src="https://cdn.gamma.app/1r9zmh8f8xmhk2w/8681dd21e93947d8b5e852aa19ee595b/original/image.png" alt="RDBMS Book Information Document" width="400" height="400">
  </div>
  <div style="text-align: center;">
    <img src="https://cdn.gamma.app/1r9zmh8f8xmhk2w/ba113ad6b32c4bddbe9357cd33ea198e/original/image.png" alt="Elasticsearch Book Information Document" width="400" height="400">
  </div>
</div>

- **정리 : Elasticsearch 적용한 검색 결과에서 검색 키워드를 그대로 포함한 데이터는 없지만 비슷한 키워드를 가진 데이터를 조회할 수 있었음**

---

<br>

#### 다중 필드 가중치

사용자가 책을 찾는 기준은 제목, 저자, 카테고리, 출판사 모두 될 수 있다

일반적으로는 책이나 저자를 검색 기준으로 삼는 편

따라서 주요 기준이 되는 필드에 가중치를 줘서 점수가 높은 순대로 검색 결과를 정렬하면

사용자가 원하는 데이터를 찾아보기 좋다 -> 사용자 경험(UX) 향상

이를 적용하기 위해서는 Elasticsearch 를 도입해야함

<div style="display: flex; justify-content: flex-start; gap: 20px;">
  <div style="text-align: center;">
    <img src="https://cdn.gamma.app/1r9zmh8f8xmhk2w/9bee6b7a760d44a48e8f57218f852ffa/original/image.png" alt="RDBMS Book Information Document" width="400" height="200">
    <br><span> title - 5배</span><br>
    <span> author - 3배</span><br>
    <span> category - 2배</span><br>
    <span> publisher - 1배</span><br><br>

  </div>
  <div style="text-align: center;">
    <img src="https://cdn.gamma.app/1r9zmh8f8xmhk2w/01f8dd7fc7754d13898321df2a644353/original/image.png" alt="Elasticsearch Book Information Document" width="400" height="400">
    <br><span> 가중치 적용 결과 </span>
  </div>
</div>


<br>

### 결론
사용자 경험 측면과 확장성, 현재 프로젝트의 진행 상황을 고려했을 때

다음 기술 결정의 요소 모두 Elasticsearch 가 적절하다고 판단.

1. 검색 성능
2. 확장성
3. 키워드 유사성
4. 다중 필드 가중치

### **[Elasticsearch 도입 결정]**
</details>



<br>


## 인프라 아키텍처 & 기술 스택

### 🏰 인프라 아키텍처

<img width="4240" alt="Image" src="https://github.com/user-attachments/assets/16a61e7e-89f4-4dfb-8202-f641af8c1c24" />


<br>

### 💎 기술 스택

###  Back-end
<img src="https://img.shields.io/badge/Java-007396?style=for-the-badge&logo=OpenJDK&logoColor=white">&nbsp;
<img src="https://img.shields.io/badge/Spring Boot-6DB33F?style=for-the-badge&logo=springboot&logoColor=white">&nbsp;
<img src="https://img.shields.io/badge/Gradle-02303A?style=for-the-badge&logo=gradle&logoColor=white">&nbsp;
<img src="https://img.shields.io/badge/MySQL-4479A1?style=for-the-badge&logo=mysql&logoColor=white">&nbsp;
<img src="https://img.shields.io/badge/Redis-DC382D?style=for-the-badge&logo=redis&logoColor=white">&nbsp;
<img src="https://img.shields.io/badge/postman-E34F26?style=for-the-badge&logo=postman&logoColor=white">&nbsp;
<img src="https://img.shields.io/badge/jwt-F80000?style=for-the-badge&logo=json web tokens&logoColor=white">&nbsp;
<br>
<img src="https://img.shields.io/badge/rabbitMQ-47A248?style=for-the-badge&logo=rabbitMQ&logoColor=white">&nbsp;
<img src="https://img.shields.io/badge/elasticsearch-005571?style=for-the-badge&logo=html5&logoColor=white">
<img src="https://img.shields.io/badge/h2-7952B3?style=for-the-badge&logo=h2&logoColor=white">&nbsp;
<img src="https://img.shields.io/badge/spring security-000000?style=for-the-badge&logo=spring security&logoColor=white">&nbsp;
<img src="https://img.shields.io/badge/spring data jpa-092E20?style=for-the-badge&logoColor=white">&nbsp;
<img src="https://img.shields.io/badge/junit5-4053D6?style=for-the-badge&logo=junit5&logoColor=white">

###  Front-end
<img src="https://img.shields.io/badge/html5-E34F26?style=for-the-badge&logo=html5&logoColor=white">&nbsp;
<img src="https://img.shields.io/badge/springboot web-6DB33F?style=for-the-badge&logoColor=white">&nbsp;
<img src="https://img.shields.io/badge/javascript-F7DF1E?style=for-the-badge&logo=javascript&logoColor=black">&nbsp;
<img src="https://img.shields.io/badge/thymeleaf-7952B3?style=for-the-badge&logo=Thymeleaf&logoColor=white">&nbsp;


### Infra
<img src="https://img.shields.io/badge/ec2-DC382D?style=for-the-badge&logo=amazonec2&logoColor=white">&nbsp;
<img src="https://img.shields.io/badge/github actions-A86454?style=for-the-badge&logo=githubactions&logoColor=white">&nbsp;
<img src="https://img.shields.io/badge/docker-DD0031?style=for-the-badge&logo=docker&logoColor=white">&nbsp;
<img src="https://img.shields.io/badge/load balancing-F7DF1E?style=for-the-badge&logo=awselasticloadbalancing&logoColor=black">&nbsp;
<img src="https://img.shields.io/badge/route 53-4053D6?style=for-the-badge&logo=amazon route 53&logoColor=white">&nbsp;
<img src="https://img.shields.io/badge/elastic beanstalk-색상?style=for-the-badge&logo=기술스택아이콘&logoColor=white">
<br>
<img src="https://img.shields.io/badge/iam-010101?style=for-the-badge&logoColor=white">&nbsp;
<img src="https://img.shields.io/badge/ecr-DC382D?style=for-the-badge&logo=ecr&logoColor=white">
<img src="https://img.shields.io/badge/s3-569A31?style=for-the-badge&logo=amazons3&logoColor=white">
<img src="https://img.shields.io/badge/google smtp-F80000?style=for-the-badge&logo=google&logoColor=white">&nbsp;
<img src="https://img.shields.io/badge/amazon ses-DD344C?style=for-the-badge&logo=amazonsimpleemailservice&logoColor=white">
<img src="https://img.shields.io/badge/amazon rds-527FFF?style=for-the-badge&logo=amazonrds&logoColor=white">

### Monitoring Tool
<img src="https://img.shields.io/badge/kibana-005571?style=for-the-badge&logo=kibana&logoColor=white">&nbsp;
<img src="https://img.shields.io/badge/cloudwatch-FF4F8B?style=for-the-badge&logo=amazoncloudwatch&logoColor=white">


### Collaborative Tool
<img src="https://img.shields.io/badge/IntelliJ IDEA-000000?style=for-the-badge&logo=IntelliJ IDEA&logoColor=white">&nbsp;
<img src="https://img.shields.io/badge/Github-181717?style=for-the-badge&logo=github&logoColor=white">&nbsp;
<img src="https://img.shields.io/badge/git-F05032?style=for-the-badge&logo=git&logoColor=white">&nbsp;
<img src="https://img.shields.io/badge/Slack-4A154B?style=for-the-badge&logo=Slack&logoColor=white">&nbsp;
<img src="https://img.shields.io/badge/notion-4053D6?style=for-the-badge&logo=notion&logoColor=white">&nbsp;
<img src="https://img.shields.io/badge/figma-339AF0?style=for-the-badge&logo=figma&logoColor=white">&nbsp;
<img src="https://img.shields.io/badge/zep-7952B3?style=for-the-badge&logoColor=white">&nbsp;
<img src="https://img.shields.io/badge/canva-00C4CC?style=for-the-badge&logo=canva&logoColor=white">&nbsp;

<br>


## 성과 및 회고

### 👍🏻 Keep

- 테스트 코드 작성으로 커버리지 56% 달성

- 정기적인 스크럼으로 커뮤니케이션 원활

- github 코드리뷰 및 slack 워크스페이스 등 협업 툴 활용

- 다양한 개발 기술 경험

<br>

### 🗓️ Try

- 서비스 완성도 보완

- 멀티서버 구축 후 RabbitMQ 관련

- 테스트 재시도

- 코드 리팩토링을 통한 성능 개선

- 이미지 관련 동시성 테스트 진행

- 출판사 권한 확대

- 익명 사용자 로그인 추가


<br>


## 역할 분담 및 협업 방식

### 🧑🏻‍🧑🏻‍ 역할 분담

| 이름/역할                              | 주요 담당 업무                                                                             |
|------------------------------------|--------------------------------------------------------------------------------------|
| 👑 **전승민 (리더)**<br>[GitHub](https://github.com/Seungmin-J)<br>[Blog](https://velog.io/@seung103/posts)  | ⚙️ 도서 검색<br>⚙️ 베스트셀러<br>⚙️ 인기 검색어<br>⚙️ 알림 |
| 👑 **서지원 (부리더)**<br>[GitHub](https://github.com/jiwonclvl)<br>[Blog ](https://velog.io/@clvl1004/posts)| ⚙️ 도서 및 리뷰<br>⚙️ 이미지<br>⚙️ 네이버 로그인<br>⚙️ 알림                      |
| 👑 **이지은**<br>[GitHub](https://github.com/queenriwon)<br>[Blog  ](https://queenriwon3.tistory.com/)     | ⚙️ 인증 인가<br>⚙️ 구글 로그인<br>⚙️ 주문 및 장바구니<br>⚙️ 결 제                     |
| 👑 **이호수**<br>[GitHub](https://github.com/Hokirby)<br>[Blog  ](https://lakevely27.tistory.com/)     | ⚙️ CI / CD<br>⚙️ 인프라 구축<br>⚙️ 카카오 로그인 <br>                  |
| 👑 **조은종**<br>[GitHub](https://github.com/Roloya28)<br>[Blog](https://cej4297.tistory.com/)       | ⚙️ 한정판 행사<br>⚙️ 한정판 예약<br>⚙️ 로그|


<br>

### 🌱 Ground Rule

![Image](https://github.com/user-attachments/assets/53ec2e76-9f3d-44dc-8e7e-1a349afcfba0)


![Image](https://github.com/user-attachments/assets/0db4db31-067a-4fbf-90e1-8522d6067c82)
