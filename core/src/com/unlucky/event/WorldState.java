package com.unlucky.event;

/**
 * Different states for events such as walking, battling, picking up items
 *
 * @author Ming Li
 */
public enum WorldState {
    NONE,
    MOVING,
    BATTLING,
    TRANSITION,
    LEVEL_UP,
    INVENTORY,
    IN_TILE_EVENT,
    DEATH,
    PAUSE
}
