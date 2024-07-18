#!/usr/bin/env bash

crb/repo-init alice
cd alice


../crb/author-init alice
../crb/author-init bob

../crb/author-append alice '1st msg'
../crb/author-append bob '1st msg'
../crb/author-append alice '2nd msg'

git log 'refs/heads/alice' --graph --pretty=oneline

