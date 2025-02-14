LINUX_ONLY ?= false
get_artifacts:
	python ./scripts/download_artifacts.py $(EDITION) $(VERSION)	
build:
	python ./scripts/build_images.py $(EDITION) $(VERSION) $(LINUX_ONLY)
test:
	python ./scripts/test_images.py $(EDITION) $(VERSION) $(LINUX_ONLY) $(EXTRA_ARGS)
release:
	python ./scripts/release.py $(EDITION) $(VERSION)
