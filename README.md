# Map4J
A tile based moving map rendering library for Java

This project is still a work in progress. See the "src/examples/java" directory for a working code example.

For a complete application framework that uses Map4J, see [Nav4J](https://github.com/joelkoz/Nav4J)

## Background

This project is heavily influenced by the original jmapviewer project from the OpenStreeMaps group. The tile
loading system was borrowed from that project, then refactored to support tile loading from offline sources.
In particular, Map4J uses MBTiles files as its primary source of data, allowing for offline map rendering.

## Features
1. Render map images from both online and offline sources
3. Use MBTiles file as an offline map source
1. Render map with an optionAL magnetic heading direction for "heading up" displays
1. Render maps independently from GUI
1. Separation of map image rendering and Swing GUI objects allows for code reuse in utilities
2. Direct support for reading/writing MBTiles files
3. Utility to save tile images from an online source to an MBTiles file for offline usage
4. Utility to generate new zoom levels in an MBTiles file (i.e. pre-compute tiles to reduce runtime processing)
2. Specialized coordinate objects to simplify working with the multiple coordinate systems required by a map tile 
   system (world space, pixel space, and tile space).
2. Robust publish/subscribe model to allow multiple sources to subscribe to map changes
