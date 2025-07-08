# 결제 시스템 설계 제안 (v2)

이 문서는 `GEMINI.md`의 업데이트된 가이드라인(유스케이스 패턴 도입, 계층별 책임 재정의)을 반영하여 복잡한 결제 시스템을 구축하기 위한 새로운 설계 방안을 제안합니다.

## 1. 목표

- **안전한 도메인 모델**: 11가지 결제 조합을 타입 시스템으로 완벽하게 표현하여 컴파일 시점에 안정성을 확보합니다.
- **유스케이스 중심 아키텍처**: Clean Architecture를 기반으로, 각 비즈니스 유스케이스를 명확히 분리하여 응집도 높고 재사용 가능한 구조를 만듭니다.
- **TDD 기반 개발**: 테스트 주도 개발을 통해 시스템의 안정성과 코드 품질을 확보합니다.

## 2. 도메인 모델 설계 (Domain Layer)

도메인 모델의 구조 자체는 이전 설계와 동일하게 유지됩니다. `sealed interface`를 활용한 ADT(대수적 데이터 타입) 설계는 새로운 가이드라인에서도 여전히 유효하며, **안전한 도메인 모델** 원칙의 핵심입니다.

### 2.1. 클래스 다이어그램 (Mermaid)

```mermaid
classDiagram
    direction LR
    
    <<sealed interface>> PaymentMethod

    <<sealed interface>> SinglePayment
    <<data>> NPayPointCompositePayment
    <<data>> NPayMoneyCompositePayment
    <<data>> NPayPointAndMoneyCompositePayment

    PaymentMethod <|-- SinglePayment
    PaymentMethod <|-- NPayPointCompositePayment
    PaymentMethod <|-- NPayMoneyCompositePayment
    PaymentMethod <|-- NPayPointAndMoneyCompositePayment

    class NPayPointPayment
    class NPayMoneyPayment
    class CardEasyPayment
    class BankEasyPayment

    SinglePayment <|-- NPayPointPayment
    SinglePayment <|-- NPayMoneyPayment
    SinglePayment <|-- CardEasyPayment
    SinglePayment <|-- BankEasyPayment

    <<sealed interface>> NPayPointOtherPayment
    NPayPointCompositePayment o-- "1" NPayPointPayment
    NPayPointCompositePayment o-- "1" NPayPointOtherPayment
    NPayPointOtherPayment <|-- NPayMoneyPayment
    NPayPointOtherPayment <|-- CardEasyPayment
    NPayPointOtherPayment <|-- BankEasyPayment

    <<sealed interface>> NPayMoneyOtherPayment
    NPayMoneyCompositePayment o-- "1" NPayMoneyPayment
    NPayMoneyCompositePayment o-- "1" NPayMoneyOtherPayment
    NPayMoneyOtherPayment <|-- CardEasyPayment
    NPayMoneyOtherPayment <|-- BankEasyPayment

    <<sealed interface>> NPayPointAndMoneyOtherPayment
    NPayPointAndMoneyCompositePayment o-- "1" NPayPointPayment
    NPayPointAndMoneyCompositePayment o-- "1" NPayMoneyPayment
    NPayPointAndMoneyCompositePayment o-- "1" NPayPointAndMoneyOtherPayment
    NPayPointAndMoneyOtherPayment <|-- CardEasyPayment
    NPayPointAndMoneyOtherPayment <|-- BankEasyPayment
```

## 3. 계층형 아키텍처 설계 (Use Case 중심)

`GEMINI.md`의 새로운 **계층형 아키텍처** 원칙에 따라, 유스케이스를 중심으로 시스템을 재설계합니다.

### 3.1. 아키텍처 다이어그램 (Mermaid)

```mermaid
graph TD
    subgraph Interfaces Layer
        A[PaymentController] --> B{PaymentRequestDto};
    end

    subgraph Application Layer
        C[ProcessPaymentUseCase] --> D{Request (nested)};
        C --> E{Response (nested)};
    end

    subgraph Domain Layer
        F[PaymentMethod]
        G[PaymentRepository (Port)]
        I[RewardPointCalculator]
    end

    subgraph Infrastructure Layer
        H[InMemoryPaymentRepository (Adapter)]
    end

    A -- invokes --> C;
    C -- uses --> G;
    C -- uses --> I;
    C -- creates/updates --> F;
    G <|-- H;
```

### 3.2. 계층별 책임 (Updated)

- **`interfaces` (표현 계층)**:
    - `PaymentController`: 외부 HTTP 요청을 수신합니다. 불안전한 `PaymentRequestDto`를 `ProcessPaymentUseCase.Request` 객체로 변환한 후, 해당 유스케이스를 실행합니다. 유스케이스 실행 결과인 `ProcessPaymentUseCase.Response`를 클라이언트에게 반환할 DTO로 변환하여 응답합니다.
