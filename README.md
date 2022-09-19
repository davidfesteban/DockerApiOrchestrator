For working with docker:

- Install Docker
- Install Socat. (brew install socat or sudo apt install socat)
- Run: socat TCP-LISTEN:2375,reuseaddr,fork UNIX-CONNECT:/var/run/docker.sock &

The goal is to enable the access to docker api remotely just. Easiest to access from localhost

---

Or, you can on run to add:

--adminPass=yoursudopass to the main script

---
Based on:

https://docs.docker.com/engine/api/v1.41/



