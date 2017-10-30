all: clean python latex
	

python:
	@echo "Starting Python..."
	@python3 tikz_generator.py
	@echo "Python done"

latex:
	@echo "Starting Latex..."
	@for file in $$(ls -d tex/snapshot_*.tex); \
	do \
		latex -output-directory=tex -output-format="pdf" $$file 1> /dev/null; \
		[[ $$? -eq 1 ]] && echo "Failed to compile $${file}"; \
		echo "Succesfully compiled $${file}"; \
	done; \
	true
	@echo "Latex done..."
	@rm -f tex/*aux
	@rm -f tex/*log

clean:
	@rm -f tex/*aux
	@rm -f tex/*log
	@rm -f tex/*tex
	@rm -f tex/*pdf
