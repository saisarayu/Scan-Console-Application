## Scan Queue Console Application

A Java console application that manages and executes scans sequentially in the background.

## Features
Add and view scans,
Start scans one at a time,
Pause after selected scans,
Stop and cancel running scans,
Remove pending scans,
Handle invalid commands.

## Technologies
Java
Collections
Multithreading
ExecutorService

## Run 
javac *.java,
java Main

## Commands
add:<id>, <name>, <duration>, <pause>,
view,
start,
stop,
remove:<id>,
exit.