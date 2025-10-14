TEST_MODE ?= false
# Optional variables:
#   DRY_RUN=true       -> append --dry-run to build/test
#   ENV_PASS=VAR_NAME  -> pass an environment variable through to test containers

get_artifacts:
	python ./scripts/download_artifacts.py $(EDITION) $(VERSION)	
build:
	python ./scripts/build_images.py $(EDITION) $(VERSION) $(TEST_MODE) $(if $(DRY_RUN),--dry-run,)
test:
	python ./scripts/test_images.py $(EDITION) $(VERSION) $(TEST_MODE) $(if $(ENV_PASS),--env-pass $(ENV_PASS),) $(if $(DRY_RUN),--dry-run,)
release:
	python ./scripts/release.py $(EDITION) $(VERSION) $(TEST_MODE)
