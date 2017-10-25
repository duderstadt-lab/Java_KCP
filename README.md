# Java_KCP
Java based implementation of the Kinetic Change Point algorithm as an ImageJ plugin.

Copy the jar file Java_KCPv1.0.jar into the plugins folder of imagej to install. The plugin can be accessed under the Plugins menu. 

Version 1.0 has been tested on Fuji (here you have to go inside the package to install) on MacOS High Sierra. It has been tested with ImageJ on MacOS Sierra and prior operating systems.

The plugin expects a table with x and y data. The region for changpoint analysis must be specified as X start and end. If X start is more than zero. Auto determination of sigma can be used with the region before X start.
