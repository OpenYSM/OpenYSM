package com.elfmcys.yesstevemodel.audio;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;

public interface IAudioStreamProvider {
    IAudioStreamSupport createAudioStream(AudioTrackData trackData) throws UnsupportedAudioFileException, IOException;
}