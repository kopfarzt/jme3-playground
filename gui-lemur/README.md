# Status #

* Switch between a `GameState` and a `UiState` by pressing the `TAB` key.
* The `GameState` should enable the fly-by camera, in `UiState` the mouse pointer should be visible.
* Geometry (incl. lights and sky-box) is initialized in the `GameState`.

## Open ## 

## Closed ##

<del>
* Game does not start with disabled GUI.
* Game needs 2 presses of TAB before it reacts to TAB.
* Switching between GUI and GAME mode is not working correctly, the mouse pointer is visible in the wrong state.
* Needs some concept about game design and switch between GUI and GAME mode.
</del>

## Lessons Learned ##

* BaseAppState represents a higher abstraction layer than AbstractAppState.
* All AppState `initialize()` functions will be called later in the order the states were added.
* `GuiGlobals.initialize()` changes something with the mouse pointer and is therefore called
  lazy in the `onEnable()` function. 