#!/bin/bash

if [ "$1" == "png" ]
then
	echo "Starting png conversion..."
	for file in $(ls -d tex/snapshot_*.pdf)
	do
		convert -density 400 $file "${file:0:$((${#file}-4))}.png"
		echo "Succesfully converted ${file}"
	done
fi


rm -f tex/*aux
rm -f tex/*log
rm -f tex/*tex

echo "Starting python..."
python3 tikz_generator.py
echo "Python done"
echo "Starting latex..."

for file in $(ls -d tex/snapshot_*.tex)
do
	latex -output-format='pdf' -output-directory=tex $file > /dev/null
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
	rm -f out.mp4
	ffmpeg -r 1/2 -i tex/snapshot_%01d.png -c:v libx264 -r 30 -pix_fmt yuv420p out.mp4 -vf "scale=trunc(iw/2)*2:trunc(ih/2)*2"
fi
