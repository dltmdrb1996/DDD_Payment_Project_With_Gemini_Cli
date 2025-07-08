# 결제 시스템 설계 제안

이 문서는 제공된 요구사항과 `GEMINI.md` 개발 가이드라인에 따라 복잡한 결제 시스템을 구축하기 위한 설계 방안을 제안합니다.

## 1. 목표

- **안전한 도메인 모델 구축**: 11가지의 복잡한 결제 조합을 타입 시스템으로 완벽하게 표현하여, 존재할 수 없는 결제 상태는 컴파일 시점에 원천 차단합니다.
- **유연하고 확장 가능한 구조**: Clean Architecture를 적용하여 각 계층의 책임을 명확히 분리하고, 향후 새로운 결제 수단이나 비즈니스 규칙 변경에 유연하게 대응할 수 있는 구조를 만듭니다.
- **TDD 기반 개발**: 테스트 주도 개발을 통해 안정성과 코드 품질을 확보하며, 명세가 명확한 코드를 작성합니다.

## 2. 도메인 모델 설계 (Domain Layer)

`GEMINI.md`의 **안전한 도메인 모델(Safe Domain Model)** 원칙에 따라, Kotlin의 `sealed interface`와 `data class`를 사용하여 11가지 결제 방식을 모델링합니다.

### 2.1. 결제 수단 (PaymentMethod) 모델링

- **최상위 인터페이스**: `PaymentMethod`를 `sealed interface`로 정의하여 모든 결제 방식의 최상위 타입 역할을 합니다.
- **카테고리 분리**: 결제 방식을 크게 4가지 카테고리(단일, 포인트 복합, 머니 복합, 포인트+머니 복합)로 구분하고, 각각을 `sealed interface` 또는 `data class`로 모델링합니다.
- **값 객체(Value Object) 활용**: 금액, 포인트 등 중요한 데이터는 `value class`로 포장하여 원시 타입의 오용을 방지하고, 생성 시점에 유효성 검사를 수행합니다.

### 2.2. 클래스 다이어그램 (Mermaid)

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

    class NPayPointPayment {
        +amount: Long
    }
    class NPayMoneyPayment {
        +amount: Long
    }
    class CardEasyPayment {
        +amount: Long
        +cardInfo: String
    }
    class BankEasyPayment {
        +amount: Long
        +bankInfo: String
    }

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

## 3. 계층형 아키텍처 설계 (Clean Architecture)

`GEMINI.md`의 **계층형 아키텍처** 원칙에 따라 시스템을 4개의 계층으로 분리합니다.

### 3.1. 아키텍처 다이어그램 (Mermaid)

```mermaid
graph TD
    subgraph Presentation Layer
        A[PaymentController] --> B{PaymentRequestDto};
    end

    subgraph Application Layer
        C[PaymentFacade] --> D{PaymentService};
        D --> E[PaymentMethodMapper];
    end

    subgraph Domain Layer
        F[PaymentMethod]
        G[PaymentRepository]
    end

    subgraph Infrastructure Layer
        H[InMemoryPaymentRepository]
    end

    A --> C;
    C --> F;
    C --> G;
    G <|-- H;
```

### 3.2. 계층별 책임

- **Presentation Layer**:
    - `PaymentController`: 외부 HTTP 요청을 받아 `Application Layer`에 위임합니다.
    - `PaymentRequestDto`: 외부의 불안전한 데이터를 받는 DTO입니다. 유효성 검증 전입니다.
- **Application Layer**:
    - `PaymentFacade`: 여러 애플리케이션 서비스나 도메인 서비스를 조합하여 하나의 완전한 유스케이스를 제공합니다. (예: 결제 처리 및 결과 통보)
    - `PaymentService`: 핵심 비즈니스 로직을 수행합니다. DTO를 안전한 도메인 모델로 변환하고, `PaymentRepository`를 통해 영속성을 처리합니다.
    - `PaymentMethodMapper`: DTO를 `PaymentMethod` 도메인 객체로 변환하는 책임을 가집니다.
- **Domain Layer**:
    - `PaymentMethod`: 위에서 설계한 결제 방식 모델입니다. 순수한 비즈니스 규칙을 담습니다.
    - `PaymentRepository`: `PaymentMethod` 객체의 영속성을 추상화하는 인터페이스입니다.
- **Infrastructure Layer**:
    - `InMemoryPaymentRepository`: `PaymentRepository`의 In-Memory 구현체입니다. 초기 개발 및 테스트에 사용됩니다.

## 4. TDD 기반 구현 계획 (Step-by-step)

`GEMINI.md`의 **테스트 주도 개발(TDD)** 원칙에 따라 다음 순서로 개발을 진행합니다.

### Step 1: Domain Layer 구현
1.  **테스트 작성**: `PaymentMethod`의 각 조합이 도메인 규칙에 맞게 생성되는지 검증하는 단위 테스트를 작성합니다. (예: `NPayPointCompositePayment`는 반드시 `NPayPointPayment`와 `NPayPointOtherPayment`를 가져야 함)
2.  **도메인 모델 구현**: 테스트를 통과하도록 `PaymentMethod` 관련 `sealed interface`와 `data class`를 구현합니다. `value class`를 사용하여 금액, 포인트 등을 포장합니다.
3.  **리포지토리 인터페이스 정의**: `PaymentRepository` 인터페이스를 정의합니다.

### Step 2: Infrastructure Layer 구현
1.  **테스트 작성**: `InMemoryPaymentRepository`가 `PaymentMethod`를 저장하고 조회하는 기능을 정확히 수행하는지 검증하는 통합 테스트를 작성합니다.
2.  **리포지토리 구현**: 테스트를 통과하도록 `InMemoryPaymentRepository`를 구현합니다.

### Step 3: Application Layer 구현
1.  **테스트 작성**: `PaymentRequestDto`가 `PaymentMethod` 도메인 모델로 정확히 변환되는지 검증하는 `PaymentMethodMapper` 단위 테스트를 작성합니다.
2.  **매퍼 구현**: 테스트를 통과하도록 `PaymentMethodMapper`를 구현합니다.
3.  **테스트 작성**: `PaymentService`가 주어진 `PaymentMethod`에 따라 적절한 비즈니스 로직(예: 결제 처리, 적립금 계산)을 수행하는지 검증하는 단위 테스트를 작성합니다. 이 때 `PaymentRepository`는 Fake Object(가짜 구현체)를 사용하여 테스트합니다.
4.  **서비스 구현**: 테스트를 통과하도록 `PaymentService`를 구현합니다.

### Step 4: Presentation Layer 구현
1.  **테스트 작성**: `PaymentController`가 특정 HTTP 요청을 받았을 때, `PaymentFacade`를 호출하고 예상된 HTTP 응답(성공/실패)을 반환하는지 검증하는 통합 테스트를 작성합니다.
2.  **컨트롤러 구현**: 테스트를 통과하도록 `PaymentController`와 관련 DTO를 구현합니다.

이러한 단계적 접근을 통해 각 컴포넌트의 역할을 명확히 하고, 테스트를 통해 시스템의 안정성을 점진적으로 확보해 나갈 수 있습니다.
