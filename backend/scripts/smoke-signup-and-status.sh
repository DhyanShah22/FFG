#!/usr/bin/env bash
set -euo pipefail

BASE="${BASE_URL:-http://localhost:8080}"
PG="${PG_BIN:-/Applications/Postgres.app/Contents/Versions/latest/bin/psql}"
LOG_FILE="${APP_LOG:-/Users/avinashjha11/.cursor/projects/Users-avinashjha11-Desktop-antarang-backend/terminals/189163.txt}"

echo "=== Seed GENDER config if missing ==="
"$PG" -h 127.0.0.1 -U avinashjha11 -d antarang_cap -q -c "
INSERT INTO configurations (configuration_group_id, code, value, display_order)
SELECT cg.id, 'MALE', 'Male', 1 FROM configuration_groups cg WHERE cg.code = 'GENDER'
AND NOT EXISTS (SELECT 1 FROM configurations c JOIN configuration_groups g ON g.id = c.configuration_group_id WHERE g.code = 'GENDER');
"
GENDER_ID=$("$PG" -h 127.0.0.1 -U avinashjha11 -d antarang_cap -tA -c "
  SELECT c.id FROM configurations c JOIN configuration_groups g ON g.id = c.configuration_group_id WHERE g.code = 'GENDER' LIMIT 1;
")
echo "genderConfigId=$GENDER_ID"

NEW_EMAIL="explorer.smoke@test.com"
NEW_PASSWORD="SecurePass123!"

echo ""
echo "=== 1. Create signup session ==="
SESSION_JSON=$(curl -s -X POST "$BASE/api/v1/auth/signup/sessions" \
  -H "Content-Type: application/json" \
  -d '{"tenantId":"11111111-1111-1111-1111-111111111111","profileType":"CAREER_EXPLORER"}')
SESSION_ID=$(echo "$SESSION_JSON" | python3 -c "import sys,json; print(json.load(sys.stdin)['data']['sessionId'])")
echo "sessionId=$SESSION_ID"

echo ""
echo "=== 2. PUT session data (drop-out path, EMAIL only) ==="
PUT_BODY=$(GENDER_ID="$GENDER_ID" NEW_EMAIL="$NEW_EMAIL" NEW_PASSWORD="$NEW_PASSWORD" python3 - <<'PY'
import json, os
print(json.dumps({
  "sessionData": {
    "email": os.environ["NEW_EMAIL"],
    "password": os.environ["NEW_PASSWORD"],
    "firstName": "Smoke",
    "lastName": "Explorer",
    "dateOfBirth": "1995-06-15",
    "genderConfigId": os.environ["GENDER_ID"],
    "country": "India",
    "state": "Maharashtra",
    "howDidYouFindOut": "API smoke test",
    "consent": {
      "consentGiven": True,
      "consentType": "SELF",
      "consentTextVersion": "2026-27-v1"
    },
    "signupDetails": {
      "careerExplorer": {
        "preferredCommunicationChannels": ["EMAIL"],
        "reasonForDropOut": "Testing signup flow"
      }
    }
  }
}))
PY
)
curl -s -X PUT "$BASE/api/v1/auth/signup/sessions/$SESSION_ID" \
  -H "Content-Type: application/json" \
  -d "$PUT_BODY" | python3 -m json.tool | head -20

echo ""
echo "=== 3. Send email OTP ==="
curl -s -X POST "$BASE/api/v1/auth/signup/sessions/$SESSION_ID/otp/send" \
  -H "Content-Type: application/json" \
  -d '{"channel":"EMAIL"}' | python3 -m json.tool

sleep 1
OTP=$(grep -a '\[SIMULATED OTP\]' "$LOG_FILE" | tail -1 | sed -n 's/.* otp=\([0-9][0-9][0-9][0-9]\).*/\1/p')
if [[ -z "$OTP" ]]; then
  echo "ERROR: Could not read simulated OTP from log: $LOG_FILE" >&2
  exit 1
fi
echo "otp=$OTP"

echo ""
echo "=== 4. Verify OTP ==="
curl -s -X POST "$BASE/api/v1/auth/signup/sessions/$SESSION_ID/otp/verify" \
  -H "Content-Type: application/json" \
  -d "{\"channel\":\"EMAIL\",\"otp\":\"$OTP\"}" | python3 -m json.tool | head -15

echo ""
echo "=== 5. Complete registration ==="
REGISTER_JSON=$(curl -s -X POST "$BASE/api/v1/auth/signup/sessions/$SESSION_ID/complete")
echo "$REGISTER_JSON" | python3 -m json.tool
NEW_USER_ID=$(echo "$REGISTER_JSON" | python3 -c "import sys,json; d=json.load(sys.stdin); print(d.get('data',{}).get('userId',''))")
REG_STATUS=$(echo "$REGISTER_JSON" | python3 -c "import sys,json; d=json.load(sys.stdin); print(d.get('data',{}).get('status',''))")
echo "newUserId=$NEW_USER_ID registrationStatus=$REG_STATUS"

echo ""
echo "=== 6. Login as new user (expect ACTIVE) ==="
NEW_LOGIN=$(curl -s -X POST "$BASE/api/v1/auth/login" \
  -H "Content-Type: application/json" \
  -d "{\"loginId\":\"$NEW_EMAIL\",\"password\":\"$NEW_PASSWORD\",\"profileType\":\"CAREER_EXPLORER\"}")
echo "$NEW_LOGIN" | python3 -m json.tool | head -25

echo ""
echo "=== 7. Admin login ==="
ADMIN_LOGIN=$(curl -s -X POST "$BASE/api/v1/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"loginId":"admin@antarang.org","password":"password","profileType":"ADMINISTRATOR"}')
ADMIN_TOKEN=$(echo "$ADMIN_LOGIN" | python3 -c "import sys,json; print(json.load(sys.stdin)['data']['accessToken'])")

echo ""
echo "=== 8. PATCH status -> INACTIVE ==="
curl -s -X PATCH "$BASE/api/v1/users/$NEW_USER_ID/status" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"status":"INACTIVE"}' | python3 -m json.tool

echo ""
echo "=== 9. Login while INACTIVE (expect 401/USER_INACTIVE) ==="
curl -s -w "\nHTTP %{http_code}\n" -X POST "$BASE/api/v1/auth/login" \
  -H "Content-Type: application/json" \
  -d "{\"loginId\":\"$NEW_EMAIL\",\"password\":\"$NEW_PASSWORD\",\"profileType\":\"CAREER_EXPLORER\"}"

echo ""
echo "=== 10. PATCH status -> ACTIVE ==="
curl -s -X PATCH "$BASE/api/v1/users/$NEW_USER_ID/status" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"status":"ACTIVE"}' | python3 -m json.tool

echo ""
echo "=== 11. Login again (expect success) ==="
curl -s -X POST "$BASE/api/v1/auth/login" \
  -H "Content-Type: application/json" \
  -d "{\"loginId\":\"$NEW_EMAIL\",\"password\":\"$NEW_PASSWORD\",\"profileType\":\"CAREER_EXPLORER\"}" \
  | python3 -c "import sys,json; d=json.load(sys.stdin); print('success:', d.get('success'), 'status:', d.get('data',{}).get('user',{}).get('status'))"

echo ""
echo "=== DONE ==="
