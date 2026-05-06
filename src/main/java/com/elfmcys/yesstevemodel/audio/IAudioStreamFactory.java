package com.elfmcys.yesstevemodel.audio;

import org.jetbrains.annotations.NotNull;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;

@FunctionalInterface
public interface IAudioStreamFactory {
    @NotNull
    IAudioStreamSupport openStream() throws UnsupportedAudioFileException, IOException;
}