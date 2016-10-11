# Changelog

## 0.2.0
* fix behavior of `latest` link for more than 10 builds (#6), thanks @felixb
* **security fixes**: Made sure that users can't break out of the directory that contains the artifacts (#7). 
  Also moved artifacts out of the lambdacd-home-dir to make it easier to distinguish between artifacts directories and other data.
  This is a **breaking change** since users can no longer access artifacts generated before upgrade. See the issue for details and a workaround.
  
## 0.1.1

* resolve paths with build number `latest` to last available artifact

## 0.1.0

First release
