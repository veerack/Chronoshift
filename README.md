# Chronoshift

A **Chronoshift** is a legendary time-bending item for Hytale servers. When held in your offhand, right-click to set a checkpoint. If your health drops below 30%, you'll automatically teleport back to your checkpoint with your inventory restored, and the Chronoshift will be consumed.

## Features

- **Checkpoint System**: Right-click to save your position, health, and inventory
- **Auto-Activation**: Triggers automatically when health falls below 30%
- **Inventory Restoration**: Returns your inventory to the exact state it was when the checkpoint was set
- **Anti-Dupe Protection**: Prevents item drops and chest interactions while Chronoshift is held
- **Time-Bending Effects**: Custom animations, particles, and sounds
- **Toggle Functionality**: Right-click again to deactivate your checkpoint

## Installation

1. Download the latest `Chronoshift.jar` from the [Releases](https://github.com/yourusername/Chronoshift/releases) page
2. Place the jar file in your server's `plugins/` folder
3. Restart your server
4. Give the item to players: `/item give Chronoshift`

## Building from Source

### Prerequisites

- Java 25 or higher
- Gradle 8.0 or higher
- Hytale Server jar (place in `libs/HytaleServer.jar`)

### Build Steps

```bash
# Clone the repository
git clone https://github.com/yourusername/Chronoshift.git
cd Chronoshift

# Build the plugin
./gradlew build

# The jar will be in build/libs/Chronoshift.jar
```

## Usage

### For Players

1. **Equip Chronoshift**: Place Chronoshift in your offhand (utility slot)
2. **Set Checkpoint**: Right-click to activate. You'll see a message: "The Chronoshift started bending time..."
3. **Play Normally**: Go explore, fight, or adventure
4. **Auto-Teleport**: If your health drops below 30%, you'll automatically be sent back to your checkpoint after 3 seconds
5. **Consumption**: The Chronoshift is consumed on use (you'll need to craft another one)

### Commands

- `/tc` - Toggle checkpoint (activate if no checkpoint, deactivate if one exists)

### Protection Rules

While holding Chronoshift:
- ✅ Can move and break/place blocks
- ❌ Cannot drop items (prevents duping)
- ❌ Cannot open chests, furnaces, or crafting stations
- ❌ Cannot move items to/from other inventories

Once a checkpoint is set:
- All restrictions above remain active
- Deactivate with `/tc` to re-enable inventory interactions

## Crafting

Chronoshift can be crafted in an Arcane Bench:

**Ingredients:**
- 4x Gold Bar
- 6x Topaz
- 1x Silver Bar
- 1x Teleporter

**Required:** Memories Level 3
**Crafting Time:** 30 seconds
**Bench:** Arcane Bench (Tier 1+)

## Configuration

Checkpoint data is stored in `plugins/Chronoshift/checkpoints.json`

## Credits

**Author**: [veerack](https://github.com/veerack)

**License**: MIT License - See [LICENSE](LICENSE) for details

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Acknowledgments

- Built for the Hytale modding community
- Uses the HyUI library for custom interfaces
- Inspired by time-manipulation mechanics in various games
