package com.checkpoint.modules.library.entity;

/**
 * Tracks where a user is with a game.
 * Stored as STRING in DB — never change these names without a migration.
 */
public enum GameStatus {
    PLAYING,
    COMPLETED,
    BACKLOG,
    WISHLIST,
    DROPPED,
    PAUSED
}