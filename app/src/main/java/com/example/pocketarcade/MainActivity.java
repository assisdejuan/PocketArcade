package com.example.pocketarcade;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AnimationUtils;
import android.view.animation.OvershootInterpolator;
import android.view.inputmethod.EditorInfo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.pocketarcade.databinding.ActivityMainBinding;
import com.example.pocketarcade.util.Extras;
import com.example.pocketarcade.util.TelaUtil;
import com.example.pocketarcade.util.Validacao;

/** Tela 1 · Apresentação: o jogador se identifica e entra no Pocket Arcade. */
public class MainActivity extends AppCompatActivity {

    private static final String TAG = "ApresentacaoActivity";
    private static final String ESTADO_ULTIMO_MODULO = "estado_ultimo_modulo";

    private static final long DURACAO_BOTAO_MS = 350L;
    private static final long DURACAO_ULTIMO_MODULO_MS = 450L;
    private static final float ESCALA_BOTAO_PRESSIONADO = 0.9f;
    private static final float ALFA_BOTAO_PRESSIONADO = 0.6f;
    private static final float ESCALA_INICIAL_DESTAQUE = 0.85f;

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TelaUtil.ativarTelaCheia(this);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Log.d(TAG, "onCreate");
        TelaUtil.aplicarEspacamentoDasBarras(binding.getRoot());

        binding.btnEntrar.setOnClickListener(view -> entrar());
        binding.etApelido.setOnEditorActionListener((view, acao, evento) -> {
            if (acao == EditorInfo.IME_ACTION_GO) {
                entrar();
                return true;
            }
            return false;
        });

        if (savedInstanceState != null) {
            CharSequence ultimoModulo = savedInstanceState.getCharSequence(ESTADO_ULTIMO_MODULO);
            if (ultimoModulo != null) {
                binding.tvUltimoModulo.setText(ultimoModulo);
                binding.tvUltimoModulo.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        restaurarBotaoEntrar();
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
    }

    /** Retorno de "Encerrar sessão" (CLEAR_TOP | SINGLE_TOP): esta mesma instância é reaproveitada. */
    @Override
    protected void onNewIntent(@NonNull Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        Log.d(TAG, "onNewIntent");
        mostrarUltimoModulo(intent.getStringExtra(Extras.ULTIMO_MODULO));
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (binding.tvUltimoModulo.getVisibility() == View.VISIBLE) {
            outState.putCharSequence(ESTADO_ULTIMO_MODULO, binding.tvUltimoModulo.getText());
        }
    }

    private void entrar() {
        String apelido = Validacao.textoDe(binding.etApelido.getText());
        if (!Validacao.apelidoValido(apelido)) {
            binding.tilApelido.setError(getString(R.string.erro_apelido));
            binding.tilApelido.startAnimation(AnimationUtils.loadAnimation(this, R.anim.tremor_erro));
            return;
        }
        binding.tilApelido.setError(null);
        TelaUtil.esconderTeclado(this);

        // Animação do botão com ViewPropertyAnimator; a navegação acontece ao fim dela.
        binding.btnEntrar.setEnabled(false);
        binding.btnEntrar.animate()
                .scaleX(ESCALA_BOTAO_PRESSIONADO)
                .scaleY(ESCALA_BOTAO_PRESSIONADO)
                .alpha(ALFA_BOTAO_PRESSIONADO)
                .setDuration(DURACAO_BOTAO_MS)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        binding.btnEntrar.animate().setListener(null);
                        abrirCentral(apelido);
                    }
                })
                .start();
    }

    private void abrirCentral(String apelido) {
        Intent intent = new Intent(this, CentralDesafiosActivity.class);
        intent.putExtra(Extras.APELIDO, apelido);
        startActivity(intent);
    }

    private void restaurarBotaoEntrar() {
        binding.btnEntrar.animate().setListener(null).cancel();
        binding.btnEntrar.setScaleX(1f);
        binding.btnEntrar.setScaleY(1f);
        binding.btnEntrar.setAlpha(1f);
        binding.btnEntrar.setEnabled(true);
    }

    private void mostrarUltimoModulo(String modulo) {
        if (!Validacao.temMinimo(modulo, 1)) {
            return;
        }
        binding.tvUltimoModulo.setText(getString(R.string.ultimo_modulo, modulo));
        binding.tvUltimoModulo.setVisibility(View.VISIBLE);
        binding.tvUltimoModulo.setAlpha(0f);
        binding.tvUltimoModulo.setScaleX(ESCALA_INICIAL_DESTAQUE);
        binding.tvUltimoModulo.setScaleY(ESCALA_INICIAL_DESTAQUE);
        binding.tvUltimoModulo.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(DURACAO_ULTIMO_MODULO_MS)
                .setInterpolator(new OvershootInterpolator())
                .setListener(null)
                .start();
    }
}
