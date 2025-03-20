{ pkgs ? import <nixpkgs> { } }:

with pkgs;

let
  android-nixpkgs = callPackage <android-nixpkgs> { };

  android-sdk = android-nixpkgs.sdk (sdkPkgs:
    with sdkPkgs; [
      cmdline-tools-latest
      build-tools-34-0-0
      platform-tools
      platforms-android-34
      emulator
      ktlint
    ]);

in mkShell {
  buildInputs = [ android-studio android-sdk zulu17 nushell ];
  shellHook = ''
    echo sdk.dir=$ANDROID_HOME > ./local.properties
    export GRADLE_USER_HOME=./.gradle_home
  '';
}
