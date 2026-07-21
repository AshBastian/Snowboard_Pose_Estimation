#!/usr/bin/env bash
# Runs the shared module's test suite and prints a per-class pass/fail summary.
#
# Usage:
#   scripts/test.sh              # run shared jvm + android host tests
#   scripts/test.sh --full       # also run the full project build (./gradlew build)
#   scripts/test.sh --tests "com.snowboardpose.shared.pose.JointTest"   # forwarded to Gradle
set -uo pipefail

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$REPO_ROOT"

# Locate a JDK if JAVA_HOME isn't already set to a valid one.
if [ -z "${JAVA_HOME:-}" ] || [ ! -x "${JAVA_HOME}/bin/java" ]; then
  for candidate in /snap/android-studio/current/jbr "$HOME/Android/Sdk/jbr" /opt/android-studio/jbr; do
    if [ -x "$candidate/bin/java" ]; then
      export JAVA_HOME="$candidate"
      break
    fi
  done
fi

if [ -z "${JAVA_HOME:-}" ] || [ ! -x "${JAVA_HOME}/bin/java" ]; then
  echo "error: could not find a JDK. Set JAVA_HOME manually and re-run." >&2
  exit 1
fi

RUN_FULL_BUILD=false
GRADLE_ARGS=()
for arg in "$@"; do
  if [ "$arg" = "--full" ]; then
    RUN_FULL_BUILD=true
  else
    GRADLE_ARGS+=("$arg")
  fi
done

echo "Using JAVA_HOME=$JAVA_HOME"
echo

./gradlew :shared:jvmTest :shared:testAndroidHostTest --console=plain "${GRADLE_ARGS[@]}"
GRADLE_STATUS=$?

echo
echo "== Shared module test summary =="
OVERALL_STATUS=$GRADLE_STATUS
for dir in shared/build/test-results/jvmTest shared/build/test-results/testAndroidHostTest; do
  [ -d "$dir" ] || continue
  echo "-- ${dir#shared/build/test-results/} --"
  for f in "$dir"/TEST-*.xml; do
    [ -f "$f" ] || continue
    header="$(grep -o '<testsuite [^>]*' "$f" | head -1)"
    name=$(echo "$header" | grep -o ' name="[^"]*"' | head -1 | cut -d'"' -f2)
    tests=$(echo "$header" | grep -o 'tests="[0-9]*"' | cut -d'"' -f2)
    failures=$(echo "$header" | grep -o 'failures="[0-9]*"' | cut -d'"' -f2)
    errors=$(echo "$header" | grep -o 'errors="[0-9]*"' | cut -d'"' -f2)
    skipped=$(echo "$header" | grep -o 'skipped="[0-9]*"' | cut -d'"' -f2)
    status="PASS"
    if [ "${failures:-0}" -gt 0 ] || [ "${errors:-0}" -gt 0 ]; then
      status="FAIL"
      OVERALL_STATUS=1
    fi
    printf "  [%s] %-55s tests=%-3s failures=%-3s errors=%-3s skipped=%s\n" \
      "$status" "$name" "${tests:-0}" "${failures:-0}" "${errors:-0}" "${skipped:-0}"
  done
done
echo

if [ "$RUN_FULL_BUILD" = true ]; then
  echo "== Full project build (./gradlew build) =="
  ./gradlew build --console=plain
  BUILD_STATUS=$?
  [ "$BUILD_STATUS" -ne 0 ] && OVERALL_STATUS=1
  echo
fi

if [ "$OVERALL_STATUS" -eq 0 ]; then
  echo "All tests passed."
else
  echo "One or more tests failed or the build failed — see output above."
fi

exit "$OVERALL_STATUS"
