# Audio Fingerprinting

This is an implementation of the Shazam song recognition algorithm inspired by the Shazam white paper.

## Description

The application has both a time-amplitude visualisation as well as a spectrogram view of audio data. The application allows you to fingerprint songs and then match unknown samples of audio data at a later time
to recognise what song the sample is coming from.

## Getting Started

### Dependencies

* The application stores fingerprints in a postgresql database (can be updated to whatever DB of choice, but configuration is required to connect to the database)
* Also requires a maven installation if you're running it outside an IDE like IntelliJ

### Installing

### Executing program

* Once the code is cloned, you can run build.bat from the terminal and it should run (provided you have maven installed)
* How to run the program
* Step-by-step bullets

## Authors

Knowledge Dzumba

## References
* [Shazam White paper](https://www.ee.columbia.edu/~dpwe/papers/Wang03-shazam.pdf)
* [Song Recognition Using Audio Fingerprinting](https://hajim.rochester.edu/ece/sites/zduan/teaching/ece472/projects/2019/AudioFingerprinting.pdf)
