package com.example.pocketarcade.util;

import android.media.MediaPlayer;

import androidx.annotation.Nullable;

/**
 * Ajudantes para reutilizar instâncias de MediaPlayer criadas no onCreate().
 * Nenhum player é criado aqui: as telas criam no onCreate() e liberam no onDestroy().
 */
public final class Sons {

    private Sons() {
    }

    /** Toca desde o início, reaproveitando a mesma instância. */
    public static void tocar(@Nullable MediaPlayer player) {
        if (player == null) {
            return;
        }
        if (player.isPlaying()) {
            player.pause();
        }
        player.seekTo(0);
        player.start();
    }

    /** Usado ao sair da tela: pausa (nunca stop()). */
    public static void pausar(@Nullable MediaPlayer player) {
        if (player != null && player.isPlaying()) {
            player.pause();
        }
    }
}
