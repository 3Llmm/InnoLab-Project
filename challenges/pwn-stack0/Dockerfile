FROM myctf/base

# Install gcc and C library
RUN apk update && apk add gcc gdb make musl-dev

# Copy challenge files to user's home directory
COPY exploit.c /home/ctfuser/exploit.c
COPY build.sh /home/ctfuser/build.sh
COPY README.md /home/ctfuser/README.md

# Copy entrypoint script
COPY entrypoint.sh /entrypoint.sh
RUN chmod +x /entrypoint.sh

# Set proper permissions
RUN chown -R ctfuser:ctfuser /home/ctfuser

# Default working directory for user
WORKDIR /home/ctfuser

EXPOSE 22

ENTRYPOINT ["/entrypoint.sh"]