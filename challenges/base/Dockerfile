FROM ubuntu:22.04

ENV DEBIAN_FRONTEND=noninteractive

RUN apt-get update && apt-get install -y \
    openssh-server sudo bash \
    && rm -rf /var/lib/apt/lists/*

RUN useradd -m -s /bin/bash ctfuser \
    && echo "ctfuser:ctfpassword" | chpasswd

RUN mkdir /var/run/sshd

COPY entrypoint.sh /entrypoint.sh
RUN chmod +x /entrypoint.sh

EXPOSE 22

ENTRYPOINT ["/entrypoint.sh"]
