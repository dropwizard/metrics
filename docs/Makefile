build:
	mook

upload: build
	rsync -avz target/ codahale.com:/home/codahale/metrics.codahale.com/
