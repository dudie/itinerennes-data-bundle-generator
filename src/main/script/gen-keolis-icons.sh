#!/bin/bash

###
# [license]
# Itinerennes data resources generator
# ----
# Copyright (C) 2013 - 2014 Dudie
# ----
# This program is free software: you can redistribute it and/or modify
# it under the terms of the GNU General Public License as
# published by the Free Software Foundation, either version 3 of the 
# License, or (at your option) any later version.
# 
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
# 
# You should have received a copy of the GNU General Public 
# License along with this program.  If not, see
# <http://www.gnu.org/licenses/gpl-3.0.html>.
# [/license]
###

function usage() {
 echo "Usage: $0 <original_icons_dir> <output_dir>"
}

which mogrify || echo "Missing imagemagik" || exit 1
test -d $1 || usage || exit 2
test -d $2 || usage || exit 3

export TARGET=$2

find $1 -iname '*.png' -exec sh -c 'cp {} $TARGET/z_ic_line_`basename {} | sed "s/^\.\///g" | sed s/^L//g | sed "s/^0*//g"` ' \;
find $2 -iname 'z_ic_line_*.png' -exec mogrify -resize "72x72" {} \;
