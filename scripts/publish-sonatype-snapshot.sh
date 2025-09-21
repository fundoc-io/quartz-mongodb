#!/usr/bin/env bash
set -euo pipefail

# Publish the current SNAPSHOT version to Sonatype OSSRH snapshot repository.
# Requires the following environment variables:
#   OSSRH_USERNAME, OSSRH_PASSWORD
# Optionally you can provide GPG key beforehand if signing is desired.

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "${SCRIPT_DIR}/.." && pwd)"
cd "${PROJECT_ROOT}"

if [[ -z "${OSSRH_USERNAME:-}" || -z "${OSSRH_PASSWORD:-}" ]]; then
  echo "OSSRH_USERNAME and OSSRH_PASSWORD must be set in the environment." >&2
  exit 1
fi

VERSION="$(mvn -B -ntp help:evaluate -Dexpression=project.version -q -DforceStdout)"
echo "Detected project version: ${VERSION}"

if [[ "${VERSION}" != *-SNAPSHOT ]]; then
  echo "Project version ${VERSION} is not a SNAPSHOT build. Aborting Sonatype snapshot publish." >&2
  exit 1
fi

TMP_SETTINGS="$(mktemp)"
trap 'rm -f "${TMP_SETTINGS}"' EXIT

cat >"${TMP_SETTINGS}" <<EOF
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0 http://maven.apache.org/xsd/settings-1.0.0.xsd">
  <servers>
    <server>
      <id>ossrh</id>
      <username>${OSSRH_USERNAME}</username>
      <password>${OSSRH_PASSWORD}</password>
    </server>
  </servers>
</settings>
EOF

mvn -B -ntp clean deploy -DskipTests --settings "${TMP_SETTINGS}"
