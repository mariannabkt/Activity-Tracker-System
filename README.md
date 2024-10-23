# Activity Tracker System

## Overview
The **Activity Tracker System** is a multi-user mobile application and backend system designed for analyzing exercise data from GPX files. The system leverages a TCP-based Java server and a MapReduce framework to efficiently process user-uploaded activity data, providing key metrics like total distance, average speed, total elevation gain, and overall exercise time. Users can also compare their statistics with others through the Android application.

## Features
- **Mobile Application**: Allows users to upload GPX files and view detailed exercise statistics.
- **Backend System**: Processes user-uploaded GPX files using a multi-threaded, TCP-based Java server.
- **MapReduce Framework**: Efficiently analyzes large GPX files by splitting them into smaller chunks processed in parallel.
- **Real-Time Stats**: Users can track personal performance metrics and compare their results with average stats.
- **Leaderboards**: Users can track their performance on predefined route segments and view leaderboards for each segment.

## Technologies Used
- **Java**: For the backend server and multi-threaded processing of GPX files.
- **Android**: For the mobile interface where users can upload GPX files and view results.
- **MapReduce**: For parallel processing of large data files.
- **TCP Sockets**: For communication between the mobile app and the backend server.

## How It Works
1. The **mobile app** allows users to select a GPX file from their device and upload it to the backend system for processing.
2. The **backend system** is implemented as a TCP server that receives GPX files, splits them into chunks (waypoints), and processes each chunk using the MapReduce model.
3. The **Map** phase computes intermediate results for each chunk, including distance, speed, elevation, and time.
4. The **Reduce** phase combines intermediate results from all chunks to generate final statistics for the user’s activity.
5. The processed data is sent back to the mobile app for display, showing metrics such as total distance, average speed, total elevation gain, and total time.
