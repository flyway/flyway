#
# Copyright 2010-2017 Boxfuse GmbH
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#         http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

FROM ubuntu:14.04
RUN sudo apt-get update && \
    sudo apt-get install openjdk-7-jre wget -y && \
    wget https://get.enterprisedb.com/ga/ppasmeta-9.5.0.5-linux-x64.tar.gz && \
	tar xf ppasmeta-9.5.0.5-linux-x64.tar.gz && \
	/ppasmeta-9.5.0.5-linux-x64/ppasmeta-9.5.0.5-linux-x64.run --mode unattended --webusername support@flywaydb.org --webpassword flywayPWD123 --superpassword flyway --create_samples no --serverport 5445
EXPOSE 5445
ENTRYPOINT /bin/bash