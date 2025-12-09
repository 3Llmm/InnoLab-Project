# challenges/base/Dockerfile
FROM alpine:latest

# Install necessary packages
RUN apk add --no-cache \
    openssh-server \
    bash \
    sudo \
    curl \
    nano \
    vim \
    && mkdir -p /run/sshd

# Create CTF user
RUN adduser -D -s /bin/bash ctfuser && \
    echo "ctfuser:ctfpassword" | chpasswd

# Configure SSH
RUN sed -i 's/#PermitRootLogin.*/PermitRootLogin no/' /etc/ssh/sshd_config && \
    sed -i 's/#PasswordAuthentication.*/PasswordAuthentication yes/' /etc/ssh/sshd_config && \
    sed -i 's/#AllowTcpForwarding.*/AllowTcpForwarding yes/' /etc/ssh/sshd_config

# Create home directory and set permissions
RUN mkdir -p /home/ctfuser && \
    chown -R ctfuser:ctfuser /home/ctfuser

# Copy entrypoint script
COPY entrypoint.sh /entrypoint.sh
RUN chmod +x /entrypoint.sh

EXPOSE 22

ENTRYPOINT ["/entrypoint.sh"]