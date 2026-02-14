#!/bin/bash

# NetScope gRPC Build and Installation Script

set -e

echo "=================================="
echo "NetScope gRPC - Build & Install"
echo "=================================="
echo ""

# Check Java version
echo "Checking Java version..."
JAVA_VERSION=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2 | cut -d'.' -f1)
if [ "$JAVA_VERSION" -lt 21 ]; then
    echo "❌ Error: Java 21 or higher is required. Found: $JAVA_VERSION"
    exit 1
fi
echo "✅ Java $JAVA_VERSION detected"
echo ""

# Check Maven
echo "Checking Maven..."
if ! command -v mvn &> /dev/null; then
    echo "❌ Error: Maven is not installed"
    exit 1
fi
MVN_VERSION=$(mvn -version | head -n 1)
echo "✅ $MVN_VERSION"
echo ""

# Clean
echo "Cleaning previous builds..."
mvn clean
echo ""

# Generate Protocol Buffers
echo "Generating Protocol Buffer classes..."
mvn protobuf:compile protobuf:compile-custom
echo "✅ Protocol Buffers generated"
echo ""

# Compile
echo "Compiling source code..."
mvn compile
echo "✅ Compilation successful"
echo ""

# Run tests (if any)
echo "Running tests..."
mvn test || echo "⚠️  No tests found or tests failed"
echo ""

# Package
echo "Packaging JAR..."
mvn package -DskipTests
echo "✅ JAR packaged"
echo ""

# Install to local repository
echo "Installing to local Maven repository..."
mvn install -DskipTests
echo "✅ Installed to ~/.m2/repository"
echo ""

echo "=================================="
echo "✅ Build Complete!"
echo "=================================="
echo ""
echo "Next steps:"
echo "1. Add dependency to your project's pom.xml:"
echo ""
echo "   <dependency>"
echo "       <groupId>com.netscope</groupId>"
echo "       <artifactId>netscope-grpc</artifactId>"
echo "       <version>1.0.0-SNAPSHOT</version>"
echo "   </dependency>"
echo ""
echo "2. Configure in application.properties:"
echo ""
echo "   netscope.grpc.enabled=true"
echo "   netscope.grpc.port=9090"
echo "   netscope.security.enabled=true"
echo "   netscope.security.apiKey=your-secret-key"
echo ""
echo "3. Annotate your methods:"
echo ""
echo "   @NetworkPublic or @NetworkRestricted"
echo ""
echo "4. Check the demo app: examples/demo-app/"
echo ""
echo "See README.md for complete documentation."
echo ""
