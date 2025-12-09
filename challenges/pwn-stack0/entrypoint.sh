#!/bin/sh
set -e

echo "🚀 Starting pwn-stack0 CTF challenge..."

# CRITICAL: Generate SSH host keys
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
    # Default test flag for development
    echo "CTF{test_flag_pwn_stack0_dev}" > /flag.txt
    chown ctfuser:ctfuser /flag.txt
    chmod 600 /flag.txt
    echo "⚠️  Using default test flag"
fi

# COMPILE THE CHALLENGE in user's home directory
echo "🔨 Setting up challenge files..."

# Ensure challenge files are in user's home
cd /home/ctfuser

# If exploit.c exists, compile it
if [ -f exploit.c ]; then
    echo "📦 Compiling challenge..."
    gcc exploit.c -o challenge -fno-stack-protector -z execstack -no-pie
    chmod +x challenge
    echo "✅ Challenge compiled successfully"
    
    # Create welcome message
    echo "Welcome to pwn-stack0 buffer overflow challenge!" > welcome.txt
    echo "" >> welcome.txt
    echo "Your goal: Overflow the buffer and call win() to get the flag." >> welcome.txt
    echo "The binary 'challenge' is vulnerable to buffer overflow via gets()." >> welcome.txt
    echo "Source code: exploit.c" >> welcome.txt
    echo "" >> welcome.txt
    echo "Try: ./challenge" >> welcome.txt
    echo "Then exploit it to call the win() function." >> welcome.txt
fi

# Ensure proper permissions
chown -R ctfuser:ctfuser /home/ctfuser
chmod 755 /home/ctfuser

# Start SSH server in foreground
echo "📡 Starting SSH server..."
echo "✅ Challenge ready! Connect via SSH as ctfuser"
exec /usr/sbin/sshd -D -e