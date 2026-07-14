# 배포 운영 메모

## 배포 리소스 정리 기준

- ECR 이미지는 lifecycle policy로 원격 저장소 기준 정리를 수행합니다.
- EC2 내부 Docker 리소스는 배포 성공 후 현재 dev/prod에서 사용 중인 태그를 제외한 동일 리포지토리의 이전 SHA 이미지들을 우선 정리합니다.
- 추가로 dangling image는 `docker image prune -f`로 정리하되, 정리 실패가 배포 실패로 이어지지 않도록 best-effort로 처리합니다.
- 초기 단계에서는 실행 중인 컨테이너나 사용 중인 이미지에 영향을 줄 수 있는 과한 정리(`docker image prune -af`, host 전체 `docker container prune` 등)는 적용하지 않습니다.
- 이후 디스크 사용량 추이를 보면서 builder cache, unused image 전체 정리 정책은 별도 이슈에서 확장합니다.
