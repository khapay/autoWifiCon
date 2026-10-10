# WiFi Manager — Android Expo

기존 Electron ZIP 소스의 UI/검색/상태 기능을 Android용으로 포팅한 **초기 구현**입니다.

## 실행

Node.js, Android SDK, JDK 17 및 Android 10+ 실기기 준비:

```bash
npm install
npx expo prebuild --platform android
npx expo run:android
```

APK 로컬 빌드(Windows):

```powershell
cd android
.\gradlew.bat assembleDebug
```

결과 경로: `android/app/build/outputs/apk/debug/app-debug.apk`

EAS: `npx eas-cli build --platform android --profile preview` (계정/서명 설정 필요)

## 제한사항

- Expo Go 불가: Expo native module 사용. Development Build 필요.
- Android 10+ Wi-Fi Network Specifier는 시스템 승인 UI가 필요하며 앱 범위 연결 요청입니다. 일반 저장 네트워크 자동 전환이 아닙니다.
- 현재 버전은 연결 요청 접수만 표시하며 **연결 성공 판정/실패 타임아웃 이벤트까지 완성된 상태가 아닙니다**. 실제 기기에서 테스트하고 콜백 기반으로 개선해야 합니다.
- Android 위치 권한과 위치 서비스가 필요합니다. Wi-Fi 검색은 OS가 제한하며 캐시된 목록이 반환될 수 있습니다.
- WPA2/WPA3 Personal/Open 요청만 지원합니다. Enterprise/OWE/WEP는 미지원입니다.
- 원본의 무작위 비밀번호 생성 기능은 제외했습니다. 본인이 관리하는 네트워크의 비밀번호만 사용하세요.
- 아직 Android SDK 빌드 및 실기기 검증은 수행하지 않았습니다.
