#!/usr/bin/env bash
set -euo pipefail

TARGET_ENV="${1:?target environment is required}"
ECR_REGISTRY="${2:?ecr registry is required}"
ECR_REPOSITORY="${3:?ecr repository is required}"
IMAGE_TAG="${4:?image tag is required}"

APP_DIR="/opt/zerost"
COMPOSE_ENV_FILE="${APP_DIR}/.env.compose"

mkdir -p "${APP_DIR}" "${APP_DIR}/nginx"

if [[ ! -f "${COMPOSE_ENV_FILE}" ]]; then
  cat <<EOF > "${COMPOSE_ENV_FILE}"
ECR_REGISTRY=${ECR_REGISTRY}
ECR_REPOSITORY=${ECR_REPOSITORY}
PROD_IMAGE_TAG=prod-latest
DEV_IMAGE_TAG=dev-latest
EOF
fi

source "${COMPOSE_ENV_FILE}"

if [[ "${TARGET_ENV}" == "prod" ]]; then
  PROD_IMAGE_TAG="${IMAGE_TAG}"
elif [[ "${TARGET_ENV}" == "dev" ]]; then
  DEV_IMAGE_TAG="${IMAGE_TAG}"
else
  echo "Unsupported target environment: ${TARGET_ENV}" >&2
  exit 1
fi

cat <<EOF > "${COMPOSE_ENV_FILE}"
ECR_REGISTRY=${ECR_REGISTRY}
ECR_REPOSITORY=${ECR_REPOSITORY}
PROD_IMAGE_TAG=${PROD_IMAGE_TAG}
DEV_IMAGE_TAG=${DEV_IMAGE_TAG}
EOF

cp "${APP_DIR}/nginx/prod.conf" /etc/nginx/conf.d/zerost-prod.conf
cp "${APP_DIR}/nginx/dev.conf" /etc/nginx/conf.d/zerost-dev.conf
rm -f /etc/nginx/conf.d/default.conf

nginx -t
systemctl reload nginx

docker compose --env-file "${COMPOSE_ENV_FILE}" -f "${APP_DIR}/compose.yaml" pull "app-${TARGET_ENV}"
docker compose --env-file "${COMPOSE_ENV_FILE}" -f "${APP_DIR}/compose.yaml" up -d "app-${TARGET_ENV}"