- **`application` (애플리케이션 계층)**:
    - `ProcessPaymentUseCase`: **결제 처리**라는 단일 유스케이스를 캡슐화합니다.
        - `Request` (nested data class): 유스케이스 실행에 필요한 입력 데이터입니다. (예: `userId`, `orderId`, `paymentInfo`)
        - `Response` (nested data class): 유스케이스 실행 결과를 담는 데이터입니다. (예: `paymentId`, `status`, `rewardPoints`)
        - **책임**: `PaymentRepository`를 통해 `PaymentMethod` 애그리게잇을 조회하거나 생성하고, 도메인 객체의 행위(메서드)를 호출합니다. `RewardPointCalculator` 같은 순수 도메인 서비스를 사용하여 비즈니스 로직을 수행하고, 모든 변경사항을 트랜잭션 내에서 `PaymentRepository`를 통해 저장합니다.
- **`domain` (도메인 계층)**:
    - `PaymentMethod`: 결제 방식에 대한 순수한 비즈니스 규칙과 상태를 가지는 애그리게잇입니다.
    - `PaymentRepository` (Port): 애그리게잇의 영속성을 위한 **명세(interface)** 입니다. 구현 기술에 대해 전혀 알지 못합니다.
    - `RewardPointCalculator`: 특정 결제 방식(`PaymentMethod`)에 따라 적립금을 계산하는 **순수 도메인 서비스**입니다. 상태를 가지지 않으며, 리포지토리에 의존하지 않고 오직 파라미터로 전달된 도메인 객체만으로 동작합니다.
- **`infrastructure` (인프라 계층)**:
    - `InMemoryPaymentRepository` (Adapter): `domain` 계층에 정의된 `PaymentRepository` 인터페이스의 In-Memory 구현체입니다.

## 4. TDD 기반 구현 계획 (v2)

변경된 아키텍처에 맞춰 TDD 개발 단계를 재구성합니다.

### Step 1: Domain Layer 구현
1.  **테스트 작성**: `PaymentMethod`의 각 조합이 도메인 규칙에 맞게 생성되는지 검증하는 단위 테스트를 작성합니다.
2.  **도메인 모델 구현**: 테스트를 통과하도록 `PaymentMethod` 관련 `sealed interface`와 `data class`를 구현합니다.
3.  **테스트 작성**: `RewardPointCalculator`가 다양한 `PaymentMethod`에 대해 정확한 적립금을 계산하는지 검증하는 단위 테스트를 작성합니다.
4.  **도메인 서비스 구현**: 테스트를 통과하도록 `RewardPointCalculator`를 구현합니다. (리포지토리 의존성 없음)
5.  **리포지토리 인터페이스 정의**: `PaymentRepository` 인터페이스(Port)를 정의합니다.

### Step 2: Infrastructure Layer 구현
1.  **테스트 작성**: `InMemoryPaymentRepository`가 `PaymentMethod`를 저장하고 조회하는 기능을 정확히 수행하는지 검증하는 통합 테스트를 작성합니다.
2.  **리포지토리 구현**: 테스트를 통과하도록 `InMemoryPaymentRepository`를 구현합니다.

### Step 3: Application Layer (UseCase) 구현
1.  **유스케이스 및 입출력 정의**: `ProcessPaymentUseCase` 클래스와 그 내부에 `Request`, `Response` `data class`를 정의합니다.
2.  **테스트 작성**: `ProcessPaymentUseCase`에 대한 단위 테스트를 작성합니다. 이 테스트는 **Fake `PaymentRepository`** 를 주입받아 다음을 검증합니다:
    - 주어진 `Request`에 따라 올바른 `PaymentMethod` 애그리게잇이 생성되는가?
    - `RewardPointCalculator`가 올바르게 호출되는가?
    - 애그리게잇의 상태 변경 후 `paymentRepository.save()`가 호출되는가?
    - 예상된 `Response` 객체가 반환되는가?
3.  **유스케이스 구현**: 테스트를 통과하도록 `ProcessPaymentUseCase`의 `invoke` 또는 `execute` 메서드를 구현합니다.

### Step 4: Interfaces Layer 구현
1.  **테스트 작성**: `PaymentController`에 대한 통합 테스트를 작성합니다. Mocking된 `ProcessPaymentUseCase`를 사용하여 다음을 검증합니다:
    - HTTP 요청 DTO가 `ProcessPaymentUseCase.Request`로 정확히 매핑되는가?
    - `ProcessPaymentUseCase`가 정확한 `Request`와 함께 호출되는가?
    - `ProcessPaymentUseCase`가 반환한 `Response`가 예상된 HTTP 응답 DTO로 변환되는가?
2.  **컨트롤러 구현**: 테스트를 통과하도록 `PaymentController`를 구현합니다.
