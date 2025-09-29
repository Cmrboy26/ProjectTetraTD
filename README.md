# Project Tetra TD

<div style="display: block-inline">
  <img src="https://github.com/user-attachments/assets/c520383f-f099-493f-8072-aa1236f64759" height="200"/>
  <img src="https://img.itch.zone/aW1nLzE2NzkyMjA4LnBuZw==/315x250%23c/I2pzYe.png" height="200"/>
  <img src="https://github.com/user-attachments/assets/4950c130-fd13-46aa-a2e0-930f368855ac" height="200"/>
</div>

An endless tower defense game made in Java with <a href="https://github.com/libgdx/libgdx">libGDX</a>, supporting online multiplayer powered by <a href="https://github.com/EsotericSoftware/kryonet">KryoNet</a>.

[![Play on itch.io](https://img.shields.io/badge/Play%20Now-itch.io-orange?style=for-the-badge&logo=itch.io)](https://cmrboy26.itch.io/project-tetra-td)

## About PTTD

Project Tetra TD is a fast-paced tower defense game where players construct towers, extract resources to upgrade, and outlast their opponents. 
In online mode, players can either work together or compete to survive the longest through strategy, time management, and resource allocation.
The game is playable on both Windows and Android.   

### Features
- Build and upgrade 5 tower types with unique abilities
- Survive waves of enemies that grow stronger over time
- Complete quests and progress through the campaign
- Save and load progress
- Battle or cooperate with friends online
- Custom music tracks, audio, and retro-style graphics
- Upload feedback directly to the developer via RESTful API

### Technologies Used
- **Programming Language:** Java
- **Frameworks:** <a href="https://github.com/libgdx/libgdx">libGDX</a> (a game development framework), <a href="https://github.com/EsotericSoftware/kryonet">KryoNet</a> (a TCP/UDP client-server network library), Node.js, Express
- **Tools:** Gradle, Git, Aseprite, FL Studio, Audacity
- **Platforms:** Windows, Android

## Personal Goals

Going into this project, I intended to learn more about and implement:
- Encryption for sharing private keys
- Networking protocols to establish online connections
- Unit testing to ensure additional content was formatted correctly
- The process behind completing and publishing a project

Finishing this project has taught me everything above and, unexpectedly, these new concepts:
- Separating axis theorem
- Swept AABB
- Minkowski sums
- Vector concepts (dot products and normals)
- SQL databases, Node.js, and Express (used to store player feedback)

## Installation and Usage

To play the game, visit the game's <a href="https://cmrboy26.itch.io/project-tetra-td">itch.io</a> page 
OR look in the "Releases" tab on the right side of the screen to download your desired version.

To compile and modify the game yourself, download this project as a ZIP file, extract its contents, and view the Gradle setup below. 
This project requires Java 8 to run and build.

<hr>

(Note: if running on Linux, prepend ``./`` to the beginning)

Initialize the Gradle environment: ``gradlew init``

Build game: ``gradlew desktop:dist`` <br>
Build dedicated server: ``gradlew desktop:dist-server`` <br>

Run game: ``gradlew desktop:run`` <br>
Run dedicated server: ``gradlew desktop:run-server`` <br>

### Tests

Run JUnit tests on Windows: ``gradlew tests:test`` <br>



