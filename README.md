# 별잡아라 (fakepit)

**편의를 위해 되도록이면 IDE 내부에서 파일을 열어주세요.**

별잡아라 (fakepit) 미니게임 코드 설명입니다.

---

## 게임 실행

게임 실행에 앞서 서버의 있는 플레이어 수는 무조건 2~12명 사이여야합니다. 그렇지 않으면 게임이 작동하질 않습니다.

- `/fakepit start` - 게임 시작
- `/fakepit stop` - 게임 종료

---

## 코드 설명

간략한 코드 설명입니다. 추후에 변경하는 사항이 있을시에 같이 업데이트 해주시면 감사하겠습니다.

### [Main](src/main/kotlin/world/komq/server/fakepit/plugin/FakepitPluginMain.kt)

- TODO

### [Object](src/main/kotlin/world/komq/server/fakepit/plugin/objects/FakepitGameContentManager.kt)

- TODO

### [Event](src/main/kotlin/world/komq/server/fakepit/plugin/events/FakepitEvent.kt) ([MapProtectEvent](src/main/kotlin/world/komq/server/fakepit/plugin/events/FakepitMapProtectEvent.kt) 제외)

- TODO

### Tasks ([ConfigReload](src/main/kotlin/world/komq/server/fakepit/plugin/tasks/FakepitConfigReloadTask.kt), [End](src/main/kotlin/world/komq/server/fakepit/plugin/tasks/FakepitEndTask.kt) 제)

- [FakepitGameTask](src/main/kotlin/world/komq/server/fakepit/plugin/tasks/FakepitGameTask.kt): 0.7초에 1점씩 점수를 올리고 소리를 재생합니다.
- [FakepitSecondsTickTask](src/main/kotlin/world/komq/server/fakepit/plugin/tasks/FakepitSecondsTickTask.kt): 매 초마다 돌아가며 1분후 킬이 없을시 네더의 별을 지급, 20분동안 우승 조건 미달성시 게임을 강제 종료합니다.
- [FakepitZeroTickTask](src/main/kotlin/world/komq/server/fakepit/plugin/tasks/FakepitZeroTickTask.kt): 매 틱마다 돌아가며 현재 네더의 별 소유자와 네더의 별 소유자가 접속 중단 시에 땅에 떨어지는 네더의 별 위치를 알려줍니다.