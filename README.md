
# 🚀 BoilerPlate

## 📌 프로젝트 개요
초기 개발 환경을 빠르게 구성할 수 있도록 **자주 반복되는 코드를 공통 프로젝트로 미리 개발**합니다.  
이를 통해 다른 프로젝트에서도 재사용이 가능하며, 지속적으로 개선하고 기존 기능을 업데이트하며,  
새로운 기능을 계속 추가해 더 나은 공통 코드를 제공합니다.

---

## 📝 컨벤션

### ✅ Commit 메세지 컨벤션
```

<타입>: 내용 #이슈번호

```

| 타입      | 설명                                                                 |
|-----------|----------------------------------------------------------------------|
| feat      | 기능 (새로운 기능)                                                   |
| fix       | 버그 (버그 수정)                                                     |
| refactor  | 리팩토링                                                              |
| design    | CSS 등 사용자 UI 디자인 변경                                          |
| comment   | 필요한 주석 추가 및 변경                                              |
| style     | 스타일 (코드 형식, 세미콜론 추가 등, **비즈니스 로직에 변경 없음**)   |
| docs      | 문서 수정 (문서 추가, 수정, 삭제, README)                             |
| test      | 테스트 코드 추가/수정/삭제 (**비즈니스 로직에 변경 없음**)            |
| chore     | 기타 변경사항 (빌드 스크립트, assets, 패키지 매니저 등)               |
| init      | 초기 생성                                                             |
| rename    | 파일/폴더명 수정 및 이동                                              |
| remove    | 파일 삭제                                                             |

---

### 🔀 Merge Commit 메세지
- `merge: 브렌치 -> 타겟 브렌치`  
  예: `merge: develop -> feature/user-login`

- `merge: 타겟 브렌치 <- 브렌치`  
  예: `merge: main <- develop`

---

### 🧪 테스트 메서드 네이밍 규칙
```

should + 동작 + 조건

```
예:
- `shouldCreateResponseWhenDataGiven`
- `shouldFailLoginWhenPasswordIsInvalid`

---

### 🌲 Git 브랜치 전략
- 기본 브랜치: `main` → `develop`
- 작업 브랜치:
    - 기능 추가: `feature/작업명`
    - 리팩토링: `refactor/작업명`  


