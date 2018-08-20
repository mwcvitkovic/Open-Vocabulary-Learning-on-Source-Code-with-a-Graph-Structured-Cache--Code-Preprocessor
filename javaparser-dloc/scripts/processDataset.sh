#!/bin/bash

echo "=================================================================================================================="
echo "Going to process directory: $1"

input=$2"/"$1
output=$2"/"$1"/output"

check_return()
{
  if [ ! $? -eq 0 ]; then
    echo "Failed to complete artifact."
    echo $1 >> failed_repositories.txt
    echo "=================================================================================================================="
    exit
  fi
}

java -Xmx8g -jar "<jar_path of javaparser-dloc-3.5.14-SNAPSHOT-jar-with-dependencies.jar" $input $output
check_return

echo "=================================================================================================================="
