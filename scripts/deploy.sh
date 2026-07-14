#!/usr/bin/env bash
set -euo pipefail

TARGET_ENV="${1:?target environment is required}"
ECR_REGISTRY="${2:?ecr registry is required}"
ECR_REPOSITORY="${3:?ecr repository is required}"
IMAGE_TAG="${4:?image tag is required}"

APP_DIR="/opt/zerost"
COMPOSE_ENV_FILE="${APP_DIR}/.env.compose"

cleanup_local_images() {
  local current_prod_ref="${ECR_REGISTRY}/${ECR_REPOSITORY}:${PROD_IMAGE_TAG}"
  local current_dev_ref="${ECR_REGISTRY}/${ECR_REPOSITORY}:${DEV_IMAGE_TAG}"

  mapfile -t repo_image_refs < <(docker images "${ECR_REGISTRY}/${ECR_REPOSITORY}" --format '{{.Repository}}:{{.Tag}}' | sort -u)

  for image_ref in "${repo_image_refs[@]}"; do
    if [[ "${image_ref}" != "${current_prod_ref}" && "${image_ref}" != "${current_dev_ref}" ]]; then
      docker image rm "${image_ref}" >/dev/null 2>&1 || echo "Warning: failed to remove old image ${image_ref}" >&2
    fi
  done

  docker image prune -f >/dev/null 2>&1 || echo "Warning: failed to prune dangling images" >&2
}

mkdir -p "${APP_DIR}" "${APP_DIR}/nginx"

if [[ ! -f "${COMPOSE_ENV_FILE}" ]]; then
  cat <<EOF > "${COMPOSE_ENV_FILE}"
ECR_REGISTRY=${ECR_REGISTRY}
ECR_REPOSITORY=${ECR_REPOSITORY}
PROD_IMAGE_TAG=prod-initial
DEV_IMAGE_TAG=dev-initial
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

if [[ "${TARGET_ENV}" == "prod" ]]; then
  sudo cp "${APP_DIR}/nginx/prod.conf" /etc/nginx/conf.d/zerost-prod.conf
elif [[ "${TARGET_ENV}" == "dev" ]]; then
  sudo cp "${APP_DIR}/nginx/dev.conf" /etc/nginx/conf.d/zerost-dev.conf
fi

sudo rm -f /etc/nginx/conf.d/default.conf

sudo nginx -t
sudo systemctl reload nginx

docker compose --env-file "${COMPOSE_ENV_FILE}" -f "${APP_DIR}/compose.yaml" pull "app-${TARGET_ENV}"
docker compose --env-file "${COMPOSE_ENV_FILE}" -f "${APP_DIR}/compose.yaml" up -d --wait --wait-timeout 180 "app-${TARGET_ENV}"

# Clean up old local deployment images without changing the deployment result.
cleanup_local_images
