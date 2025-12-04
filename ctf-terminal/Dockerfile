FROM node:18-alpine

# Install Docker CLI (needed for Docker socket access)
RUN apk add --no-cache docker-cli

WORKDIR /app

COPY package*.json ./
RUN npm ci --only=production

COPY server.js ./

# Ensure the app runs as root or docker group
USER root

EXPOSE 3001

CMD ["node", "server.js"]