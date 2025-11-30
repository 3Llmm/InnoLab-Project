#!/bin/bash
set -e

# 1) Write FLAG
echo "$FLAG" > /flag.txt
chown ctfuser:ctfuser /flag.txt
chmod 600 /flag.txt


# 2) Start SSH
service ssh start

# 3) Copy flag into challenge folder (Linux Basics only)
if [ -f /flag.txt ]; then
    mkdir -p /home/ctfuser/hidden
    cp /flag.txt /home/ctfuser/hidden/flag.txt
    chown ctfuser:ctfuser /home/ctfuser/hidden/flag.txt
fi

# 4) KEEP CONTAINER ALIVE (MUST BE LAST)
tail -f /dev/null
