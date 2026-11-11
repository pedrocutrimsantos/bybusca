#!/usr/bin/env bash
# Instalação ByBusca em VPS Ubuntu (Hostinger).
# Uso (como root): bash scripts/hostinger-vps.sh
set -euo pipefail

if [[ "${EUID}" -ne 0 ]]; then
  echo "Rode como root: sudo bash scripts/hostinger-vps.sh"
  exit 1
fi

export DEBIAN_FRONTEND=noninteractive
apt-get update -y
apt-get install -y ca-certificates curl git ufw

if ! command -v docker >/dev/null 2>&1; then
  curl -fsSL https://get.docker.com | sh
fi

systemctl enable --now docker
ufw allow OpenSSH
ufw allow 80/tcp
ufw allow 443/tcp
ufw --force enable || true

echo "Docker pronto. Clone o repo, crie o .env e suba:"
echo "  git clone https://github.com/pedrocutrimsantos/bybusca.git /opt/bybusca"
echo "  cd /opt/bybusca"
echo "  cp .env.example .env && nano .env"
echo "  docker compose -f docker-compose.prod.yml up -d --build"
