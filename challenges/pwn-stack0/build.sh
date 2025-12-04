#!/bin/bash
gcc exploit.c -o challenge -fno-stack-protector -z execstack -no-pie
