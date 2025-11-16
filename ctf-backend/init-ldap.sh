#!/bin/sh
set -e

echo "[INIT] Waiting for LDAP to be ready..."

# Wait for LDAP port to be open (max 60s)
timeout 60 sh -c 'until nc -z ldap 389; do sleep 2; done'
echo "[INIT] LDAP is ready!"

# Create users OU
cat <<EOF > /tmp/create_ou_users.ldif
dn: ou=users,dc=ctf,dc=local
objectClass: organizationalUnit
ou: users
EOF

# Create test user
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

# Create admin user
cat <<EOF > /tmp/create_admin_user.ldif
dn: uid=admin,ou=users,dc=ctf,dc=local
objectClass: inetOrgPerson
objectClass: posixAccount
objectClass: top
cn: Admin User
sn: User
uid: admin
uidNumber: 1000
gidNumber: 1000
homeDirectory: /home/admin
loginShell: /bin/bash
userPassword: admin123
EOF

echo "[INIT] Adding LDAP entries..."

# Add entries with retry logic
for i in 1 2 3; do
    echo "Attempt $i to add LDAP entries..."

    ldapadd -x -H ldap://ldap:389 -D "cn=admin,dc=ctf,dc=local" -w admin -f /tmp/create_ou_users.ldif || true
    ldapadd -x -H ldap://ldap:389 -D "cn=admin,dc=ctf,dc=local" -w admin -f /tmp/create_test_user.ldif || true
    ldapadd -x -H ldap://ldap:389 -D "cn=admin,dc=ctf,dc=local" -w admin -f /tmp/create_admin_user.ldif || true

    # Verify admin user was created
    if ldapsearch -x -LLL -H ldap://ldap:389 -D "cn=admin,dc=ctf,dc=local" -w admin -b "ou=users,dc=ctf,dc=local" "(uid=admin)" | grep -q "dn:"; then
        echo "[SUCCESS] Admin user created successfully!"
        break
    else
        echo "[RETRY] Admin user not found, retrying..."
        sleep 5
    fi
done

echo "[INIT] Final verification of LDAP users:"
ldapsearch -x -LLL -H ldap://ldap:389 -D "cn=admin,dc=ctf,dc=local" -w admin -b "ou=users,dc=ctf,dc=local" "(uid=*)"

echo "[INIT] LDAP setup completed!"