{ pkgs ? import <nixpkgs> { } }:

with pkgs;

let
  android-nixpkgs = callPackage <android-nixpkgs> { };

  android-sdk = android-nixpkgs.sdk (sdkPkgs:
    with sdkPkgs; [
      cmdline-tools-latest
      build-tools-32-0-0
      platform-tools
      platforms-android-31
      emulator
    ]);

in mkShell { buildInputs = [ android-studio android-sdk ]; }
