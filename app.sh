#!/bin/bash

if [ -z "$1" ]; then
    read -p "Task (test|build|run) : " TASK
else
    TASK=$1
fi

if [ -z "$2" ]; then
    read -p "Version : " VER
else
    VER=$2
fi

function build() {
    echo "Build docker image with version \"${VER}\""
	docker build -t ${IMAGE}:${VER}
    docker images
}

function run() {
    echo "Test docker image with version \"${VER}\""
    docker run --rm ${IMAGE}:${VER}
}

function push() {
    echo "Push docker image with version \"${VER}\""
    docker push ${IMAGE}:${VER}
    docker tag ${IMAGE}:${VER} ${IMAGE}:latest
    docker push ${IMAGE}:latest
}

case $TASK in
    build)
        build
    ;;
    run)
        test
    ;;
    push)
        push
    ;;
    *)
        echo "Invalid environment! Valid options: run, build, push"
    ;;
esac