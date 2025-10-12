FROM node:20-alpine

# Update Alpine packages and install docker client
RUN apk update && apk upgrade && apk add --no-cache make gcc g++ python3 docker

# Set workdir
WORKDIR /app

# Copy source code
COPY . .

# Initialize package.json and install dependencies
RUN npm init -y \
 && npm install express ws node-pty

# Expose your server port
EXPOSE 3001

# Start server
CMD ["node", "server.js"]
