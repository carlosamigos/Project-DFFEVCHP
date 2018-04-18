#!/bin/bash

FILES=(`ls -I copier.sh -I cleaner.sh`)

if [[ $1 -ge 2 ]]; then
	echo "Copying input files..."
	for i in ${FILES[*]}; do
		for j in $(seq 1 $1); do
			NAME="${i::-4}"
			cp $i ${NAME}_${j}.txt
		done
		rm $i
	done
fi
