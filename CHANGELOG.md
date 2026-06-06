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
