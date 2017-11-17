module load xpress/8.0.4
module load java/9.0.1
ant clean
ant build
java -cp "./bin:/share/apps/xpress/8.0.4/lib/xprm.jar" tio4500.Main
