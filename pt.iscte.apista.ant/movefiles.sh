#!/bin/bash

while read p;
do
	mv "$p" /c/git/apista/Resources/Repos/SWTRepos
done < results.txt