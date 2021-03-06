#!/bin/bash

# Script that generates random wikipedia article titles.


# ============================================
# Setup environment
# ============================================

# Halt on first error
set -e 

# Fail on unset variable
set -u



# ============================================
# Configuration variables
# ============================================


NUM_PAGES=100


function _main_ {	
	for ((i=1 ; i <= ${NUM_PAGES}; i++)); do
		random_wiki_article
	done
}

function random_wiki_article {
	curl --head --silent http://en.wikipedia.org/wiki/Special:Random \
		| get_http_location \
		| urldecode
}

function get_http_location {
	grep -E "^Location: " | grep -oE "[^/]+$" 
}

function urldecode {
	echo -e `sed -e's/%\([0-9A-F][0-9A-F]\)/\\\\\x\1/g'`
}

_main_ "$@"