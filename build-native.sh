#!/bin/bash
# CLIPRO Native Build Script
# Requires GraalVM (https://www.graalvm.org/)

set -e

echo "Building CLIPRO native binary..."

# Check for GraalVM
if ! command -v native-image &> /dev/null; then
    echo "GraalVM not found. Installing..."
    # Install GraalVM if not present
    export JAVA_HOME=/usr/lib/jvm/graalvm
    export PATH=$JAVA_HOME/bin:$PATH
fi

# Set Java home to GraalVM
export JAVA_HOME=$(dirname $(dirname $(readlink -f $(which java))))
export PATH=$JAVA_HOME/bin:$PATH

echo "Using Java: $JAVA_HOME"
echo "Building JAR first..."
./gradlew uberJar

echo "Building native image..."
native-image \
    --jar build/libs/clipro-0.1.0.jar \
    --no-fallback \
    -H:Name=clipro \
    -H:+ReportExceptionStackTraces \
    -H:+DashboardAll \
    --initialize-at-run-time=com.fasterxml.jackson.databind.ObjectMapper \
    --initialize-at-run-time=org.eclipse.jgit.internal.JGitText \
    -R:+ReportUselessTypeHierarchies

echo ""
echo "Native build complete!"
echo "Run with: ./clipro"
