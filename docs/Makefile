all: clean site

html:
	@cp -R static/* target/
	@rm target/template.html
	@for file in *.md; do \
		pandoc --html5 --toc --normalize --smart --template static/template.html -o "target/$${file%%.md}.html" < $$file ;\
	done

clean:
	@rm -rf target/*

site: clean html
	@open target/index.html

upload: clean html
	rsync -avz target/ codahale.com:/home/codahale/metrics.codahale.com/
