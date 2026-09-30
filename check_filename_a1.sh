#!/usr/bin/env bash

echo "Ensure the filename checker is in the same directory as your assignment files."

read -r -p "Enter your CCID: " ccid

expected_filename="${ccid}-RapidRecall.zip"

# Get directory where this script is located
script_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

if [ -f "$script_dir/$expected_filename" ]; then
    echo "Correct filename for submission found: $expected_filename"
    echo "Your zip filename is correct for submission."
    result=0
else
    echo "Filename not found. Ensure the appropriate filename was used."
    echo ""
    echo "Expected filename: $expected_filename"
    echo "Looking in: $script_dir"
    result=1
fi

echo "Ensure all your filenames are correctly formatted within the .zip file before submitting your assignment."
read -p "Press Enter to exit..."
exit $result
