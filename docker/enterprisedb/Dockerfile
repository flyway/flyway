FROM ubuntu:14.04
COPY ppasmeta-9.5.0.5-linux-x64.tar.gz /install/ppasmeta-9.5.0.5-linux-x64.tar.gz
RUN wget https://get.enterprisedb.com/ga/ppasmeta-9.5.0.5-linux-x64.tar.gz && \
	tar xf ppasmeta-9.5.0.5-linux-x64.tar.gz && \
    sudo apt-get update && \
    sudo apt-get install openjdk-7-jre -y && \
	/ppasmeta-9.5.0.5-linux-x64/ppasmeta-9.5.0.5-linux-x64.run --mode unattended --webusername support@flywaydb.org --webpassword flywayPWD123 --superpassword flyway --create_samples no --serverport 5445
EXPOSE 5445
ENTRYPOINT /bin/bash