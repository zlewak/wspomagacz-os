#!/bin/bash


function get_input()
{
    # Project-specific params
    SVN= #s
    GIT= #g
    TRAC= #t
    PUBLIC= #p
    NAME= #n
    PROJECT_OWNER= #o
    DESCRIPTION= #d
    USER= #u

    # Global params
    TEMPLATES_DIR= #T
    TRAC_DIR= #R
    GIT_REPO_DIR= #G
    SVN_REPO_DIR= #S
    SVN_ACCESS_CONTROL_FILE= #C
    PROJECTS_ARCHIVE_DIR= #A

    while getopts ":sgtpn:d:u:o:T:R:G:S:C:A:" optname ; do
        echo "Opcja : $optname arg: $OPTARG" > /dev/stderr
        case "$optname" in
            "s")
                SVN=1
                ;;
            "g")
                GIT=1
                ;;
            "t")
                TRAC=1
                ;;
            "p")
                PUBLIC=1
                ;;
            "n")
                NAME="$OPTARG"
                ;;
            "d")
                DESCRIPTION="$OPTARG"
                ;;
            "u")
                USER="$OPTARG"
                ;;    
            "o")
                PROJECT_OWNER="$OPTARG"
                ;;
            "T")
                TEMPLATES_DIR="$OPTARG"
                ;;
            "R")
                TRAC_DIR="$OPTARG"
                ;;
            "G")
                GIT_REPO_DIR="$OPTARG"
                
                ;;
            "S")
                SVN_REPO_DIR="$OPTARG"
                ;;
            "C")
                SVN_ACCESS_CONTROL_FILE="$OPTARG"
                ;;
            "A")
                PROJECTS_ARCHIVE_DIR="$OPTARG"
                ;;
            *)
                echo "Błąd: Nieznana opcja $OPTARG" > /dev/stderr
                exit 1
                ;;
        esac
    done
}