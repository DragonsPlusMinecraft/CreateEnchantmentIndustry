## Create: Enchantment Industry 1.4.1

Final maintenance release for the legacy Minecraft 1.20.1-v1 branch.

### Fixed
- Fixed raw experience totals losing a point at some experience-bar progress values (#463).
- Fixed compatibility with Clumps. Merged liquid experience is now settled exactly once instead of repeatedly awarding the aggregate value (#389, #444).
- Prevented vanilla experience orbs from merging with raw experience-fluid orbs, and kept regular and hyper experience-fluid orbs separate so merged orbs retain the correct pickup behavior.
- Fixed the Disenchanter losing experience when it absorbs merged or partially accepted experience orbs.
- Fixed the amount of liquid experience dropped when one block is removed from a multi-block fluid tank.