package com.example.pocketarcade.util;

import androidx.annotation.Nullable;

/** Regras de validação compartilhadas pelas telas. */
public final class Validacao {

    public static final int MINIMO_APELIDO = 3;
    public static final int MINIMO_MENSAGEM = 3;
    public static final int MINIMO_PALAVRA = 2;

    private Validacao() {
    }

    public static boolean temMinimo(@Nullable CharSequence texto, int minimo) {
        return texto != null && texto.toString().trim().length() >= minimo;
    }

    public static boolean apelidoValido(@Nullable CharSequence apelido) {
        return temMinimo(apelido, MINIMO_APELIDO);
    }

    /** Texto digitado sem espaços nas pontas; nunca null. */
    public static String textoDe(@Nullable CharSequence texto) {
        return texto == null ? "" : texto.toString().trim();
    }
}
