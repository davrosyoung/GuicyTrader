#!/usr/bin/env bash
#JAR=guicy-trader-1.0-SNAPSHOT.jar
JAR=guicy-trader-1.0-SNAPSHOT-jar-with-dependencies.jar
java -cp target/${JAR} au.com.livewire.Assignment --exchange ASX --profile some.properties

