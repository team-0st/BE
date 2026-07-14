# Terraform 실행 메모

## 현재 구조

- 인프라는 단일 환경으로 관리합니다.
- EC2는 1대, RDS는 1세트로 운영합니다.
- dev/prod 분리는 인프라가 아니라 애플리케이션 레벨에서 관리합니다.
- 예시:
  - `zerost_prod` 애플리케이션 -> `8080`
  - `zerost_dev` 애플리케이션 -> `8081`
  - RDS 내부 데이터베이스 -> `zerost_prod`, `zerost_dev`
- Terraform으로 RDS를 만들 때는 초기 데이터베이스를 하나만 생성합니다.
- 현재 Terraform 기본값은 `zerost`를 생성하도록 두고, 이후 필요하면 직접 접속해서 `zerost_dev`, `zerost_prod` 등으로 나누는 흐름을 기준으로 합니다.

## tfvars 파일

커밋되는 파일:

- `terraform.tfvars.example`

로컬에서만 사용하는 파일:

- `terraform.tfvars`

추가로 설정하는 값:

- `github_repository`: OIDC AssumeRole을 허용할 GitHub 레포지토리
- `github_oidc_branches`: 배포를 허용할 브랜치 목록

적용 후 Terraform output의 `github_actions_role_arn` 값을 GitHub Secret `AWS_ROLE_ARN`에 등록합니다.

## 기본 실행 명령어

```bash
terraform fmt
terraform init
terraform plan -var-file="terraform.tfvars"
terraform apply -var-file="terraform.tfvars"
```
