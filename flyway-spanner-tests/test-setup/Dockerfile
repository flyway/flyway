#
# Copyright 2010-2020 Redgate Software Ltd
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

FROM gcr.io/cloud-spanner-emulator/emulator as emulator
FROM ubuntu:18.04

# Expose ports
EXPOSE 9010 9020

# Install curl
RUN apt-get update
RUN apt-get -y install curl

COPY --from=emulator emulator_main /
COPY --from=emulator gateway_main /
COPY ./start_emulator_with_instance_and_database.sh /
RUN chmod +x /start_emulator_with_instance_and_database.sh
CMD ["/start_emulator_with_instance_and_database.sh"]
