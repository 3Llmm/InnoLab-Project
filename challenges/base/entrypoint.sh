#!/bin/bash
set -e

echo "$FLAG" > /flag.txt
chmod 600 /flag.txt

service ssh start

tail -f /dev/null
