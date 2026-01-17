#!/bin/bash

echo "=== DEBUG TEST: requiresInstance parameter ==="
echo "This test will help us trace where the requiresInstance value gets lost"
echo ""

# Create a test challenge with requiresInstance=true
echo "Creating test challenge with requiresInstance=true..."

TEMP_FILE=$(mktemp)
echo "test content" > "$TEMP_FILE"

echo "Sending request with requiresInstance=true..."
curl -X POST "http://localhost:8081/api/challenges" \
  -F "title=DEBUG TEST TRUE" \
  -F "description=Debug test with requiresInstance true" \
  -F "category=debug" \
  -F "difficulty=easy" \
  -F "points=100" \
  -F "flag=DEBUG{true_test}" \
  -F "requiresInstance=true" \
  -F "downloadFile=@$TEMP_FILE" \
  -v 2>&1 | grep -E "(requiresInstance|DEBUG)" || echo "No debug output found"

rm "$TEMP_FILE"

sleep 3

echo ""
echo "Creating test challenge with requiresInstance=false..."

TEMP_FILE=$(mktemp)
echo "test content" > "$TEMP_FILE"

echo "Sending request with requiresInstance=false..."
curl -X POST "http://localhost:8081/api/challenges" \
  -F "title=DEBUG TEST FALSE" \
  -F "description=Debug test with requiresInstance false" \
  -F "category=debug" \
  -F "difficulty=easy" \
  -F "points=100" \
  -F "flag=DEBUG{false_test}" \
  -F "requiresInstance=false" \
  -F "downloadFile=@$TEMP_FILE" \
  -v 2>&1 | grep -E "(requiresInstance|DEBUG)" || echo "No debug output found"

rm "$TEMP_FILE"

echo ""
echo "=== DEBUG TEST COMPLETED ==="
echo "Check the backend console logs for detailed DEBUG output"