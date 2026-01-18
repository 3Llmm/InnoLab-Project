FROM alpine:latest

# Install required packages
RUN apk add --no-cache openssh-server bash

# Create user
RUN adduser -D -s /bin/bash ctfuser && \
    echo "ctfuser:ctfpassword" | chpasswd

# Configure SSH
RUN echo "PermitRootLogin yes" >> /etc/ssh/sshd_config && \
    echo "PasswordAuthentication yes" >> /etc/ssh/sshd_config && \
    echo "UsePrivilegeSeparation no" >> /etc/ssh/sshd_config && \
    echo "UsePAM no" >> /etc/ssh/sshd_config

# Create necessary directories
RUN mkdir -p /var/run/sshd && \
    mkdir -p /root/.ssh && \
    chmod 700 /root/.ssh

WORKDIR /challenge

# Copy and fix permissions
COPY docker/entrypoint.sh /entrypoint.sh
RUN chmod +x /entrypoint.sh && \
    # Ensure proper line endings
    sed -i 's/\r$//' /entrypoint.sh

EXPOSE 22

ENTRYPOINT ["/entrypoint.sh"]