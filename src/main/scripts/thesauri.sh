#!/bin/bash

# ============================================
# Setup environemnt
# ============================================

# Halt on first error
set -e 

# Fail on unset variable
set -u


# ============================================
# Configuration variables
# ============================================


# INPUT_FILE="~/Data/enwiki-20130204-pages-articles.xml.bz2"
INPUT_FILE="/Volumes/LocalScratchHD/LocalHome/Data/random.xml"


OUTPUT_DIR="out"

BYBLO_PATH="byblo"

WAG_PATH="wag"

CHARSET="UTF-8"


function main {
	
	# Convert paths to 
	INPUT_FILE=`canonicalize ${INPUT_FILE}`
	OUTPUT_DIR=`canonicalize ${OUTPUT_DIR}`
	BYBLO_PATH=`canonicalize ${BYBLO_PATH}`
	WAG_PATH=`canonicalize ${WAG_PATH}`
	WORKING_DIR=`canonicalize "$(pwd - P)"`
	INPUT_FILE_NAME="$(basename -- "$INPUT_FILE")"
	
	# ============================================
	# Run Wikipedia Alias Generator
	# ============================================
	
	ALIASES_FILE="${OUTPUT_DIR%%/}/${INPUT_FILE_NAME}.aliases"
	
	if [ ! -f ${ALIASES_FILE} ]; then
		echo "Extracting aliases from ${INPUT_FILE} to ${ALIASES_FILE}"
		cd $WAG_PATH
		./wag.sh \
			--charset "${CHARSET}" \
			--identityAliases \
			--output "${ALIASES_FILE}" \
			--outputColumns SOURCE,TARGET \
			--outputFormat TSV \
			"${INPUT_FILE}"
		cd $WORKING_DIR
	else
		echo "Aliases file ${ALIASES_FILE} already exists; skipping."
	fi
	
	
	# ============================================
	# Split the source and target columns
	# ============================================

	SOURCE_FILE="${OUTPUT_DIR%%/}/${INPUT_FILE_NAME}.source"
	TARGET_FILE="${OUTPUT_DIR%%/}/${INPUT_FILE_NAME}.target"

	if [ ! -f ${SOURCE_FILE} ]; then
		echo "Creating source column file ${SOURCE_FILE}"
		cut -f 1 ${ALIASES_FILE} > ${SOURCE_FILE}
	else	
		echo "Source column file ${SOURCE_FILE} already exists; skipping."
	fi
	
	if [ ! -f ${TARGET_FILE} ]; then
		echo "Creating target column file ${TARGET_FILE}"
		cut -f 2 ${ALIASES_FILE} > ${TARGET_FILE}
	else	
		echo "Target column file ${TARGET_FILE} already exists; skipping."
	fi



	# ============================================
	# Produce just the source followed by the target
	# and the target followed by the source
	# ============================================

	SOURCE_TARGET_FILE="${OUTPUT_DIR%%/}/${INPUT_FILE_NAME}.source-target"
	TARGET_SOURCE_FILE="${OUTPUT_DIR%%/}/${INPUT_FILE_NAME}.target-source"

	if [ ! -f ${SOURCE_TARGET_FILE} ]; then
		echo "Creating source-target ordered file ${SOURCE_TARGET_FILE}"
		paste ${SOURCE_FILE} ${TARGET_FILE} > ${SOURCE_TARGET_FILE}
	else	
		echo "Source-target ordered file ${SOURCE_TARGET_FILE} already exists; skipping."
	fi


	if [ ! -f ${TARGET_SOURCE_FILE} ]; then
		echo "Creating target-source ordered file ${TARGET_SOURCE_FILE}"
		paste ${TARGET_FILE} ${SOURCE_FILE} > ${TARGET_SOURCE_FILE}
	else	
		echo "Target-source ordered file ${TARGET_SOURCE_FILE} already exists; skipping."
	fi


	# ============================================
	# Use Byblo to build frequency counts
	# ============================================

	cd $BYBLO_PATH
	
	counts "${SOURCE_TARGET_FILE}" "${OUTPUT_DIR}"

	counts "${TARGET_SOURCE_FILE}" "${OUTPUT_DIR}"

	cd $WORKING_DIR
	
}

function counts {
	
	INSTANCES_FILE="$1"
	OUTPUT_DIR="$2"
	OUTPUT_BASE="${OUTPUT_DIR%%/}/$(basename $INSTANCES_FILE)"
	
	FEATURE_INDEX_FILE="${OUTPUT_BASE}.feature-index"
	ENTRY_INDEX_FILE="${OUTPUT_BASE}.entry-index"
	
	ENTRY_COUNT_FILE="${OUTPUT_BASE}.entries"
	FEATURE_COUNT_FILE="${OUTPUT_BASE}.features"
	EVENT_COUNT_FILE="${OUTPUT_BASE}.events"
	
	echo "Creating frequency counts for source target ordered file"
	./byblo.sh --charset ${CHARSET} \
		-i ${INSTANCES_FILE} -o ${OUTPUT_DIR} 
		# \
		# 		-s enumerate,count
	
	echo "Unenumating Events"
	./tools.sh unindex-events \
		--charset ${CHARSET} \
		-Xe ${ENTRY_INDEX_FILE} -Xf ${FEATURE_INDEX_FILE} -et JDBM \
		-i ${EVENT_COUNT_FILE} -o ${EVENT_COUNT_FILE}.strings -Ef -Ee
		
	echo "Unenumating entires"
	./tools.sh unindex-entries \
		--charset ${CHARSET} \
	 	-Xe ${ENTRY_INDEX_FILE} -Xf ${FEATURE_INDEX_FILE} -et JDBM \
		-i ${ENTRY_COUNT_FILE} -o ${ENTRY_COUNT_FILE}.strings -Ef -Ee
		
	echo "Unenumating features"
	./tools.sh unindex-features \
		--charset ${CHARSET} \
	 	-Xe ${ENTRY_INDEX_FILE} -Xf ${FEATURE_INDEX_FILE} -et JDBM \
		-i ${FEATURE_COUNT_FILE} -o ${FEATURE_COUNT_FILE}.strings -Ef -Ee
		
}

function canonicalize {	
	TEMP_PWD="$(pwd -P)"
	if [ -d $1 ]; then
		cd -P -- "$1" &&
		echo "$(pwd -P)"	
	else
		cd -P -- "$(dirname -- "$1")" &&
		echo "$(pwd -P)/$(basename -- "$1")"
	fi
	cd ${TEMP_PWD}
}


main

