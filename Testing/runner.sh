#!/bin/bash

echo -n "Username: "
read USER

case $USER in
    "magnusnm" )
        echo "PbY8!12" | pbcopy ;;
    "carlaj" )
        echo "Yx66!aB" | pbcopy ;;
    "simehe" )
        echo "" | pbcopy ;;
esac

ssh $USER@solstorm-login.iot.ntnu.no
