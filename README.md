# RetroTowerDefense

A simple, pixel-art-styled, endless tower defense game with online multiplayer support.

## Learning Goals

In developing this game, I want to learn more about:
- encryption
- networking protocols
- unit testing

So far, this project has unexpectedly taught me about:
- separating axis theorem
- swept AABB
- Minkowski sums
- dot products and normals

## Minimal Viable Product

An MVP of this game contains:
- simple player movement
- collision handling
- enemies (with "path following")
- towers
    - attacking
    - player placing
    - player breaking
- tower shop
- waves
- lose condition

## Installation and Usage

To install, download this project as a ZIP file and extract its contents. 
This project requires Java 8 to be run and built.

<hr>

(Note: if running on Linux, prepend ``./`` to the beginning)

Build game: ``gradlew desktop:dist`` <br>
Build dedicated server: ``gradlew desktop:dist-server`` <br>

Run game: ``gradlew desktop:run`` <br>
Run dedicated server: ``gradlew desktop:run-server`` <br>

### Tests

Run JUnit tests on Windows: ``gradlew tests:test`` <br>

