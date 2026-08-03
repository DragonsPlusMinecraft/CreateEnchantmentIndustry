## Create: Enchantment Industry 1.4.1

Final maintenance release for the legacy Minecraft 1.20.1 branch.

### Fixed
- Fixed raw experience totals losing a point at some experience-bar progress values (#463).
- Fixed compatibility with Clumps. Merged liquid experience is now settled exactly once instead of repeatedly awarding the aggregate value (#389, #444).
- Prevented vanilla experience orbs from merging with raw experience-fluid orbs, and kept regular and hyper experience-fluid orbs separate so merged orbs retain the correct pickup behavior.
- Fixed the Disenchanter losing experience when it absorbs merged or partially accepted experience orbs.
- Fixed the amount of liquid experience dropped when one block is removed from a multi-block fluid tank.

## Create: Enchantment Industry 1.4.0

Update for Create 6.0.8

### Fixed
- Fixed liquid experience from open-ended pipes bypassing Create's open pipe lifecycle, which could leave experience leaking after pumps stopped, valves closed, or pipes were removed.
- Fixed liquid experience grants being multiplied by other mods' XP gain bonuses. Liquid experience now grants and removes raw experience points so XP remains conserved.
- Fixed experience orbs created by liquid experience drops being affected by XP gain multipliers when picked up.
- Fixed the "Experienced Recycler" advancement only counting item disenchanting. It now counts experience recycled by the Disenchanter from items, players, and experience orbs when the recycled experience is successfully inserted into the internal tank.

### Added
- Added server config `enchantmentLevelCaps` for per-enchantment hard level caps. Use entries like `minecraft:protection=10` or `minecraft:knockback=20`.
- Printer now refuses to copy enchanted books above a configured per-enchantment cap.
