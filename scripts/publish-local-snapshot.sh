#!/usr/bin/env bash
set -euo pipefail

# Publish the current project version to a local Maven repository so it can be
# consumed for smoke-testing before pushing to Sonatype.
# Usage: scripts/publish-local-snapshot.sh [repository-dir]
# The default repository location is ./build/local-m2-repo relative to the project root.

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "${SCRIPT_DIR}/.." && pwd)"
REPO_DIR="${1:-${PROJECT_ROOT}/build/local-m2-repo}"

mkdir -p "${REPO_DIR}"

VERSION="$(mvn -B -ntp help:evaluate -Dexpression=project.version -q -DforceStdout)"
echo "Detected project version: ${VERSION}"

if [[ "${VERSION}" != *-SNAPSHOT ]]; then
  echo "Project version ${VERSION} is not a SNAPSHOT build. Aborting local snapshot publish." >&2
  exit 1
fi

ALT_REPO="local::default::file://${REPO_DIR}"

echo "Publishing to ${REPO_DIR}"
mvn -B -ntp clean deploy -DskipTests -DaltDeploymentRepository="${ALT_REPO}"

echo "Artifacts deployed to ${REPO_DIR}. Add this repository to your consumer project's settings.xml or pom.xml to test."
