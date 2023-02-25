#!/bin/bash

function send_notification() {
    echo "$1"
}

function assert_success() {
    "${@}"
    local status=${?}
    if [ ${status} -ne 0 ]; then
        send_notification "### Error ${status} at: ${BASH_LINENO[*]} ###"
        exit ${status}
    fi
}

app=tasks_bot
version="`grep ../gradle.properties -e "^docker_version=" | grep -e "[0-9.]*" -o`"
server_prefix=insanusmokrassar/

assert_success ../gradlew build
assert_success sudo docker build -t $app:"$version" .
assert_success sudo docker tag $app:"$version" $server_prefix$app:$version
assert_success sudo docker tag $app:"$version" $server_prefix$app:latest
assert_success sudo docker push $server_prefix$app:$version
assert_success sudo docker push $server_prefix$app:latest
