package com.example.pocketarcade.util;

/** Chaves dos extras trocados entre as Activities por Intents explícitas. */
public final class Extras {

    private static final String PREFIXO = "com.example.pocketarcade.extra.";

    /** Apelido do jogador (Tela 1 → Central → desafios). */
    public static final String APELIDO = PREFIXO + "APELIDO";
    /** Nome do último módulo usado (desafio → Tela 1, em "Encerrar sessão"). */
    public static final String ULTIMO_MODULO = PREFIXO + "ULTIMO_MODULO";

    private Extras() {
    }
}
