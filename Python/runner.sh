#!/bin/bash

if [ "$1" == "png" ]
then
	echo "Starting png conversion..."
	for file in $(ls -d tex/snapshot_*.pdf)
	do
		convert -density 400 $file -background "#FFFFFF" -flatten "${file:0:$((${#file}-4))}.png"
		echo "Succesfully converted ${file}"
	done
else
	rm -f tex/*aux
	rm -f tex/*log
	rm -f tex/*tex

	echo "Starting python..."
	python3 tikz_generator.py
	echo "Python done"
	echo "Starting latex..."

	for file in $(ls -d tex/snapshot_*.tex)
	do
		latex -output-directory=tex -output-format="pdf" $file > /dev/null
		echo "Succesfully compiled ${file}"
	done

	echo "Latex done"

	rm -f tex/*.aux
	rm -f tex/*.log

	if [ "$1" == "git" ] || [ "$2" == "git" ]
	then
		git add tex/snapshot_*.tex
		git commit -m "Test run"
		git push
	fi

	if [ "$1" == "video" ] || [ "$2" == "video" ]
	then
		height=$(sips -g pixelHeight tex/*0.png | tail -n1 | cut -d" " -f4)
		width=$(sips -g pixelWidth tex/*0.png | tail -n1 | cut -d" " -f4)
		pair_h=$(($height & 1))
		pair_w=$(($width & 1))
		changed="0"
		
		if [ $pair_h -eq 1 ]
		then
			height=$(($height + 1))
			changed="1"
		fi

		if [ $pair_w -eq 1 ]
		then
			width=$(($width + 1))
			changed="1"
		fi

		if [ $changed == "1" ]
		then
			for file in $(ls -d tex/snapshot_*.png)
			do
				echo "Resizing ${file}"
				convert $file -background transparent -extent "${width}x${height}" $file
			done
		fi

		rm -f *.mp4
		ffmpeg -framerate 2 -i tex/snapshot_%d.png -pix_fmt yuv420p output.mp4
	fi

fi

