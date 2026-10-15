package com.example.pocketarcade.util;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import androidx.activity.ComponentActivity;
import androidx.activity.EdgeToEdge;
import androidx.activity.SystemBarStyle;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.pocketarcade.R;

/** Ajustes visuais comuns às três telas. */
public final class TelaUtil {

    private TelaUtil() {
    }

    /** Desenha atrás das barras do sistema mantendo ícones claros sobre o fundo noturno. */
    public static void ativarTelaCheia(ComponentActivity activity) {
        int scrim = ContextCompat.getColor(activity, R.color.scrim_navegacao);
        EdgeToEdge.enable(activity,
                SystemBarStyle.dark(Color.TRANSPARENT),
                SystemBarStyle.dark(scrim));
    }

    /** Afasta o conteúdo das barras do sistema e do teclado. */
    public static void aplicarEspacamentoDasBarras(View raiz) {
        final int esquerda = raiz.getPaddingLeft();
        final int topo = raiz.getPaddingTop();
        final int direita = raiz.getPaddingRight();
        final int base = raiz.getPaddingBottom();
        ViewCompat.setOnApplyWindowInsetsListener(raiz, (view, insets) -> {
            Insets barras = insets.getInsets(WindowInsetsCompat.Type.systemBars()
                    | WindowInsetsCompat.Type.displayCutout());
            Insets teclado = insets.getInsets(WindowInsetsCompat.Type.ime());
            view.setPadding(esquerda + barras.left,
                    topo + barras.top,
                    direita + barras.right,
                    base + Math.max(barras.bottom, teclado.bottom));
            return WindowInsetsCompat.CONSUMED;
        });
    }

    public static void esconderTeclado(Activity activity) {
        View foco = activity.getCurrentFocus();
        if (foco == null) {
            return;
        }
        InputMethodManager imm =
                (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(foco.getWindowToken(), 0);
        }
        foco.clearFocus();
    }
}
