# Git-based Causal Broadcast

*Please read the entire README before starting the implementation, there are many useful suggestions written in the API  and Hints sections at the end. All Git commands that you need to implement this project are described in the 'Git from the ground up' slides from the previous Exercise 0 session.* 

Table of content:
  1. [Dependencies](#dependencies)
  2. [Installation](#installation)
  3. [Implementation Tutorial](#implementation-tutorial)
  4. [Properties](#properties)
  5. [Causal Broadcast Scenario](#causal-broadcast-scenario)
  6. [API](#api)
  7. [Hints](#hints)

## Dependencies

Requires [Git](https://git-scm.com/) to be installed and in the `PATH` environment variable. Executing `which git` on Linux or MacOS on the commandline should return a non-empty path to the installation directory (ex: `/usr/local/bin/git`). Windows users may try the [Windows Subsystem for Linux (WSL)](https://learn.microsoft.com/en-us/windows/wsl/install) but we have not tested it.

## Installation

Download the latest version:
````
    git clone https://git.scicore.unibas.ch/cn/CN-Public/FDS-2023/git-cb-template.git
````

Add the script to the executables accessible by default in the environment in every terminal you use:
````
    cd git-cb-template
    export PATH=$PATH:$(pwd)
````
You may want to modify `.bash_rc` to automatically do this when starting your
terminal session, otherwise repeat the last two lines in every terminal you
open to test your code. The later examples all assume this was done so every
command is accessible from `crb/COMMAND`.

Note that the path to your `git-cb-template` repository must not have spaces otherwise 
the path resolution will not work.

## Implementation Tutorial

To make your life easier, we suggest you implement the API commands from lower level to higher level in the following order:
  1. [`author-append`](#1-implement-author-append-see-api)
  2. [`repo-push`](#2-implement-crbrepo-push-see-api)
  3. [`repo-fetch`](#3-implement-crbrepo-fetch-see-api)
  4. [`repo-merge`](#4-implement-crbrepo-merge-see-api)
  5. [`repo-deliver`](#5-implement-crbrepo-deliver-see-api)

Once you have implemented (correctly!) and tested them using the instructions below, `crb/broadcast` and `crb/deliver` should work correctly. You will then be ready to test the [Causal Broadcast Scenario](#causal-broadcast-scenario).

*The following commands assume that the `causal-broadcast` directory is within
your `PATH` environment variable, following the installation instructions so that all commands under `causal-broadcast/crb` can be accessed with `crb/COMMAND`.*

### 1. Implement `author-append` (see [API](#crbauthor-append-author-msg-src))

Relevant commands are provided in the slides for 'Commit' and 'Reference'.

#### Test

Create a new repo and change to this directory:
````
    crb/repo-init alice
    cd alice
````

Initialize two authors:
````
    crb/author-init alice
    crb/author-init bob
````

Append new messages:
````
    crb/author-append alice '1st msg'
    crb/author-append bob '1st msg'
    crb/author-append alice '2nd msg'
````

Print the commit history for alice:
````
    git log 'refs/heads/alice' --graph --pretty=oneline
````

You should obtain a graph similar to the following but with different hashes:
````
*   15e8b53313887787518963d55451d5c91e0a46f5 (alice) 2nd msg
|\
| * 21399c8bea1d951979b21e325d33adccf2d89651 (bob) 1st msg
|/|
* | aff657b3c8dbc59330063d830fc4d1580f6c2b1d 1st msg
|\|
| * 3497eced5af412ff8f41c82185f3bfed31482e6f Author 'bob' initialized
* 631e77729fcdafa1e6183f290c3e3f6d9fe3c55e Author 'alice' initialized
````

### 2. Implement `crb/repo-push` (see [API](#crbrepo-push-origin-src))

Relevant commands are provided under the 'Push (branch)' slide.

#### Test

Create two new processes and change to the first one (alice is `author-init`ed implicitly by `process-init`):
````
    crb/process-init alice
    crb/process-init bob
    cd alice
````

Add `bob`'s repository to the remotes of `alice`:
````
    git remote add bob ../bob
````

Append a new message for `alice`:
````
    crb/author-append alice '1st msg'
````

Push `alice`'s messages to `bob`:
````
    crb/repo-push bob
````

Print all messages in `bob` repository:
````
    cd ../bob
    git log 'refs/remotes/alice/alice' --graph --pretty=oneline
````

This should output (with different hashes):
````
* 0cf25da1ede0767e9a7ab679ca1a79dc5bcfe8bf (alice/alice) 1st msg
* 586b558295c6c6c9823fd561eb11ef8145aeb513 Author 'alice' initialized
````

### 3. Implement `crb/repo-fetch` (see [API](#crbrepo-fetch-origin-src))

Relevant commands are provided under the 'Fetch (branch)' slide.

#### Test

Create two new processes and change to the first one (alice is `author-init`ed implicitly by `process-init`):
````
    crb/process-init alice
    crb/process-init bob
    cd alice
````

Append a new message for `alice`:
````
    crb/author-append alice '1st msg'
````

Add `alice`'s repository to the remotes of `bob`:
````
    cd ../bob
    git remote add alice ../alice
````

Pull `alice`'s messages from `bob`:
````
    crb/repo-fetch alice
````

Print all messages in `bob` repository:
````
    git log 'refs/remotes/alice/alice' --graph --pretty=oneline
````

This should output (with different hashes):
````
* 4f7f9c01de86975b40fa5df47a2cb55b10e43f1f (alice/alice) 1st msg
* fa7966ab35256c561e48501ab5f59e0d4163f59e Author 'alice' initialized
````

### 4. Implement `crb/repo-merge` (see [API](#crbrepo-merge-src))

Relevant commands are provided under the 'Is Ancestor' and 'Reference' slides. Do not use the 'git pull' command to implement `crb/repo-merge` because it will automatically create a `main` branch that will break the [assumptions](#assumptions).

#### Test

Create two new processes and change to the first one:
````
    crb/process-init alice
    crb/process-init bob
    cd alice
````

Append a new message for `alice`:
````
    crb/author-append alice '1st msg'
````

Add `alice`'s repository to the remotes of `bob`:
````
    cd ../bob
    git remote add alice ../alice
````

Fetch `alice`'s messages from `bob`:
````
    crb/repo-fetch alice
````

Merge all messages from remote repositories:
````
    crb/repo-merge
````

Print all messages in `bob` repository:
````
    cd ../bob
    git log 'refs/heads/alice' --graph --pretty=oneline
````

This should output (with different hashes):
````
* 4f7f9c01de86975b40fa5df47a2cb55b10e43f1f (alice/alice,alice) 1st msg
* fa7966ab35256c561e48501ab5f59e0d4163f59e Author 'alice' initialized
````
Notice that `alice` is now listed in `(alice/alice, alice)` before `1st msg`.
Executing `repo-merge` again should not change anything.

### 5. Implement `crb/repo-deliver` (see [API](#crbrepo-deliver-src))

Relevant commands are provided under the 'Reference' and 'Commit History' slides. 

Note that you cannot update all references at once (in contrast to when you use `git fetch` or `git push`), you have to manually update every reference for each author. 

#### Test

Create two new processes and change to the first one:
````
    crb/process-init alice
    crb/process-init bob
    cd alice
````

Append a new message for `alice`:
````
    crb/author-append alice 'alice 1'
````

Add `alice`'s repository to the remotes of `bob`:
````
    cd ../bob
    git remote add alice ../alice
````

Fetch `alice`'s messages from `bob`:
````
    crb/repo-fetch alice
````

Merge all messages from remote repositories:
````
    crb/repo-merge
````

Append a new message for `bob`:
````
    crb/author-append bob 'bob 1'
````

Deliver all messages:

````
    crb/repo-deliver
````

This should print the followings (your hashes will be different):
````
a59558211383588dab89cb93e0bce052d49e097e
8fd71550389a72b4f681ffee4f34cb29a6fbf8a5
54634777e69a84cce69b436c72f6a7e126dd7c69
d3ac9286f8c94f4adde462c30c3b2f602a0cada8
````

Your hashes should be consistent with the following history printed by `crb/repo-monitor`:
````
*   d3ac928 (bob) bob 1   (bob, refs/delivered/bob)
|\
| * 5463477 (bob) Author 'bob' initialized
* 8fd7155 (alice) alice 1   (alice/alice, alice, refs/delivered/alice)
* a595582 (alice) Author 'alice' initialized
````

Notice that the `bob 1` message was printed last and that `refs/delivered/bob` points to that 'bob 1' message after delivery.

## Properties

This Git-based reliable causal broadcast abstraction implements the
corresponding abstraction described in ["Introduction to Reliable and Secure
Distributed Programming"](https://distributedprogramming.net/) (Chapter 3) with
the following properties:

  1. *validity*: If a correct process *p* broadcasts a message *m*, then *p* eventually delivers *m*. 
  2. *no duplication*: No message is delivered more than once.
  3. *no creation*: If a process delivers a message *m* with sender *s*, then *m* was previously broadcast by process *s*.
  4. *agreement*: If a message *m* is delivered by some correct process, then *m* is eventually delivered by every correct process.
  5. *causal delivery*: For any message *m_1* that potentially caused a message *m_2*, i.e. *m_1 -> m_2*, no process delivers *m_2* unless it has already delivered *m_1*.

With the only difference that messages are delivered when explicitly requested
(by invoking `crb/deliver`) instead of automatically as soon as they are
available.


## Causal Broadcast Scenario

In this section we provide an interaction scenario between three processes that
illustrates both the behaviour of the implementation as well as the properties
of the abstraction it implements.

### Initialization

Create three processes, one for each author `alice`, `bob`, and `carol`:
````
    crb/process-init alice
    crb/process-init bob
    crb/process-init carol
````

Establish the following partial topology in which:
 1. `alice` may push or pull messages from `bob` and `carol`.
 2. `bob` may push or pull messages only from `carol`.
 3. `carol` may push or pull messages only from `alice`. 
````
    cd alice
    git remote add bob ../bob
    git remote add carol ../carol
    cd ../bob
    git remote add carol ../carol
    cd ../carol
    git remote add alice ../alice
````

Check the current state of `alice`'s repository:
````
    cd ../alice
    crb/repo-monitor
````

This should display the following (with a different hash) and refresh every 3 seconds:
````
* c7ecc1f (alice) Author 'alice' initialized   (alice)
````

The previous line is composed of:
  1. `c7ecc1f`: the short hash of the message (encoded as a git commit)
  2. `(alice)`: the author of the message
  3. `Author 'alice' initialized`: the content of the message
  4. 2nd `(alice)`: the branches that point to this commit, in this case a single local branch named after alice.

As new messages are locally recreated and replicated from other processes, more messages will be displayed. You can interrupt the display with `CTRL-C` at any time to return to the
terminal's interactive prompt. You may want to use `crb/repo-monitor` in a dedicated
terminal window to continuously display the latest state and avoid retyping the command
all the time.

### Broadcasting

Broadcast one new message from `alice`:
````
    crb/broadcast 'alice 1'
````

`crb/repo-monitor` within `alice` should now display:
````
* 4e2e9cc (alice) alice 1   (alice)
* c7ecc1f (alice) Author 'alice' initialized
````
`crb/repo-monitor` within `bob` should now display:
````
* 4e2e9cc (alice) alice 1   (alice/alice)
* c7ecc1f (alice) Author 'alice' initialized
* cae004f (bob) Author 'bob' initialized   (bob)
````

In contrast to the local branch on `alice`'s repository, alice's messages are stored in the `alice/alice` branch in `bob`'s repository, which reads as "alice's branch obtained from alice's repository", i.e. the `alice/` prefix represents alice's repository while the following `alice` represents the local `alice` branch that was obtained from it.

`crb/repo-monitor` within `carol` should now display:
````
* 4e2e9cc (alice) alice 1   (alice/alice)
* c7ecc1f (alice) Author 'alice' initialized
* 339f6c8 (carol) Author 'carol' initialized   (carol)
````

### Delivering

Deliver `alice`'s new messages from within `alice`'s repository: 
```
    crb/deliver
```
This should print two new message hashes (one per line) that should be  in reverse (causal order), the hash of the initialization message being on the first line and the hash of the "alice 1" message on the second line. The hashes should be consistent with the hashes shown by `crb/repo-monitor` within `alice`, albeit with the former being in long form and the latter being in short form and in reversed order:
````
* 4e2e9cc (alice) alice 1   (alice, refs/delivered/alice)
* c7ecc1f (alice) Author 'alice' initialized
````
Notice also that after delivering the messages, a new reference `refs/delivered/alice` keeps track of what messages were delivered last to only deliver them once (`no duplication` property).

The repository state for `bob` should not have changed. Now deliver bob
messages as follows, adding pretty printing of the sender and content using `xargs`:
````
    crb/deliver | xargs -n1 git show --pretty=oneline
````

This should print the followings (with different hashes) and return immediately:
````
cae004f1c4c623d36bc32b0fb6710188bf6455a4 (bob) Author 'bob' initialized
c7ecc1f5532b585e28db34e4acf56be22264a7d6 Author 'alice' initialized
4e2e9ccb5150e0928aa7ce7d1c903fffc2683499 (alice/alice, alice) alice 1
````

`crb/repo-monitor` within `bob` should now display updated references `refs/delivered/alice` and `refs/delivered/bob` that correspond to all messages from `bob` and `alice` that have been delivered:
````
* 4e2e9cc (alice) alice 1   (alice/alice, alice, refs/delivered/alice)
* c7ecc1f (alice) Author 'alice' initialized
* cae004f (bob) Author 'bob' initialized   (bob, refs/delivered/bob)
````

Delivering additional times from within `bob` should not print additional
messages and not change the state of the repository:
````
    crb/deliver
````

### Causality

Broadcast one message from `bob`:
````
    crb/broadcast "bob 1"    
````

`crb/repo-monitor` within `bob` should now display that the message "bob 1" is
the most recent previous messages from `bob` and has `alice`'s messages as parents but has not
been delivered yet:
````
*   0ff4428 (bob) bob 1   (bob)
|\
| * cae004f (bob) Author 'bob' initialized   (refs/delivered/bob)
* 4e2e9cc (alice) alice 1   (alice/alice, alice, refs/delivered/alice)
* c7ecc1f (alice) Author 'alice' initialized
````

`crb/repo-monitor` within `carol` should now display the same messages (plus
that of carol) with none delivered yet:
````
*   0ff4428 (bob) bob 1   (bob/bob)
|\
| * cae004f (bob) Author 'bob' initialized
* 4e2e9cc (alice) alice 1   (bob/alice, alice/alice)
* c7ecc1f (alice) Author 'alice' initialized
* 339f6c8 (carol) Author 'carol' initialized   (carol)
````

### Fetch messages

Fetch messages from `carol` into `alice`:
````
    cd ../alice
    crb/repo-fetch carol
````
should only add `carol` initialization message but not retrieve any of bob
messages because they have not been merged (which should happen when `carol` delivers messages). You should therefore see the followings when executing `crb/repo-monitor` (`carol`'s message could also be first instead of third, that is also correct):
````
* 4e2e9cc (alice) alice 1   (alice, refs/delivered/alice)
* c7ecc1f (alice) Author 'alice' initialized
* 339f6c8 (carol) Author 'carol' initialized   (carol/carol)
````

Delivering messages  within `carol`:
````
    crb/deliver
````
should then print all 5 messages in causal order and update `refs/delivered`
references for `alice`, `bob`, and `carol`, resulting in the following
repository state on `carol` with `crb/repo-monitor`:
````
*   0ff4428 (bob) bob 1   (bob/bob, bob, refs/delivered/bob)
|\
| * cae004f (bob) Author 'bob' initialized
* 4e2e9cc (alice) alice 1   (bob/alice, alice/alice, alice, refs/delivered/alice)
* c7ecc1f (alice) Author 'alice' initialized
* 339f6c8 (carol) Author 'carol' initialized   (carol, refs/delivered/carol)
````

Fetch messages from `carol` into `alice` and delivering the messages:
````
    cd ../alice
    crb/repo-fetch carol
    crb/deliver
````
should result in the following state on `alice` with `crb/repo-monitor`:
````
*   0ff4428 (bob) bob 1   (carol/bob, bob, refs/delivered/bob)
|\
| * cae004f (bob) Author 'bob' initialized
* 4e2e9cc (alice) alice 1   (carol/alice, alice, refs/delivered/alice)
* c7ecc1f (alice) Author 'alice' initialized
* 339f6c8 (carol) Author 'carol' initialized   (carol/carol, carol, refs/delivered/carol)
````

### Eventual agreement (consistency)

Pushing messages from `alice` to `bob` and delivering the messages:
````
    crb/repo-push bob
    cd ../bob
    crb/deliver
````
should result in consistent local branches `alice`, `bob`, `carol` and delivered messages on `bob` (as well as on `alice` and `carol`) with `crb/repo-monitor`:
````
*   0ff4428 (bob) bob 1   (alice/bob, bob, refs/delivered/bob)
|\
| * cae004f (bob) Author 'bob' initialized
* 4e2e9cc (alice) alice 1   (alice/alice, alice, refs/delivered/alice)
* c7ecc1f (alice) Author 'alice' initialized
* 339f6c8 (carol) Author 'carol' initialized   (alice/carol, carol, refs/delivered/carol)
````

## API

The API is presented by concepts, from the high-level abstraction interface of Causal Reliable Broadcast (CRB) to suggested implementation commands. Commands that are already implemented for you are marked with *provided*, those you must implement are marked with *TBD*.

### Process

Implements a process abstraction with persistent storage using a Git repository and communication with Git replication protocols.

#### `crb/process-init NAME` [(src)](./crb/process-init)
*provided*

Create a new repository with `NAME` and an author with `NAME` within the
repository.

### Causal Reliable Broadcast (CRB)

Implements the reliable broadcast with the added causal ordering property. The 'happens-before' relationship is defined as follows, `m_1 happens before m_2` if and only if:
    
  1. `m_1` was `crb-broadcast` before `m_2` from the same process.
  2. `m_1` was `crb-deliver`ed before `m_2` was `crb-broadcast` from the same process. 
  3. there exists `m_3` such that `m_1 -> m_3` and `m_3 -> m_2`.

Can only be executed within a valid repository created with `process-init`.

#### `crb/broadcast MSG` [(src)](./crb/broadcast)
*provided, but depends on other TBD api calls below*

Append `MSG` after the process's latest messages with `crb/author-append`, and push `MSG` and its ancestry to
all known remote repositories with `crb/repo-push`.

#### `crb/deliver [INTERVAL]` [(src)](./crb/deliver)
*provided, but depends on other TBD api calls below*

Poll the local repository every `INTERVAL` (in seconds), merging remote messages 
with `crb/repo-merge` then print the newer message hashes in causal order on 
the standard output with `crb/repo-deliver`.  If `INTERVAL` is not set, only 
deliver once all messages created or received since the last call to `crb/deliver`.

### Author

Identity of the process creating messages. Implemented as a branch name within a Git repository (process).

#### `crb/author-init AUTHOR` [(src)](./crb/author-init)
*provided*

Initialize author named `AUTHOR` with a first message and associated branch name.  `AUTHOR` must not contain the `/` character.

#### `crb/author-append AUTHOR MSG` [(src)](./crb/author-append)
*TBD*

Create a new commit with the following fields:
  1. commit message `MSG`;
  2. author and committer name `AUTHOR`;
  3. parents equal to the latest commits of all other authors in local branches (under `refs/heads/*`). 
  
Then update reference `refs/heads/$AUTHOR` to this new commit.
 
### Repository

#### `crb/repo-deliver` [(src)](./crb/repo-deliver)
*TBD*

Print all messages from all authors under `refs/heads/*` newer than those under `refs/delivered/*` in causal order, then update `refs/delivered/*` to reference the same messages as `refs/heads/*`.

#### `crb/repo-fetch ORIGIN` [(src)](./crb/repo-fetch)
*TBD*

Replicate all branches under `refs/heads/*` in remote repository at `ORIGIN` and store them under `remotes/ORIGIN/*`. `ORIGIN` must be a valid remote repository (see `git remote --help`).

#### `crb/repo-frontier` [(src)](./crb/repo-frontier)
*provided*

Return all commit references from both `ref/heads/*` and `refs/remotes/*`.

#### `crb/repo-init REPOPATH [NAME]` [(src)](./crb/repo-init)
*provided*

Initialize a new repository at `REPOPATH` with name `NAME`. If
`NAME` is not specified, use `REPOPATH`. Store the name under
`$REPOPATH/.git/git-cb`.  `NAME` must not contain the `/` character.


#### `crb/repo-merge` [(src)](./crb/repo-merge)
*TBD*

For each remote branch `refs/remotes/ORIGIN/AUTHOR`, if the local branch `refs/heads/AUTHOR` does not exist yet, create it. Otherwise, if `refs/remotes/ORIGIN/AUTHOR` contains newer messages than `refs/heads/AUTHOR`, update `refs/heads/AUTHOR` to the newest commit.

#### `crb/repo-monitor INTERVAL` [(src)](./crb/repo-monitor)
*provided*

Clear terminal and print the state of the repository every `INTERVAL` seconds. `INTERVAL` defaults to 3 seconds.

#### `crb/repo-push ORIGIN` [(src)](./crb/repo-push)
*TBD*

Replicate all local branches under `refs/heads/*` to remote repository `ORIGIN` under branch names
`remotes/LOCAL/*`, where `LOCAL` is the name of the local repository (stored
under `.git/git-cb` of the local repository).  `ORIGIN` must be a valid remote repository (see
`git remote --help`).

### Assumptions

The implementation of the previous API is based on the following assumptions:

  1. Messages from all processes are stored as Git commits and the current process state is encoded as Git references within a Git repository.
  2. Each process is uniquely associated to a single Git repository.
  3. Each process uses a unique branch name to represent their sequence of messages, which we hereafter refer to as the process `NAME`.
  4. Each process' repository uses a (unique) name that is the same as the process `NAME` and stored in `.git/git-crb`.
  5. A process `p` always broadcasts from their specific repository, so this repository always has the latest message `p` previously broadcast.
  6. All processes faithfully follow the protocol, i.e. they only interact with others through the API mentioned below using the latest version of the code, but may take an arbitrarily long but finite time to deliver messages. This is implemented by requiring an explicit call to `crb/deliver` to deliver messages.
  7. Each branch `refs/heads/BRANCHNAME` is only used for maintaining the state of either the latest message a process has broadcast (if `BRANCHNAME=NAME`) or the latest message they have merged from other processes (if `BRANCHNAME!=NAME`).
  8. Each branch `refs/remotes/ORIGIN/NAME` is only used to reference (possibly unmerged) messages broadcast by remote process `NAME` obtained from remote process `ORIGIN` (`ORIGIN` may not be equal to `NAME`).
  9. Each branch `refs/delivered/NAME` is only used to reference the latest message delivered by the current process that was previously broadcast by process `NAME`.

Breaking any of these assumptions will result in undefined behaviour.

## Hints

### Bash Programming

#### Variable replacement within strings

String syntax: `'$VAR'` literally prints `'$VAR'` while `"$VAR"` replaces `$VAR` by its value (`''` if undefined).

#### String Append

Append a string at the end of an existing string: `STR='1';STR=$STR'2'`

#### Extract directory and file name from path

You can use `dirname` and `basename`:
````
    > dirname foo/bar/baz
    foo/bar
    > basename foo/bar/baz
    baz
````

### Git 

#### Git push

You can specify which local branch should be pushed to which remote branch with the `:` syntax: `git push ORIGIN '<local_branch>:<remote_branch>'`. Moreover, you can use a `prefix/*` to push all branches under `prefix/`.
