#!/usr/bin/env bash

# First update base packages of minimal image
apt update
apt upgrade -y

# Now create non-root sudo user and switch to it
adduser axel
usermod -aG sudo axel
su - axel

# Set timezone to UTC
sudo timedatectl set-timezone UTC

# Install Git and ensure credentials will be cached for 10 years
sudo apt install git -y
git config --global credential.helper 'cache --timeout=360000000'

# Install Docker CE and automatically run it on system startup
sudo apt install apt-transport-https ca-certificates curl gnupg software-properties-common -y
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo apt-key add -
sudo add-apt-repository "deb [arch=amd64] https://download.docker.com/linux/ubuntu $(lsb_release -cs) stable"
sudo apt install docker-ce docker-compose -y
sudo systemctl enable docker
sudo usermod -aG docker $(whoami)
docker login

# Install Jenkins
curl -fsSL https://pkg.jenkins.io/debian/jenkins.io.key | sudo apt-key add -
sudo add-apt-repository "deb http://pkg.jenkins.io/debian-stable binary/"
sudo apt install openjdk-8-jdk-headless openjdk-11-jdk-headless jenkins -y
sudo usermod -aG shadow jenkins