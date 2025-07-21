# BreakoutExtension

**BreakoutExtension** is an advanced version of the classic Breakout game built with the ACM Java libraries. It features power‑ups (“surprises”), bullets you can fire, color‑coded bricks with different point values, sound effects, animated win/lose screens, and more.

---

## 🎮 Gameplay

- **Lives**: You start with 3 turns (balls).  
- **Bricks**: 10 rows × 10 columns.  
  - Cyan = 1 pt, Green = 2 pt, Yellow = 4 pt, Orange = 8 pt, Red = 16 pt  
- **Surprises** (random drop from bricks at 10% chance):
  - Enlarge or shrink paddle  
  - Darken background  
  - +10 score  
  - +1 bullet (max 3)  
- **Bullets**: Press **Space** to shoot (max one every 2 s).  
- **Audio**: Intro, brick hits, shooting, win/lose sounds.  
- **Win**: Clear all bricks → celebratory animation.  
- **Lose**: Run out of lives → game over screen.

---

## ⚙️ How It Works

1. **Setup**:  
   - Builds bricks in a grid.  
   - Places paddle at bottom; listens to mouse movement.  
2. **Game Loop**:  
   - Waits for click → launches ball with random velocity.  
   - Moves ball; bounces off walls/paddle.  
   - Detects brick collisions → removes brick, updates score, may drop a surprise.  
   - Handles bullets if fired.  
3. **Surprise Logic**:  
   - Randomly generates an extra object (GOval) when a brick is hit.  
   - If caught by paddle → applies one of five effects.  
4. **Scoring & Labels**:  
   - Dynamically updates and removes on‑screen labels (score, lives, “+10 points,” etc.).  
5. **End Conditions**:  
   - Win: no bricks left → color‑cycling “YOU WON” screen.  
   - Lose: no turns → “YOU LOST” screen.

---

## 🖥️ Requirements

- Java 8 or later  
- ACM Java Libraries (acm.jar) on your classpath  
- Eclipse (optional) or any Java IDE / `javac` + `java`


