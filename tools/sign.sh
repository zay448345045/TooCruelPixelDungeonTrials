#!/usr/bin/env bash

set -e

if [[ -z "$KEYSTORE_BASE64" ]]; then
    echo "KEYSTORE_BASE64 is not set"
    exit 1
fi

if [[ -z "$KEYSTORE_PASSWORD" ]]; then
    echo "KEYSTORE_PASSWORD is not set"
    exit 1
fi

mkdir -p build

echo $KEYSTORE_BASE64 | base64 -d > build/release.keystore

$ANDROID_HOME/build-tools/34.0.0/zipalign -f -p 4 ./android/build/outputs/apk/release/android-release-unsigned.apk ./build/aligned.apk
$ANDROID_HOME/build-tools/34.0.0/zipalign -c 4 ./build/aligned.apk

$ANDROID_HOME/build-tools/34.0.0/apksigner sign --ks-key-alias tcpd --ks ./build/release.keystore --ks-pass env:KEYSTORE_PASSWORD ./build/aligned.apk
$ANDROID_HOME/build-tools/34.0.0/apksigner verify ./build/aligned.apk

rm ./build/release.keystore