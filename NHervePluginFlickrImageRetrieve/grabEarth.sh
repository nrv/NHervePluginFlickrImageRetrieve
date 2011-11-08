#!/bin/sh
#
# Launch FlickrGrabAroundEarth
#

JAVA_HOME=/mnt/net/srv02/data_for_tests/nherve_dir/java/jdk1.7.0
#JAVA_HOME=/mnt/net/srv13/nherve/java/jdk1.7.0


JDK=$JAVA_HOME/bin/java

CP='classes:lib/*'

$JDK -version

$JDK -Xmx2000m -XX:CompileCommand=exclude,icy/image/IcyBufferedImage,createFrom -cp $CP plugins.nherve.flickr.grab.FlickrGrabAroundEarth $1 $2 $3 $4 $5 $6 $7 $8 $9


