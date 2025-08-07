#!/bin/bash

set -e  # Exit on any error

echo "Building Bid My Hobby for Elastic Beanstalk..."

# Clean previous builds
echo "Cleaning previous builds..."
mvn clean

# Build frontend separately to avoid Maven plugin issues
echo "Building frontend..."
cd src/frontend

# Use npm instead of yarn to avoid version conflicts
if command -v npm &> /dev/null; then
    echo "Installing frontend dependencies with npm..."
    npm install
    echo "Building frontend..."
    npm run build
else
    echo "npm not found. Please install Node.js and npm."
    exit 1
fi

cd ../..

# Copy frontend build to Maven resources
echo "Copying frontend build files..."
mkdir -p src/main/resources/static
mkdir -p src/main/resources/public
cp -r src/frontend/dist/* src/main/resources/static/
cp src/frontend/dist/index.html src/main/resources/public/

# Package with Maven (skip frontend plugin)
echo "Packaging application with Maven..."
mvn package -Dfrontend.skip=true

# Check if the build was successful
if [ $? -eq 0 ]; then
  echo "Build successful!"
  echo "JAR file is located at: target/bid-my-hobby-0.0.1-SNAPSHOT.jar"
  echo "You can now deploy this JAR file to AWS Elastic Beanstalk."
else
  echo "Build failed. Please check the logs for errors."
  exit 1
fi