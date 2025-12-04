FROM myctf/base

RUN apt-get update && apt-get install -y gcc gdb make && rm -rf /var/lib/apt/lists/*

COPY exploit.c /challenge/exploit.c
COPY build.sh /challenge/build.sh

RUN chmod +x /challenge/build.sh && cd /challenge && ./build.sh

# challenge binary should be executed by user inside container
RUN chown -R ctfuser:ctfuser /challenge
