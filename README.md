<div align="center">

<img src="src/main/resources/chronoshift/Common/Icons/ItemsGenerated/TimewiseClock.png" alt="Chronoshift icon" width="160" />

# Chronoshift

### *A Time-Bending Item for Hytale*

*A mythical, ancient clock that manipulates time in dangerous situations.*

[![License: MIT](https://img.shields.io/badge/License-MIT-purple.svg)](https://opensource.org/licenses/MIT)
[![Java](https://img.shields.io/badge/Java-25+-orange.svg)](https://www.oracle.com/java/)
[![Gradle](https://img.shields.io/badge/Gradle-8.0+-green.svg)](https://gradle.org/)

</div>

---

## Features

- **Auto-Activation**: Automatically triggers when you take fatal damage.
- **Easy Usage**: Right-click to activate or deactivate Chronoshift (toggle).
- **Time Rewind**: Saves you from dangerous situations by rewinding time, restoring your health and inventory.
- **Limitations**: Due to the game mechanics, I had to make sure duping wasn't possible, so:
  - Chronoshift can be activated from anywhere, but it will only work if it's in your offhand (left bottom slot (1)) when you take fatal damage.
  - When you activate Chronoshift, you can't drop items from your inventory. 
  - Just having Chronoshift in your inventory also blocks you from interacting with... basically everything (you still can drop items). 
  - You can move items **INSIDE** your inventory at any time (even while Chronoshift is active).

---

## Installation

### Quick Start

1. **Download** the latest `Chronoshift-x.x.x.jar` from the [Releases](https://github.com/veerack/Chronoshift/releases) page
2. **Place** the jar file in your server's `AppData\Roaming\Hytale\UserData\Mods` folder
3. **Activate** it in the Hytale client for your world

## How to Use

### Basic Usage

1. **Equip Chronoshift**: Hold Chronoshift
2. **Activation**: Right-click to activate
3. **MANDATORY**: The **Chronoshift** item must be held in the off-hand, in the **FIRST** slot (bottom left one) in order for it to be triggered upon death.
3. **Adventure**: Explore, fight, risk your life
4. **Auto-Protection**: If you take fatal damage:
   - 3-second countdown begins
   - Visual and audio effects play
   - Teleport back to where you activated Chronoshift
   - Health restored to 100%
   - Inventory restored to how it was when you activated Chronoshift
5. **Consumption**: Chronoshift disappears after use (one-time use)

### Protection System

While Chronoshift is **active** (checkpoint set):

| Action | Status | Reason |
|--------|--------|--------|
| Move/Break/Place Blocks | ✅ Allowed | Normal gameplay |
| Move items in inventory | ✅ Allowed | Organization |
| Drop items | ❌ Blocked | Prevents duping |
| Open containers | ❌ Blocked | Prevents duping |
| Interact with blocks | ❌ Blocked | Prevents exploits |

---

## Crafting

### Recipe

Craft at an **Arcane Bench** (Tier 1+):

| Ingredient | Quantity |
|------------|----------|
| Gold Bar | 4x |
| Topaz | 6x |
| Silver Bar | 1x |
| Teleporter | 1x |

**Requirements**:
- Memories Level: 3
- Crafting Time: 30 seconds
- Category: Arcane Portals

---

## Building from Source

### Prerequisites

- JDK 25+
- Gradle 8.0+
- `HytaleServer.jar`

### Build Steps

```bash
# Clone the repository
git clone https://github.com/veerack/Chronoshift.git
cd Chronoshift

# Build with Gradle
./gradlew build

# Output: build/libs/Chronoshift-1.0.0.jar
```

**Development**:
```bash
./gradlew clean
./gradlew jarChronoshift
./gradlew test
```

---

## Configuration

Data is stored in:
```
plugins/Chronoshift/checkpoints.json
```

---

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

## Author

**veerack** - [GitHub](https://github.com/veerack)

---

## Acknowledgments

- **[Hytix](https://x.com/HytixEU) Team** – Thanks for making this mod possible!

<div align="center">

[⬆ Back to Top](#-chronoshift)

</div>
