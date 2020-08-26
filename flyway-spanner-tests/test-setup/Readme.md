# Spanner emulator test setup

The container does the following:
1. Starts an emulator
2. Creates a default instance (test-instance) and database (test_database).

Steps to execute this:
1. build an image:
`docker build -t start-spanner-emulator .`

2. run docker container:
`docker run --detach --name emulator -p 9010:9010 -p 9020:9020 start-spanner-emulator`

2b. Verify the container is running:
`docker ps`

3. stop docker when your IT tests complete:
`docker stop emulator`

You can verify this is working by using gcloud commands:
$ gcloud config list
[api_endpoint_overrides]
spanner = http://0.0.0.0:9020/
[auth]
disable_credentials = true
[core]
project = test-project

$ gcloud spanner instances list --project=test-project
NAME           DISPLAY_NAME   CONFIG           NODE_COUNT  STATE
test_instance  Test Instance  emulator-config  1           READY

$ gcloud spanner databases list --instance=test_instance --project=test-project
NAME           STATE
test_database  READY

4. Connect in Flyway with JDBC URL: `jdbc:cloudspanner://localhost:9010/projects/test-project/instances/test-instance/databases/test_database`
