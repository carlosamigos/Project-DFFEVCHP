#!/bin/bash

E1="Aborting"
E2="Automatic merge failed"
E3="Pull is not possible because you have unmerged files."
S1="Merge made by the 'recursive' strategy"
S2="Already up-to-date."

echo "################"
echo "###Test suite###"
echo "################"
while true; do
    echo "Pulling latest changes from Github..."
    GITSTATUS="$(git pull 2>&1)"
    case $GITSTATUS in
        *$E1* )
            echo "There exist un-commited changes. Please resolve before running this script again."
            exit ;;
        *$E2* )
            echo "Automatic merge failed. Please fix manually before running this script again."
            exit ;;
	*$E3* )
            echo "Some files need manual merging. Please fix before running this script again."
	    exit ;;
	*$S1* )
	    break ;;
        *$S2* )
	    break ;;
    esac
    break;
done
echo "Done pulling."
echo "################"
echo " "
echo "################"

echo -n "If you want to the default constants please enter 'D': "
read DEFAULT

if [ "$DEFAULT" == "D" ] ; then
    if [ -f "../Java/src/constants/DefaultConstants.java" ] ; then
        mv "../Java/src/constants/DefaultConstants.java" "../Java/src/constants/Constants.java"
    fi
    cd ../Java
    ./runner.sh
    echo "################"
    exit
fi

echo "################"
echo " "
echo "################"
re='[1-2]'
re2='[1-9][0-9]*'

while true; do
    echo "Available static solvers: "
    echo "1. Mosel"
    echo "2. ALNS"
    echo -n "Please choose one (1 or 2): "
    read CHOSEN
    if [[ $CHOSEN =~ $re ]] ; then
        break
    fi
    echo "Must choose either 1 or 2"
    echo " "
done

if [ $CHOSEN == 1 ] ; then
    SOLVER="mosel"
    AVAILSOLVER=(`ls ../Mosel/Models`)
    COUNT=0
    echo " "
    echo "Available Mosel models"
    for i in ${AVAILSOLVER[*]}; do
       let "COUNT++"
       echo "$COUNT. $i"
    done

    while true; do
        if [[ $COUNT == 0 ]] ; then
            echo "There are no available model folders. Please add models before running this script."
    	    exit
        elif [[ $COUNT == 1 ]] ; then
    	    echo -n "There is only one available model folder. Enter 1 to use the model: "
        else
    	    echo -n "Choose the number associated with the test you want to run (1-$COUNT): "
        fi
        read CHOSENSOLVER
        if [[ $CHOSENSOLVER =~ $re2 ]] ; then
	    if [ "$CHOSENSOLVER" -le "$COUNT" ] ; then
    	        break;
	    fi
        fi
        echo "Must choose a legal number."
        echo " "
    done 
else
    SOLVER="alns"
fi
echo "################"
echo " "
echo "################"
while true; do
    echo "Available test modes: "
    echo "1. Static"
    echo "2. Dynamic"
    echo -n "Please choose one (1 or 2): "
    read MODE
    if [[ $MODE =~ $re ]] ; then
        break;
    fi
    echo "Must choose either 1 or 2"
    echo " "
done



if [[ $MODE == 1 ]] ; then
    TYPE="Static"
else
    TYPE="Dynamic"
fi

FOLDER="Input/$TYPE"
ARRAY=(`ls $FOLDER`)
COUNT1=0

echo "################"
echo " "
echo "################"
echo "Available tests"
for i in ${ARRAY[*]}; do
    let "COUNT1++"
    echo "$COUNT1. $i"
done

while true; do
    if [[ $COUNT1 == 0 ]] ; then
        echo "There are no available test folders. Please add tests before running this script."
    	exit
    elif [[ $COUNT1 == 1 ]] ; then
    	echo -n "There is only one available test folder. Enter 1 to run the test: "
    else
    	echo -n "Choose the number associated with the test you want to run (1-$COUNT1): "
    fi
    read CHOSEN
    if [[ $CHOSEN =~ $re2 ]] ; then
	if [ "$CHOSEN" -le "$COUNT1" ] ; then
    	    break;
	fi
    fi
    echo "Must choose a legal number."
    echo " "
done 

CHOSEN=$(( $CHOSEN - 1))
NAME=${ARRAY[$CHOSEN]}

if [ ! -d "Output" ] ; then
    mkdir Output
fi

if [ ! -d "Output/$TYPE" ] ; then
    mkdir Output/$TYPE
fi

if [ ! -d "Output/$TYPE/$NAME" ] ; then
    mkdir Output/$TYPE/$NAME
fi

if [ -f "Input/$TYPE/$NAME/Constants.java" ] ; then 
    if [ ! -f "../Java/src/constants/DefaultConstants.java" ] ; then
        mv "../Java/src/constants/Constants.java" "../Java/src/constants/DefaultConstants.java"
    fi
    cp "Input/$TYPE/$NAME/Constants.java" "../Java/src/constants/Constants.java"
else
    if [ -f "../Java/src/constants/DefaultConstants.java" ] ; then
        mv "../Java/src/constants/DefaultConstants.java" "../Java/src/constants/Constants.java"
    fi
fi


echo "################"
cd ../Java
module load xpress/8.0.4
module load java/9.0.1
ant clean
ant build

TEST_TYPE="${TYPE,,}"

java -cp "./bin:/share/apps/xpress/8.0.4/lib/xprm.jar" tio4500.Main "$TEST_TYPE:$NAME" solver:$SOLVER
