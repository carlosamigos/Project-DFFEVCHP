module load xpress/8.0.4
module load java/jdk-9.0.4
ant clean
ant build
java -cp "./bin:/share/apps/xpress/8.0.4/lib/xprm.jar" code.Main
