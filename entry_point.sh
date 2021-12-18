#!/bin/bash

CMD="./gradlew "
CMD+="$APP_NAME:"

APK_PATH=""
TEST_PATH=""

if [ "$DEFAULT_PROFILE" = true ]; then
    CMD+="defaultProfile "
fi

if [ "$CUSTOM_PROFILE" = true ]; then
    CMD+="customProfile "
fi

if [ "$PGRANULARITY" = "class" ]; then
    CMD+=" -Pgranularity=class "
    CMD+=$PGRANULARITY_VALUE
fi

if [ "$PGRANULARITY" = "method" ]; then
    CMD+=" -Pgranularity=method "
    CMD+=$PGRANULARITY_VALUE
fi

if [ "$CUSTOM_APP" = "class" ]; then
    CMD+=$APP_MODULE
fi

$CMD