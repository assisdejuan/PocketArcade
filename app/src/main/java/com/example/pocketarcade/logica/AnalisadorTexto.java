package com.example.pocketarcade.logica;

import java.text.Normalizer;
import java.util.Locale;

/** Regras do Scanner de Mensagens. Java puro, sem dependências do Android. */
public final class AnalisadorTexto {

    private static final String VOGAIS = "aeiou";

    private AnalisadorTexto() {
    }

    /** As cinco medidas exibidas na tabela do Scanner. */
    public static final class Resultado {
        public final int caracteresSemEspacos;
        public final int palavras;
        public final int vogais;
        public final String textoInvertido;
        public final boolean palindromo;

        Resultado(int caracteresSemEspacos, int palavras, int vogais,
                  String textoInvertido, boolean palindromo) {
            this.caracteresSemEspacos = caracteresSemEspacos;
            this.palavras = palavras;
            this.vogais = vogais;
            this.textoInvertido = textoInvertido;
            this.palindromo = palindromo;
        }
    }

    public static Resultado analisar(String texto) {
        return new Resultado(
                contarCaracteresSemEspacos(texto),
                contarPalavras(texto),
                contarVogais(texto),
                inverter(texto),
                ehPalindromo(texto));
    }

    public static int contarCaracteresSemEspacos(String texto) {
        int total = 0;
        for (int i = 0; i < texto.length(); ) {
            int codigo = texto.codePointAt(i);
            if (!Character.isWhitespace(codigo)) {
                total++;
            }
            i += Character.charCount(codigo);
        }
        return total;
    }

    public static int contarPalavras(String texto) {
        String limpo = texto.trim();
        return limpo.isEmpty() ? 0 : limpo.split("\\s+").length;
    }

    /** Conta vogais com ou sem acento (á, ê, õ, ü… contam como vogais). */
    public static int contarVogais(String texto) {
        String semAcentos = removerAcentos(texto).toLowerCase(Locale.ROOT);
        int total = 0;
        for (int i = 0; i < semAcentos.length(); i++) {
            if (VOGAIS.indexOf(semAcentos.charAt(i)) >= 0) {
                total++;
            }
        }
        return total;
    }

    public static String inverter(String texto) {
        return new StringBuilder(texto).reverse().toString();
    }

    /**
     * Ignora espaços e diferenças entre maiúsculas e minúsculas
     * (e também acentos e pontuação), então "Ana" e "A sacada da casa" são palíndromos.
     */
    public static boolean ehPalindromo(String texto) {
        String semAcentos = removerAcentos(texto).toLowerCase(Locale.ROOT);
        StringBuilder normalizado = new StringBuilder();
        for (int i = 0; i < semAcentos.length(); i++) {
            char c = semAcentos.charAt(i);
            if (Character.isLetterOrDigit(c)) {
                normalizado.append(c);
            }
        }
        if (normalizado.length() == 0) {
            return false;
        }
        String ida = normalizado.toString();
        return ida.equals(normalizado.reverse().toString());
    }

    private static String removerAcentos(String texto) {
        return Normalizer.normalize(texto, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
    }
}
