package com.example.pocketarcade.util;

import android.app.Activity;
import android.content.Intent;

import androidx.annotation.StringRes;

import com.example.pocketarcade.MainActivity;

/** Intents explícitas usadas por mais de uma tela. */
public final class Navegacao {

    private Navegacao() {
    }

    /** Abre um desafio levando o apelido do jogador. */
    public static void abrirDesafio(Activity origem, Class<? extends Activity> desafio, String apelido) {
        Intent intent = new Intent(origem, desafio);
        intent.putExtra(Extras.APELIDO, apelido);
        origem.startActivity(intent);
    }

    /**
     * "Encerrar sessão": volta à instância já existente da Tela 1, removendo Central e desafio
     * da pilha (CLEAR_TOP) sem recriar a Tela 1 (SINGLE_TOP → onNewIntent()).
     */
    public static void encerrarSessao(Activity origem, @StringRes int nomeModulo) {
        Intent intent = new Intent(origem, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra(Extras.ULTIMO_MODULO, origem.getString(nomeModulo));
        origem.startActivity(intent);
    }
}
