# Minesweeper (Minecraft Edition)

A fresh, blocky twist on the classic Minesweeper game built in Java Swing. Instead of a traditional flat square grid, you play on a hexagonal grid styled with isometric Minecraft-inspired voxel blocks!

---

## What Makes It Different?

- **Hexagonal Gameplay**: Every tile has 6 neighbors instead of the usual 8, changing how you deduce safe cells and spot danger patterns.
- **Isometric Minecraft Visuals:**
  - **Unmined tiles:** Lush **Grass blocks** with textured dirt and pixel speckling.
  - 🪨 **Revealed safe tiles:** Smooth **Stone blocks** displaying neighbor mine counts (1–6).
  - **Flagged tiles:** Bright **Red Wool blocks** with cross-hatch weaving.
  - **Mines:** Classic **TNT blocks** complete with dynamite fuses and pixelated lettering.
- **Fair First Click:** Mines are distributed only after your first click, guaranteeing that your starting tile and its immediate neighbors are always safe.
- **Chained Cascade (Flood Fill):** Clearing a blank cell automatically cascades and uncovers connected zero-mine areas.

---

## Controls

- **Left Click:** Reveal a tile.
- **Right Click:** Place or remove a flag (Red Wool block).
- **New Game Button:** Reset the board at any time.

---

## Difficulty Modes

Choose your challenge upon startup:
- **Beginner:** 9 × 9 grid — 10 mines
- **Intermediate:** 16 × 16 grid — 40 mines
- **Expert:** 16 × 30 grid — 99 mines

---

## How to Run

Make sure you have Java (JDK 8 or newer) installed.

1. **Compile the game:**
   ```bash
   javac *.java
   ```

2. **Run the game:**
   ```bash
   java Main
   ```
