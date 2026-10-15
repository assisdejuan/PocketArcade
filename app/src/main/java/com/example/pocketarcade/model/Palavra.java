package com.example.pocketarcade.model;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;

import java.io.Serializable;

/** Item cadastrado no Randomizador. Serializable só para sobreviver à rotação. */
public class Palavra implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String texto;
    private final String categoria;
    @DrawableRes
    private final int iconeCategoriaResId;

    public Palavra(@NonNull String texto, @NonNull String categoria, @DrawableRes int iconeCategoriaResId) {
        this.texto = texto;
        this.categoria = categoria;
        this.iconeCategoriaResId = iconeCategoriaResId;
    }

    @NonNull
    public String getTexto() {
        return texto;
    }

    @NonNull
    public String getCategoria() {
        return categoria;
    }

    @DrawableRes
    public int getIconeCategoriaResId() {
        return iconeCategoriaResId;
    }
}
