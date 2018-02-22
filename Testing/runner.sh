#!/bin/bash

echo -n "Username: "
read USER

case $USER in
    "magnusnm" )
        echo "PbY8!12" ;;
    "carlaj" )
        echo "Yx66!aB" ;;
    "simehe" )
        echo "" ;;
esac

ARRAY=(`ls Input`)
for i in ${ARRAY[*]}; do
    echo "$i";
done
