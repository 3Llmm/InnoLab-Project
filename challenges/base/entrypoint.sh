#!/bin/sh
set -e

echo "🚀 Starting CTF container..."

# CRITICAL: Generate SSH host keys (Alpine doesn't auto-generate them)
if [ ! -f /etc/ssh/ssh_host_rsa_key ]; then
    echo "🔑 Generating SSH host keys..."
    ssh-keygen -A
    echo "✅ SSH host keys generated"
fi

# Write FLAG from environment variable
if [ -n "$FLAG" ]; then
    echo "$FLAG" > /flag.txt
    chown ctfuser:ctfuser /flag.txt
    chmod 600 /flag.txt
    echo "✅ Flag written to /flag.txt"
else
    echo "⚠️  No FLAG environment variable set"
fi

# Ensure proper permissions
chown -R ctfuser:ctfuser /home/ctfuser
chmod 755 /home/ctfuser

# Start SSH server in foreground
echo "📡 Starting SSH server..."
exec /usr/sbin/sshd -D -e