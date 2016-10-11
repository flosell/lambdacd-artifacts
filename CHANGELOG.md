# Changelog

## 0.2.0
* fix behavior of `latest` link for more than 10 builds (#6), thanks @felixb
* **security fix**: moved artifacts out of the lambdacd-home-dir to prevent users accessing other things in the home dir (#TODO).
  This is a **breaking change** since users can no longer access artifacts generated before upgrade. See the issue for details and a workaround.

## 0.1.1

* resolve paths with build number `latest` to last available artifact

## 0.1.0

First release
