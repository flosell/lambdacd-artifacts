#!/bin/bash 
set -e
SCRIPT_DIR=$(dirname "$0")

test() {
  lein test
}

push() {
  test && git push
}

publishReleaseNotes() {
    cd ${SCRIPT_DIR}

    VERSION=$(chag latest)
    CHANGELOG=$(chag contents)
    USER="flosell"
    REPO="lambdacd-artifacts"

    echo "Publishing Release to GitHub: "
    echo "Version ${VERSION}"
    echo "${CHANGELOG}"
    echo

    github-release release \
        --user ${USER} \
        --repo ${REPO} \
        --tag ${VERSION} \
        --name ${VERSION} \
        --description "${CHANGELOG}"

    echo "Published release"
}

release() {
  test && lein release && publishReleaseNotes
}

serve() {
  lein run
}

if [ $# -ne 0 ] && type $1 &>/dev/null; then
    $1 $2
else
    echo "usage: $0 <goal>

goal:
    test     -- run unit tests
    push     -- run all tests and push current state
    serve    -- run the sample pipeline
    release  -- release the library to clojars"

    exit 1
fi
