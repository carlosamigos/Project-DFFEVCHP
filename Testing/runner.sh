#!/bin/bash

USER="simehe"
#echo -n "Username: "
#read USER

case $USER in
    "magnusnm" )
        echo "PbY8!12" | pbcopy ;;
    "carlaj" )
        echo "Yx66!aB" | pbcopy ;;
    "simehe" )
        echo "pW88*b2" | pbcopy ;;
esac

ssh $USER@solstorm-login.iot.ntnu.no
