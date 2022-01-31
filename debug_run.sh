#!/bin/bash

HOME_PATH=$(pwd)
export HOME_PATH

CLASSPATH=$HOME_PATH/build/classes/java/main:$HOME_PATH/assets:$HOME_PATH/src:$HOME_PATH/thirdparty/batik.jar:$HOME_PATH/thirdparty/formsrt.jar:$HOME_PATH/thirdparty/jh.jar:$HOME_PATH/thirdparty/junit.jar
export CLASSPATH


java -classpath $CLASSPATH com.jeta.abeille.main.Launcher
