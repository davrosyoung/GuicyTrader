#!/usr/bin/env bash
EXCHANGE="ASX"
if [ "Z"$1 != "Z" ]
then EXCHANGE=$1
fi
echo "exchange="${EXCHANGE}
JAR=guicy-trader-1.0-SNAPSHOT.jar
#JAR=guicy-trader-1.0-SNAPSHOT-jar-with-dependencies.jar
java -cp target/${JAR} au.com.livewire.Assignment --exchange ${EXCHANGE} --profile some.properties

