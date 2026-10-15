package com.example.pocketarcade.logica;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/** Regras do Cofre do Código Secreto. Java puro, sem dependências do Android. */
public final class CodigoSecreto {

    public static final int TAMANHO = 4;
    public static final int MAXIMO_TENTATIVAS = 8;

    /** Pista de cada dígito, inspirada no jogo TERMO. */
    public enum Pista {
        /** Dígito certo na posição certa. */
        VERDE,
        /** Dígito existe no código, mas em outra posição. */
        AMARELA,
        /** Dígito não existe no código. */
        CINZA
    }

    public enum ErroPalpite {
        NENHUM,
        VAZIO,
        TAMANHO_INVALIDO,
        DIGITOS_REPETIDOS
    }

    private CodigoSecreto() {
    }

    /** Sorteia quatro dígitos diferentes (o zero pode aparecer em qualquer posição). */
    public static String gerar(Random random) {
        List<Integer> digitos = new ArrayList<>();
        for (int d = 0; d <= 9; d++) {
            digitos.add(d);
        }
        Collections.shuffle(digitos, random);
        StringBuilder codigo = new StringBuilder();
        for (int i = 0; i < TAMANHO; i++) {
            codigo.append(digitos.get(i));
        }
        return codigo.toString();
    }

    /** Valida na ordem: campo vazio, exatamente quatro números, sem dígitos repetidos. */
    public static ErroPalpite validar(String palpite) {
        if (palpite == null || palpite.isEmpty()) {
            return ErroPalpite.VAZIO;
        }
        if (palpite.length() != TAMANHO) {
            return ErroPalpite.TAMANHO_INVALIDO;
        }
        boolean[] visto = new boolean[10];
        for (int i = 0; i < palpite.length(); i++) {
            char c = palpite.charAt(i);
            if (c < '0' || c > '9') {
                return ErroPalpite.TAMANHO_INVALIDO;
            }
            if (visto[c - '0']) {
                return ErroPalpite.DIGITOS_REPETIDOS;
            }
            visto[c - '0'] = true;
        }
        return ErroPalpite.NENHUM;
    }

    /** Pré-condição: palpite já validado (quatro dígitos diferentes). */
    public static Pista[] avaliar(String codigo, String palpite) {
        Pista[] pistas = new Pista[TAMANHO];
        for (int i = 0; i < TAMANHO; i++) {
            char digito = palpite.charAt(i);
            if (codigo.charAt(i) == digito) {
                pistas[i] = Pista.VERDE;
            } else if (codigo.indexOf(digito) >= 0) {
                pistas[i] = Pista.AMARELA;
            } else {
                pistas[i] = Pista.CINZA;
            }
        }
        return pistas;
    }

    public static boolean acertou(Pista[] pistas) {
        for (Pista pista : pistas) {
            if (pista != Pista.VERDE) {
                return false;
            }
        }
        return true;
    }

    public static int contar(Pista[] pistas, Pista tipo) {
        int total = 0;
        for (Pista pista : pistas) {
            if (pista == tipo) {
                total++;
            }
        }
        return total;
    }
}
