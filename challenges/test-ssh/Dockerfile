# challenges/test-ssh/Dockerfile
FROM myctf/base

# Add welcome banner
COPY welcome.txt /home/ctfuser/welcome.txt
RUN chown ctfuser:ctfuser /home/ctfuser/welcome.txt

# Create a test directory structure
RUN mkdir -p /home/ctfuser/test && \
    echo "Test file content" > /home/ctfuser/test/example.txt && \
    chown -R ctfuser:ctfuser /home/ctfuser/test
