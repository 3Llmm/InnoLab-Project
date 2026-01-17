#!/bin/bash

# Test script to verify the requiresInstance parameter fix
# This simulates what the frontend does when creating a challenge

echo "Testing requiresInstance parameter handling..."

# Test case 1: requiresInstance = true
echo "\n=== Test Case 1: requiresInstance = true ==="

# Create a temporary file for testing
TEMP_FILE=$(mktemp)
echo "test content" > "$TEMP_FILE"

# Simulate the frontend request with requiresInstance=true
curl -X POST "http://localhost:8081/api/challenges" \
  -F "title=Test Challenge True" \
  -F "description=Test description" \
  -F "category=test" \
  -F "difficulty=easy" \
  -F "points=100" \
  -F "flag=TEST{flag}" \
  -F "requiresInstance=true" \
  -F "downloadFile=@$TEMP_FILE" \
  -v

# Clean up
rm "$TEMP_FILE"

# Wait a bit before next test
sleep 2

# Test case 2: requiresInstance = false
echo "\n=== Test Case 2: requiresInstance = false ==="

# Create another temporary file
TEMP_FILE=$(mktemp)
echo "test content" > "$TEMP_FILE"

# Simulate the frontend request with requiresInstance=false
curl -X POST "http://localhost:8081/api/challenges" \
  -F "title=Test Challenge False" \
  -F "description=Test description" \
  -F "category=test" \
  -F "difficulty=easy" \
  -F "points=100" \
  -F "flag=TEST{flag}" \
  -F "requiresInstance=false" \
  -F "downloadFile=@$TEMP_FILE" \
  -v

# Clean up
rm "$TEMP_FILE"

echo "\n=== Test completed ==="