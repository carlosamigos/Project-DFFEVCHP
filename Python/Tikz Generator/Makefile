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

report:
	@for file in $$(ls -d tex/report/snapshot_*.tex); \
	do \
		python3 tikz_stripper.py $$file; \
	done; \
	true
	@rm -f ../../../Report/Figures/ProblemDescription/*.tikz
	@for file in $$(ls -d tex/report/snapshot_*.tikz); \
	do \
		cp $$file ../../../Report/Figures/ProblemDescription; \
	done; \
	true
	@echo "Done copying"
	cd ../../../Report/Figures/ProblemDescription && git add * && git commit -m "Updated tikz files" && git pull && git push

clean:
	@rm -f tex/*aux
	@rm -f tex/*log
	@rm -f tex/*tex
	@rm -f tex/*pdf
