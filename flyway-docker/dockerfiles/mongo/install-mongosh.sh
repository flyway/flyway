#!/usr/bin/env bash
set -euo pipefail

MONGOSH_VERSION="${MONGOSH_VERSION:-2.5.0}"

log() { echo "[mongosh-install] $*"; }

is_available() { command -v "$1" >/dev/null 2>&1; }

install_via_apt() {
  log "Detected apt-get; installing mongosh ${MONGOSH_VERSION}" || true
  apt-get update || return 0
  apt-get install -y --no-install-recommends ca-certificates wget gnupg || true
  if [ ! -f /etc/apt/trusted.gpg.d/server-8.0.asc ]; then
    wget -qO- https://www.mongodb.org/static/pgp/server-8.0.asc | tee /etc/apt/trusted.gpg.d/server-8.0.asc >/dev/null
  fi
  echo "deb [ arch=amd64,arm64 ] https://repo.mongodb.org/apt/ubuntu jammy/mongodb-org/8.0 multiverse" > /etc/apt/sources.list.d/mongodb-org-8.0.list
  apt-get update || true
  # Try pinned version first, fall back to latest if pin not found.
  (apt-get install -y --no-install-recommends mongodb-mongosh="${MONGOSH_VERSION}" || \
   apt-get install -y --no-install-recommends mongodb-mongosh) || true
  rm -rf /var/lib/apt/lists/* || true
}

install_via_npm() {
  log "Attempting npm-based mongosh ${MONGOSH_VERSION} install" || true
  install_npm_if_needed || return 0
  npm update -g npm || true
  npm install -g "mongosh@${MONGOSH_VERSION}" || true
}

install_npm_if_needed() {
  # Install npm if missing and possible; return success even if not installed to keep best-effort contract.
  if is_available npm; then
    return 0
  fi
  if is_available apk; then
    apk add --no-cache --update npm || true
    return 0
  fi
  log "npm not available and no supported package manager for npm install"
  return 0
}

verify_mongosh() {
  if is_available mongosh; then
    log "Installed mongosh version: $(mongosh --version 2>/dev/null || echo 'unknown')"
  else
    log "mongosh not installed (proceeding without it)."
  fi
}

main() {
  if is_available apt-get; then
    install_via_apt
  elif is_available apk || is_available npm; then
    install_via_npm
  else
    log "No supported package manager detected; skipping mongosh installation"
  fi
  verify_mongosh
}

main "$@"