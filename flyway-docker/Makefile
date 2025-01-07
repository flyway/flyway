get_artifacts:
	python ./scripts/download_artifacts.py $(EDITION) $(VERSION)
	
build:
	python ./scripts/build_images.py $(EDITION) $(VERSION)

test:
	python ./scripts/test_images.py $(EDITION) $(VERSION) $(EXTRA_ARGS)

release:
	python ./scripts/release.py $(EDITION) $(VERSION)
