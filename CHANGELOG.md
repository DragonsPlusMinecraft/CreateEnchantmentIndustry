## Create: Enchantment Industry 2.5.0c for Minecraft 1.20.1

### Fixes
* Restored Blaze Enchanter sample behavior to match the 1.21.1 implementation: enchantable equipment can be used as samples, while Enchanting Templates cannot ([#473](https://github.com/DragonsPlusMinecraft/CreateEnchantmentIndustry/issues/473)).
* Revalidated enchantment results and processing requirements continuously, preventing stale saved results from completing after their conditions change.
* Restricted generated penalty enchantments to obtainable curses.

### Compatibility
* Corrected legacy 1.4.1 and early 2.5 save migration. Obsolete exact-target data, invalid template samples, and incomplete processing state are discarded instead of being reinterpreted, while transported input items are preserved.
