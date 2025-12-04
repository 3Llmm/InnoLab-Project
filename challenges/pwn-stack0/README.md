# pwn-stack0

Simple Buffer Overflow challenge.

Goal:
Trigger the overflow in exploit.c and call win().

Compile flags:
- -fno-stack-protector
- -z execstack
- -no-pie

Run:
./challenge
