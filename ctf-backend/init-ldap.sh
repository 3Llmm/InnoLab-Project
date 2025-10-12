#!/bin/sh
set -e

echo "[INIT] Waiting for LDAP to be ready..."

# Wait for LDAP port to be open (max 30s)
timeout 30 sh -c 'until nc -z ldap 389; do sleep 1; done'

# Your LDIF content creation
cat <<EOF > /tmp/create_ou_users.ldif
dn: ou=users,dc=ctf,dc=local
objectClass: organizationalUnit
ou: users
EOF

cat <<EOF > /tmp/create_test_user.ldif
dn: uid=testuser,ou=users,dc=ctf,dc=local
objectClass: inetOrgPerson
objectClass: posixAccount
objectClass: top
cn: Test User
sn: User
uid: testuser
uidNumber: 1001
gidNumber: 1001
homeDirectory: /home/testuser
loginShell: /bin/bash
userPassword: password
EOF

# Run inside LDAP container via hostname (compose handles networking)
ldapadd -x -H ldap://ldap:389 -D "cn=admin,dc=ctf,dc=local" -w admin -f /tmp/create_ou_users.ldif
ldapadd -x -H ldap://ldap:389 -D "cn=admin,dc=ctf,dc=local" -w admin -f /tmp/create_test_user.ldif

ldapsearch -x -LLL -H ldap://ldap:389 -D "cn=admin,dc=ctf,dc=local" -w admin -b "ou=users,dc=ctf,dc=local" "(uid=testuser)"
