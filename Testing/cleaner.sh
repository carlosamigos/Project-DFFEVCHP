#!/bin/bash

FILES=(`ls -I cleaner.sh -I copier.sh`)
COUNT=0

if [[ $1 -ge 1 ]]; then
	for i in ${FILES[*]}; do
		CHECK=$(($COUNT%$1))
		if [[ $CHECK -ne 0 ]] ; then
			rm $i
		else
			if [[ $1 -ge 9 ]] ; then
				NAME="{$i::-7}"
			else
				NAME="${i::-6}"
			fi
			mv $i ${NAME}.txt
		fi
		let "COUNT++"
	done	
fi

