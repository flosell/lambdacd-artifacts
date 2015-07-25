#!/bin/bash 
set -e

test() {
  lein test
}


push() {
  test && git push
}

release() {
  lein release "$1"
}

serve() {
  lein run
}

if type $1 &>/dev/null; then
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
