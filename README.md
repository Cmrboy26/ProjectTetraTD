# Project Tetra TD

<img src="https://img.itch.zone/aW1nLzE2NzkyMjA4LnBuZw==/315x250%23c/I2pzYe.png">

A simple, pixel-art-styled, endless tower defense game with online multiplayer support.

## Installation and Usage

To play the game, visit https://cmrboy26.itch.io/project-tetra-td to view the game page OR
look in the "Releases" tab on the right side of the screen to download your desired version.

To compile and modify the game yourself, download this project as a ZIP file, extract its contents, and view the Gradle setup below. 
This project requires Java 8 to be run and built.

<hr>

(Note: if running on Linux, prepend ``./`` to the beginning)

Initialize the Gradle environment: ``gradlew init``

Build game: ``gradlew desktop:dist`` <br>
Build dedicated server: ``gradlew desktop:dist-server`` <br>

Run game: ``gradlew desktop:run`` <br>
Run dedicated server: ``gradlew desktop:run-server`` <br>

### Tests

Run JUnit tests on Windows: ``gradlew tests:test`` <br>

## Personal Goals

In developing this game, I wanted to learn more about:
- Encryption
- Networking protocols
- Unit testing
- Finishing a project

So far, this project has unexpectedly taught me about:
- Separating axis theorem
- Swept AABB
- Minkowski sums
- Dot products and normals
- SQL databases (used to store player feedback)



