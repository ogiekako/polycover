#!/bin/bash
# pretty print unsolved problems.
find problem | grep "\.no" | while read f; do
    echo $f
    cat $f | sed 's/\./□/g' | sed 's/#/■/g'
done
