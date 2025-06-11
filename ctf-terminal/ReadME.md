
### Installing the dependencies

```sh
npm install express ws node-pty
```

### Starting the application 

```sh
node server.js
```



### To run docker

```sh
docker run -p 3001:3001 -v /var/run/docker.sock:/var/run/docker.sock ctf-terminal
```