#!/usr/bin/env bash

crb/process-init alice
crb/process-init bob
cd alice

../crb/author-append alice 'alice 1'

cd ../bob
git remote add alice ../alice


../crb/repo-fetch alice

../crb/repo-merge

../crb/author-append bob 'bob 1'

../crb/repo-deliver
