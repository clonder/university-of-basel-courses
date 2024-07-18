#!/usr/bin/env bash

crb/process-init alice
crb/process-init bob
cd alice

../crb/author-append alice '1st msg'

cd ../bob

git remote add alice ../alice

../crb/repo-fetch alice

echo "Merging"

../crb/repo-merge

git log 'refs/heads/alice' --graph --pretty=oneline
