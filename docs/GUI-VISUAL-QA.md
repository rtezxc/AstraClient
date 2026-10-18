# Astra GUI Visual QA

Use this checklist before calling a GUI change release-ready. Mark a check complete only after performing it; a successful Gradle build does not prove visual correctness.

## Automated checks

- [ ] Clean Java 21 / Gradle build succeeds.
- [ ] Production JAR contains the expected client classes and valid Fabric metadata.
- [ ] GUI input handlers reject mutations during the closing transition and clear drag state.
- [ ] Transition timing uses a monotonic clock, bounded progress, and shared duration constants.
- [ ] `git diff --check` passes.
- [ ] No duplicate module IDs or mismatch between registered modules and GUI entries.

## Manual in-game visual checks

### Layout and scaling
- [ ] Small, typical, and large window sizes.
- [ ] Multiple Minecraft GUI scales.
- [ ] Long module names and descriptions.
- [ ] Empty category and empty search results.
- [ ] Scroll at the top, middle, and bottom without drawing outside the list viewport.
- [ ] Resizing and dragging keep the panel within accessible screen bounds.

### Input and transitions
- [ ] Rapid category switching and search edits.
- [ ] Repeated opening and closing, including Escape during the opening transition.
- [ ] Close while dragging a slider, resizing, or scrolling.
- [ ] Sliders at minimum and maximum values.
- [ ] Keybind capture does not leak the captured key into gameplay.
- [ ] Reduced-motion mode removes unnecessary movement and closes predictably.
- [ ] No clicks pass through a visible modal or context menu.

### Rendering and lifecycle
- [ ] No flicker, ghosting, stale panels, or broken hitboxes after scale changes.
- [ ] Open and close after entering a world, leaving a world, and returning to the title screen.
- [ ] Optional background effects can be disabled without making the menu unusable.
- [ ] No persistent animation or background work after the screen closes.

### Persistence
- [ ] Theme and GUI preferences survive restart.
- [ ] Module settings restore correctly.
- [ ] Invalid or missing configuration values fail safely.
- [ ] Reset returns settings to valid defaults.
- [ ] Failed import/save does not destroy the last valid configuration.

## Reporting results

For every release candidate, record:
- Commit SHA and build run URL.
- Automated checks that actually ran and their outcomes.
- Minecraft version, rendering backend, window size, and GUI scale used for manual checks.
- Any checks not performed, with the reason.

Never mark manual checks complete based on compilation alone. If screenshots or an in-game runtime are unavailable, say so explicitly.
