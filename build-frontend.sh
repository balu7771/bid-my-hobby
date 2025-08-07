#!/bin/bash

# Navigate to frontend directory
cd src/frontend

# Install dependencies if needed
echo "Installing dependencies..."
yarn install

# Build the frontend
echo "Building frontend..."
yarn build

# Create static directories if they don't exist
mkdir -p ../main/resources/static
mkdir -p ../main/resources/public

# Copy build files to Spring Boot static resources
echo "Copying build files to Spring Boot resources..."
cp -r dist/* ../main/resources/static/
cp -r dist/index.html ../main/resources/public/

echo "Frontend build and copy complete!"