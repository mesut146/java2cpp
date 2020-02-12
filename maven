#!/bin/sh

if [ $1 == "jar" ]
 then
  mvn compile assembly:single
elif [ $1 == "run" ]
then
  mvn exec:java -D"exec.mainClass"="com.mesut.j2cpp.Main"
elif [ $1 == "comp" ]
then
  mvn compile
fi

