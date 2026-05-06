package com.elfmcys.yesstevemodel.audio;

public class PlaybackFlags {

    private final boolean audioEnabled;

    private AudioPlayerManager audioPlayerManager;

    private boolean paused;

    private boolean stopped;

    public PlaybackFlags(boolean z) {
        this.audioEnabled = z;
    }

    public void setPaused(boolean z) {
        this.paused = z;
    }

    public void setStopped(boolean z) {
        this.stopped = z;
    }

    public boolean isPaused() {
        return this.paused;
    }

    public boolean isStopped() {
        return this.stopped;
    }

    public AudioPlayerManager getAudioPlayerManager() {
        if (!this.audioEnabled) {
            return null;
        }
        if (this.audioPlayerManager == null) {
            this.audioPlayerManager = new AudioPlayerManager();
        }
        return this.audioPlayerManager;
    }
}