#!/bin/bash

FILES=(`ls -I cleaner.sh -I copier.sh`)
COUNT=0

for i in ${FILES[*]}; do
	CHECK=$(($COUNT%$1))
	if [[ $CHECK -ne 0 ]] ; then
		rm $i
	else
		NAME="${i::-6}"
		mv $i ${NAME}.txt
	fi
	let "COUNT++"
done	
