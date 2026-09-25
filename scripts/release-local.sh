#!/usr/bin/env bash

set -euo pipefail

mode="${1:-}"
version_name="${2:-}"

if [[ "$mode" != "verify" && "$mode" != "stage" ]]; then
  echo "Usage: $0 <verify|stage> vMAJOR.MINOR.PATCH" >&2
  exit 2
fi

if [[ ! "$version_name" =~ ^v[0-9]+\.[0-9]+\.[0-9]+$ ]]; then
  echo "Version must use vMAJOR.MINOR.PATCH form" >&2
  exit 2
fi

repository_root="$(git rev-parse --show-toplevel)"
cd "$repository_root"

if [[ -n "$(git status --porcelain)" ]]; then
  echo "Release verification requires a clean worktree" >&2
  exit 1
fi

export VERSION_NAME="$version_name"
export JAVA_HOME="${JAVA_HOME:-/Applications/Android Studio.app/Contents/jbr/Contents/Home}"
export ANDROID_HOME="${ANDROID_HOME:-$HOME/Library/Android/sdk}"

if [[ "$mode" == "verify" ]]; then
  ./gradlew --no-daemon clean test lint assembleRelease publishReleasePublicationToMavenLocal
  exit 0
fi

if [[ "$(git describe --exact-match --tags HEAD 2>/dev/null || true)" != "$version_name" ]]; then
  echo "Central staging requires HEAD to match signed tag $version_name" >&2
  exit 1
fi

required_environment=(
  OSSRH_USERNAME
  OSSRH_PASSWORD
  SIGNING_KEY
  SIGNING_KEY_ID
  SIGNING_PASSWORD
)

for variable_name in "${required_environment[@]}"; do
  if [[ -z "${!variable_name:-}" ]]; then
    echo "Missing required environment variable: $variable_name" >&2
    exit 1
  fi
done

./gradlew --no-daemon publishToSonatype --max-workers 1 closeSonatypeStagingRepository
