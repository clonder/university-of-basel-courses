#!/usr/bin/env bash

crb/process-init alice
crb/process-init bob
cd alice

git remote add bob ../bob

../crb/author-append alice '1st msg'

../crb/repo-push bob

cd ../bob

git log 'refs/remotes/alice/alice' --graph --pretty=oneline
