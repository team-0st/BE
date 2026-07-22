# Terraform 실행 메모

## 현재 구조

- 인프라는 단일 환경으로 관리합니다.
- EC2는 1대, RDS는 1세트로 운영합니다.
- dev/prod 분리는 인프라가 아니라 애플리케이션 레벨에서 관리합니다.
- 예시:
  - `zerost_prod` 애플리케이션 -> `8080`
  - `zerost_dev` 애플리케이션 -> `8081`
  - RDS 내부 데이터베이스 -> `zerost_prod`, `zerost_dev`
- 외부에서 접근하는 포트는 Nginx 기준 `80`, `443`이며, Cloudflare를 통한 HTTPS 연결을 위해 EC2 보안그룹에 `443` 인바운드가 반드시 열려 있어야 합니다.
- 공용 정적 이미지는 같은 S3 버킷을 사용하되 `public/` prefix 아래에 저장하고, CloudFront가 해당 prefix만 origin path로 바라보는 구조를 사용합니다.
- Terraform으로 RDS를 만들 때는 초기 데이터베이스를 하나만 생성합니다.
- 현재 Terraform 기본값은 `zerost`를 생성하도록 두고, 이후 필요하면 직접 접속해서 `zerost_dev`, `zerost_prod` 등으로 나누는 흐름을 기준으로 합니다.

## tfvars 파일

커밋되는 파일:

- `terraform.tfvars.example`

로컬에서만 사용하는 파일:

- `terraform.tfvars`

추가로 설정하는 값:

- `ecr_repository_name`: 배포 이미지를 저장할 ECR 리포지토리 이름
- `github_repository`: OIDC AssumeRole을 허용할 GitHub 레포지토리
- `github_oidc_branches`: 배포를 허용할 브랜치 목록
- `public_assets_domain`: 공용 이미지 도메인
- `public_assets_origin_prefix`: 공용 이미지 S3 prefix
- `public_assets_enable_custom_domain`: ACM 인증서를 요청/관리할지 여부
- `public_assets_attach_custom_domain`: CloudFront에 커스텀 도메인을 실제로 붙일지 여부
- `public_assets_acm_certificate_arn`: `us-east-1`에서 발급 완료된 ACM 인증서 ARN

적용 후 Terraform output의 `github_actions_role_arn` 값을 GitHub Secret `AWS_ROLE_ARN`에 등록합니다.

## 공용 이미지 CloudFront 적용 순서

공용 이미지 배포는 `S3(private) + CloudFront + Cloudflare DNS` 기준으로 구성합니다.

### 1. 1차 적용

로컬 `terraform.tfvars`에 아래 값을 우선 넣습니다.

```hcl
public_assets_domain               = "assets.zero-st.com"
public_assets_origin_prefix        = "public"
public_assets_enable_custom_domain = false
public_assets_attach_custom_domain = false
public_assets_acm_certificate_arn  = ""
```

그리고 기본 계획/적용을 실행합니다.

```bash
terraform fmt
terraform plan -var-file="terraform.tfvars"
terraform apply -var-file="terraform.tfvars"
```

이 단계가 끝나면 아래 output을 확인할 수 있습니다.

- `public_assets_cloudfront_domain_name`

### 2. ACM 인증서 요청

CloudFront 커스텀 도메인 인증서는 반드시 `us-east-1` 리전에 있어야 합니다.

`terraform.tfvars`를 아래처럼 바꾼 뒤 다시 적용합니다.

```hcl
public_assets_enable_custom_domain = true
public_assets_attach_custom_domain = false
public_assets_acm_certificate_arn  = ""
```

```bash
terraform plan -var-file="terraform.tfvars"
terraform apply -var-file="terraform.tfvars"
```

이 단계에서는 ACM 인증서 요청만 생성하고, CloudFront는 계속 기본 `cloudfront.net` 도메인을 유지합니다.

적용 후 output에서 아래 값을 확인합니다.

- `public_assets_acm_certificate_arn`
- `public_assets_acm_validation_records`

### 3. Cloudflare DNS 검증 레코드 추가

Cloudflare에서:

- `zero-st.com` 선택
- `DNS`
- `Add record`

그리고 `public_assets_acm_validation_records` output에 나온 CNAME 레코드를 그대로 추가합니다.

주의:

- ACM 검증용 CNAME은 `DNS only`로 두는 편이 안전합니다.
- Cloudflare가 자동으로 주는 프록시(주황 구름)는 끄고 회색 구름으로 둡니다.

### 4. ACM 발급 완료 확인

AWS 콘솔에서:

- `N. Virginia (us-east-1)` 리전 선택
- `Certificate Manager`
- `assets.zero-st.com` 인증서 상태가 `Issued`인지 확인

### 5. 2차 적용

발급 완료 후에는 Terraform이 관리 중인 인증서를 그대로 CloudFront에 연결할 수 있도록 아래처럼 바꿉니다.

```hcl
public_assets_enable_custom_domain = true
public_assets_attach_custom_domain = true
public_assets_acm_certificate_arn  = ""
```

이미 외부에서 발급한 인증서를 붙이고 싶다면 `public_assets_acm_certificate_arn`에 그 ARN을 넣으면 됩니다.

그리고 다시 적용합니다.

```bash
terraform plan -var-file="terraform.tfvars"
terraform apply -var-file="terraform.tfvars"
```

이 단계가 끝나면 output의 `public_assets_base_url`이 `https://assets.zero-st.com`으로 바뀝니다.

### 6. Cloudflare 서비스 도메인 연결

Cloudflare에서:

- `zero-st.com` 선택
- `DNS`
- `Add record`

아래 레코드를 추가합니다.

- Type: `CNAME`
- Name: `assets`
- Target: Terraform output의 `public_assets_cloudfront_domain_name`

처음에는 `DNS only`로 확인하고, 정상 동작 확인 후 필요하면 `Proxied` 전환을 검토합니다.

### 7. 파일 업로드 위치

공용 이미지는 아래 경로 규칙을 사용합니다.

- `s3://<upload_bucket>/public/shops/...`
- `s3://<upload_bucket>/public/profile-characters/...`

CloudFront URL은 `public/` prefix를 제외한 형태로 접근합니다.

예:

- S3 객체 키: `public/shops/almang-shop.png`
- 접근 URL: `https://assets.zero-st.com/shops/almang-shop.png`

GitHub Actions에 추가로 설정하는 값:

- Variables
  - `AWS_REGION`
  - `ECR_REPOSITORY`
- Secrets
  - `AWS_ROLE_ARN`
  - `EC2_HOST`
  - `EC2_USER`
  - `EC2_SSH_KEY`
  - `EC2_KNOWN_HOSTS`
  - `ENV_FILE_DEV`
  - `ENV_FILE_PROD`

## 기본 실행 명령어

```bash
terraform fmt
terraform init
terraform plan -var-file="terraform.tfvars"
terraform apply -var-file="terraform.tfvars"
```
