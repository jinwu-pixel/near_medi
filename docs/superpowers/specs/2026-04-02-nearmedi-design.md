# 니어메디 (NearMedi) — 설계 문서

## 개요

내 위치 기반으로 가까운 병원/약국을 Google Maps에 표시하고 목록으로 보여주는 Android 앱.

## 범위

- **대상:** 네이티브 Android 앱 (Kotlin)
- **v1 기능:** 내 위치 기반 병원/약국 목록 + 지도 표시
- **비대상 (v2 이후):** 진료과목 필터링, 상세 정보, 즐겨찾기, 리뷰

## 데이터 소스

**국립중앙의료원 전국 병·의원 찾기 서비스**
- 공공데이터포털: https://www.data.go.kr/data/15000736/openapi.do
- 기본 URL: `http://apis.data.go.kr/B552657/HsptlAsembySearchService`
- 오퍼레이션: `getHsptlMdcncListInfoInqire`
- 데이터 포맷: XML
- 비용: 무료
- 업데이트: 실시간

### 요청 파라미터

| 파라미터 | 설명 | 예시 |
|---------|------|------|
| `serviceKey` | API 인증키 (필수) | 발급받은 키 |
| `Q0` | 시도 | 서울특별시 |
| `Q1` | 시군구 | 강남구 |
| `QZ` | 기관구분 (B:병원, C:의원) | B |
| `QD` | 진료과목 (D001~D029) | D001 |
| `QT` | 진료요일 (1~7, 공휴일8) | 1 |
| `QN` | 기관명 | 삼성병원 |
| `ORD` | 정렬순서 | NAME |
| `pageNo` | 페이지 번호 | 1 |
| `numOfRows` | 목록 건수 | 10 |

### 응답 필드 (예상, API 호출로 확인 필요)

| 필드 | 설명 |
|------|------|
| `dutyName` | 기관명 |
| `dutyAddr` | 주소 |
| `dutyTel1` | 대표 전화번호 |
| `wgs84Lon` | 경도 (longitude) |
| `wgs84Lat` | 위도 (latitude) |
| `dutyDiv` | 기관 구분 |
| `dutyTime*` | 진료시간 |

**참고:** 정확한 응답 필드는 API 키 발급 후 실제 호출로 확인 필요. 개발 초기에 테스트 호출로 필드 목록을 확정한다.

## 기술 스택

| 항목 | 기술 |
|------|------|
| 언어 | Kotlin |
| 최소 SDK | 26 (Android 8.0) |
| 지도 | Google Maps SDK for Android |
| HTTP | Retrofit 2 + OkHttp |
| XML 파싱 | Retrofit XML Converter (SimpleXML 또는 TikXml) |
| 위치 | Google Play Services FusedLocationProviderClient |
| 아키텍처 | MVVM (ViewModel + LiveData) |
| 빌드 | Gradle (Kotlin DSL) |

## 화면 구성

### 메인 화면 (단일 화면)

```
┌──────────────────────────┐
│      Google Maps          │
│   (내 위치 + 마커들)       │
│   마커 탭 → 말풍선         │
│      (이름/주소)           │
│                           │
├──────────────────────────┤
│  🏥 서울내과의원           │
│     서울시 강남구 ... 0.3km│
├──────────────────────────┤
│  🏥 강남세브란스           │
│     서울시 강남구 ... 0.5km│
├──────────────────────────┤
│  🏥 삼성서울병원           │
│     서울시 강남구 ... 1.2km│
└──────────────────────────┘
```

- **상단:** Google Maps Fragment (화면 약 50%)
  - 내 위치 파란 점 표시
  - 병원/약국 마커 표시
  - 마커 탭 시 이름 + 주소 말풍선 (InfoWindow)
- **하단:** RecyclerView 목록 (화면 약 50%)
  - 기관명, 주소, 내 위치로부터의 거리
  - 거리순 정렬

### 동작 흐름

1. 앱 시작 → 위치 권한 요청
2. 권한 허용 → 현재 위치 획득 (FusedLocationProvider)
3. 현재 위치의 시도/시군구를 역지오코딩으로 추출
4. API 호출 (Q0=시도, Q1=시군구)
5. 응답 XML 파싱 → Hospital 리스트
6. 각 Hospital의 좌표로 거리 계산 → 거리순 정렬
7. Google Maps에 마커 표시 + RecyclerView에 목록 표시

### 위치 → 시도/시군구 변환

API가 좌표 기반 검색을 지원하지 않으므로, 현재 위치 좌표를 시도/시군구 문자열로 변환해야 한다:
- Android Geocoder API 사용 (`Geocoder.getFromLocation()`)
- 위도/경도 → 주소 → 시도(adminArea), 시군구(locality 또는 subAdminArea) 추출

## 프로젝트 구조

```
app/src/main/java/com/nearmedi/
├── data/
│   ├── api/
│   │   └── HospitalApiService.kt   — Retrofit 인터페이스
│   ├── model/
│   │   └── Hospital.kt             — 데이터 모델 + XML 매핑
│   └── repository/
│       └── HospitalRepository.kt   — API 호출 + 데이터 변환
├── ui/
│   ├── MainActivity.kt             — 지도 + 목록 화면
│   ├── MainViewModel.kt            — 비즈니스 로직
│   └── HospitalAdapter.kt          — RecyclerView 어댑터
└── util/
    └── LocationHelper.kt           — 위치 권한 + 좌표 획득 + 역지오코딩

app/src/main/res/
├── layout/
│   ├── activity_main.xml           — 지도 + 목록 레이아웃
│   └── item_hospital.xml           — 목록 아이템 레이아웃
└── values/
    └── strings.xml
```

## 필요한 API 키

1. **공공데이터포털 API 키** — data.go.kr에서 발급 (무료)
2. **Google Maps API 키** — Google Cloud Console에서 발급

두 키 모두 `local.properties` 또는 `BuildConfig`에 저장하여 소스코드에 노출되지 않게 관리.

## 에러 처리

- **위치 권한 거부:** 권한 필요 안내 다이얼로그 표시
- **위치 획득 실패:** "위치를 확인할 수 없습니다" 메시지 + 재시도 버튼
- **API 호출 실패:** "데이터를 불러올 수 없습니다" 메시지 + 재시도 버튼
- **결과 없음:** "주변에 병원/약국이 없습니다" 메시지

## 권한

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
```

## 의존성

```kotlin
// Google Maps
implementation("com.google.android.gms:play-services-maps:18.2.0")
implementation("com.google.android.gms:play-services-location:21.1.0")

// Retrofit + XML
implementation("com.squareup.retrofit2:retrofit:2.9.0")
implementation("com.squareup.retrofit2:converter-simplexml:2.9.0")
implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

// ViewModel + LiveData
implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")
implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.7.0")
```
