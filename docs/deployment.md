# 배포 운영 메모

## Cloudflare HTTPS 및 Nginx 도메인 라우팅

- 운영 API 도메인은 `api.zero-st.com`, 개발 API 도메인은 `dev-api.zero-st.com`을 사용합니다.
- Cloudflare DNS는 두 도메인 모두 동일한 EC2 공인 IP를 가리키고, Nginx가 `server_name` 기준으로 prod/dev upstream을 분기합니다.
- `Full (strict)`를 사용할 때는 `api.zero-st.com`, `dev-api.zero-st.com` DNS 레코드가 모두 Cloudflare `Proxied` 상태여야 합니다.
- prod는 `127.0.0.1:8080`, dev는 `127.0.0.1:8081`로 reverse proxy 됩니다.
- Origin 인증서는 서버에 수동으로 저장하며, 현재 경로는 다음과 같습니다.
  - `/etc/ssl/cloudflare/zero-st-origin.crt`
  - `/etc/ssl/cloudflare/zero-st-origin.key`
- 개인키 파일은 `root:root`, `0600` 권한으로 유지하고, 인증서 파일은 비밀 값이 아니므로 읽기 가능 권한으로 관리합니다.
- `deploy.sh` 실행 전에는 위 두 파일이 서버에 모두 존재해야 하며, 누락된 상태에서는 Nginx HTTPS 설정이 정상 반영되지 않습니다.
- Cloudflare SSL/TLS 모드는 origin 인증서 반영 후 `Full (strict)`를 사용합니다.
- 배포 workflow는 레포의 `nginx/prod.conf`, `nginx/dev.conf`를 EC2의 `${APP_DIR}/nginx`로 동기화하고, `deploy.sh`가 이를 `/etc/nginx/conf.d`에 반영합니다.
- `deploy.sh`는 설정 반영 후 `nginx -t`로 검증에 성공한 경우에만 `systemctl reload nginx`를 수행합니다.

## 배포 리소스 정리 기준

- ECR 이미지는 lifecycle policy로 원격 저장소 기준 정리를 수행합니다.
- EC2 내부 Docker 리소스는 배포 성공 후 현재 dev/prod에서 사용 중인 태그를 제외한 동일 리포지토리의 이전 SHA 이미지들을 우선 정리합니다.
- 추가로 dangling image는 `docker image prune -f`로 정리하되, 정리 실패가 배포 실패로 이어지지 않도록 best-effort로 처리합니다.
- 초기 단계에서는 실행 중인 컨테이너나 사용 중인 이미지에 영향을 줄 수 있는 과한 정리(`docker image prune -af`, host 전체 `docker container prune` 등)는 적용하지 않습니다.
- 이후 디스크 사용량 추이를 보면서 builder cache, unused image 전체 정리 정책은 별도 이슈에서 확장합니다.
