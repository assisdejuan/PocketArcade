package com.example.pocketarcade.model;

import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;

import com.example.pocketarcade.R;

import java.util.Random;

/** Jogadas da Arena de Pedra, Papel e Tesoura. */
public enum Jogada {
    PEDRA(R.string.jogada_pedra, R.drawable.img_pedra),
    PAPEL(R.string.jogada_papel, R.drawable.img_papel),
    TESOURA(R.string.jogada_tesoura, R.drawable.img_tesoura);

    public static final int VITORIA = 1;
    public static final int EMPATE = 0;
    public static final int DERROTA = -1;

    @StringRes
    private final int nomeResId;
    @DrawableRes
    private final int imagemResId;

    Jogada(@StringRes int nomeResId, @DrawableRes int imagemResId) {
        this.nomeResId = nomeResId;
        this.imagemResId = imagemResId;
    }

    @StringRes
    public int getNomeResId() {
        return nomeResId;
    }

    @DrawableRes
    public int getImagemResId() {
        return imagemResId;
    }

    /** @return VITORIA, EMPATE ou DERROTA do ponto de vista desta jogada. */
    public int compararCom(Jogada outra) {
        if (this == outra) {
            return EMPATE;
        }
        return venceDe(outra) ? VITORIA : DERROTA;
    }

    private boolean venceDe(Jogada outra) {
        switch (this) {
            case PEDRA:
                return outra == TESOURA;
            case PAPEL:
                return outra == PEDRA;
            default:
                return outra == PAPEL;
        }
    }

    public static Jogada sortear(Random random) {
        Jogada[] todas = values();
        return todas[random.nextInt(todas.length)];
    }
}
