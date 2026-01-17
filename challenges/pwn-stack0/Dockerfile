FROM myctf/base

# Install gcc and C library
RUN apk update && apk add gcc gdb make musl-dev

# Copy challenge files from docker/ subfolder to user's home directory
COPY docker/exploit.c /home/ctfuser/exploit.c
COPY docker/build.sh /home/ctfuser/build.sh
COPY docker/README.md /home/ctfuser/README.md

# Copy entrypoint script with proper permissions
COPY docker/entrypoint.sh /entrypoint.sh
RUN chmod +x /entrypoint.sh && \
    chown root:root /entrypoint.sh && \
    # Ensure script has Unix line endings
    sed -i 's/\r$//' /entrypoint.sh

# Set proper permissions
RUN chown -R ctfuser:ctfuser /home/ctfuser

# Default working directory for user
WORKDIR /home/ctfuser

EXPOSE 22

ENTRYPOINT ["/entrypoint.sh"]