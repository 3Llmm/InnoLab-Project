FROM myctf/base

# Add welcome banner
COPY welcome.txt /home/ctfuser/welcome.txt
RUN chown ctfuser:ctfuser /home/ctfuser/welcome.txt

# The base image already:
# - creates ctfuser
# - starts SSH
# - writes /flag.txt dynamically
# - keeps container alive
