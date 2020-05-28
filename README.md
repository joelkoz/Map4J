# Map4J
A tile based moving map rendering library for Java

This project is still a work in progress. See the "src/examples/java" directory for a working code example.

For a complete application framework that uses Map4J, see [Nav4J](https://github.com/joelkoz/Nav4J)

## Background

This project is heavily influenced by the original jmapviewer project from the OpenStreeMaps group. The tile
loading system was borrowed from that project, then refactored to support tile loading from offline sources.
In particular, Map4J uses MBTiles files as its primary source of data, allowing for offline map rendering.

## Project goals

This project is based on the following needs that the JMapViewer project could not satisfy as written:

1. Support offline tile sources with an emphasis on MBTiles files
2. Support for a "moving map" in the UI, including image rotation for "heading up" rendering
2. Separate map rendering code from Java Swing code as much as possible to allow for the
creation of map manipulation and rendering utilities independent of a GUI.
2. Simplify working with the multiple coordinate systems required by a map tile system by making 
discrete coordinate types to represent world space, pixel space, and tile space.
