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

png:
	@rm -f tex/*png
	@echo "Starting png conversion..."
	@for file in $$(ls -d tex/snapshot_*.pdf); \
	do \
		convert -density 400 $$file -background "#FFFFFF" -flatten "$${file:0:$$(($${#file}-4))}.png"; \
		echo "Successfully converted $${file}"; \
	done; \
	true
	@height=$$(sips -g pixelHeight tex/*0.png | tail -n1 | cut -d" " -f4)
	@width=$$(sips -g pixelWidth tex/*0.png | tail -n1 | cut -d" " -f4)
	@pair_h=$$(($$height & 1))
	@pair_w=$$(($$width & 1))
	@changed="0"
	@if [ $$pair_h -eq 1 ]; \
	then
		height=$$(($$height + 1)); \
		changed="1"; \
	fi; \
	true
	@if [ $$pair_w -eq 1 ]; \
	then \
		width=$$(($$width + 1)); \
		changed="1"; \
	fi; \
	true
	@if [ $$changed == "1" ]; \
	then \
		for file in $$(ls -d tex/snapshot_*.png); \
		do \
			echo "Resizing $${file}"; \
			convert $$file -background transparent -extent "$${width}x$${height}" $$file; \
		done; \
	fi; \
	true


clean:
	@rm -f tex/*aux
	@rm -f tex/*log
	@rm -f tex/*tex
	@rm -f tex/*pdf
