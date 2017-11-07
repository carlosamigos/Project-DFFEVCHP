#!/bin/bash

if [ "$1" == "png" ]
then
	rm -f tex/*png
	echo "Starting png conversion..."
	for file in $(ls -d tex/snapshot_*.pdf)
	do
		convert -density 400 $file -background "#FFFFFF" -flatten "${file:0:$((${#file}-4))}.png"
		echo "Succesfully converted ${file}"
	done

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
		echo "Starting resizing..."
		for file in $(ls -d tex/snapshot_*.png)
		do
			convert $file -background transparent -extent "${width}x${height}" $file
			echo "Succesfully resized ${file}"
		done
	fi
fi

if [ "$1" == "video"] || [ "$2" == "video" ]
then
	rm -f *.mp4
	ffmpeg -framerate 5 -i tex/snapshot_%3d.png -vb 20M -pix_fmt yuv420p video.mp4

fi

