#!/usr/bin/env bash

crb/process-init alice
crb/process-init bob
cd alice

../crb/author-append alice '1st msg'

cd ../bob

git remote add alice ../alice

../crb/repo-fetch alice

git log 'refs/remotes/alice/alice' --graph --pretty=oneline
