#!/bin/bash

echo "=================================================================================================================="
echo "Going to process repo: $1"

output=$2
mkdir $output

package_name=$(cut -d':' -f2 <<< $1)
repository=$output"/"$package_name"/"

if [ -d "$repository" ]; then
  echo "Skipping: Directory exist:$repository"
  echo "=================================================================================================================="
  exit
fi

echo "Output path: $repository"
mkdir $repository

dependencies=$repository"/dependencies"
mkdir $dependencies

sources=$repository"/sources"
mkdir $sources

train_output=$repository"/output"
mkdir $train_output

check_return()
{
  if [ ! $? -eq 0 ]; then
    echo "Failed to download artifact."
    rm -rf $repository
    echo "=================================================================================================================="
    exit
  fi
}

mvn -Dartifact=$1":jar:sources" org.apache.maven.plugins:maven-dependency-plugin:2.4:get -Ddest=$repository$package_name".jar"
check_return

unzip -q $repository$package_name".jar" -d $sources
check_return

mvn -Dartifact=$1":pom" org.apache.maven.plugins:maven-dependency-plugin:2.4:get -Ddest=$repository"pom.xml"
check_return

mvn -f $repository"pom.xml" org.apache.maven.plugins:maven-dependency-plugin:3.0.2:copy-dependencies -DincludeScope=runtime -DoutputDirectory=$dependencies
check_return

echo "=================================================================================================================="