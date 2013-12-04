#!/bin/bash

function usage() {
 echo "Usage: $0 <original_icons_dir> <output_dir>"
}

which mogrify || echo "Missing imagemagik" || exit 1
test -d $1 || usage || exit 2
test -d $2 || usage || exit 3

export TARGET=$2

find $1 -iname '*.png' -exec sh -c 'cp {} $TARGET/z_ic_line_`basename {} | sed "s/^\.\///g" | sed s/^L//g | sed "s/^0*//g"` ' \;
find $2 -iname 'z_ic_line_*.png' -exec mogrify -resize "72x72" {} \;
