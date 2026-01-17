#!/bin/bash

echo "=== Starting Dynamic Flag Test Challenge ==="

# Generate SSH host keys
echo "Generating SSH host keys..."
ssh-keygen -t rsa -f /etc/ssh/ssh_host_rsa_key -N '' -q
ssh-keygen -t ecdsa -f /etc/ssh/ssh_host_ecdsa_key -N '' -q
ssh-keygen -t ed25519 -f /etc/ssh/ssh_host_ed25519_key -N '' -q

# Create necessary directories
mkdir -p /var/run/sshd
mkdir -p /var/log
mkdir -p /challenge

# Start SSH server
echo "Starting SSH server..."
/usr/sbin/sshd -D -p 22 -f /etc/ssh/sshd_config -E /var/log/sshd.log &

# Give SSH time to start
sleep 5

# Create the flag file in multiple locations
echo "Creating flag files..."
echo "$FLAG" > /flag.txt
echo "$FLAG" > /challenge/flag.txt
echo "$FLAG" > /home/ctfuser/flag.txt
echo "$FLAG" > /root/flag.txt

# Create welcome message
cat > /welcome.txt << EOF
=== Dynamic Flag Test Challenge ===
Your unique flag has been generated!

Flag: $FLAG

The flag has been placed in multiple locations:
- /flag.txt
- /challenge/flag.txt
- /home/ctfuser/flag.txt
- /root/flag.txt

You can also get the flag directly from the environment variable:
echo \$FLAG

To complete this challenge:
1. Find the flag using one of the methods above
2. Copy the flag
3. Submit it through the platform
EOF

# Copy welcome message to user's home directory
cp /welcome.txt /home/ctfuser/welcome.txt

# Set proper permissions
chown ctfuser:ctfuser /home/ctfuser/flag.txt
chown ctfuser:ctfuser /home/ctfuser/welcome.txt

echo "Challenge setup complete!"
echo "Flag: $FLAG"
echo "SSH server is running on port 22"

# Keep container running
exec tail -f /dev/null