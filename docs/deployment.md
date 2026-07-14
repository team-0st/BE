# 배포 운영 메모

## 배포 리소스 정리 기준

- ECR 이미지는 lifecycle policy로 원격 저장소 기준 정리를 수행합니다.
- EC2 내부 Docker 리소스는 배포 성공 후 `docker image prune -f`, `docker container prune -f`를 실행해 안전한 범위에서만 정리합니다.
- 초기 단계에서는 실행 중인 컨테이너나 사용 중인 이미지에 영향을 줄 수 있는 과한 정리(`docker image prune -af` 등)는 적용하지 않습니다.
- 이후 디스크 사용량 추이를 보면서 builder cache, unused image 전체 정리 정책은 별도 이슈에서 확장합니다.
