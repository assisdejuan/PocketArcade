package com.example.pocketarcade;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.pocketarcade.databinding.ActivityScannerMensagensBinding;
import com.example.pocketarcade.logica.AnalisadorTexto;
import com.example.pocketarcade.util.Extras;
import com.example.pocketarcade.util.Navegacao;
import com.example.pocketarcade.util.TelaUtil;
import com.example.pocketarcade.util.Validacao;

/** Tela 3 · Scanner de Mensagens: analisa um texto em cinco medidas. */
public class ScannerMensagensActivity extends AppCompatActivity {

    private static final String TAG = "ScannerMensagensActivity";
    private static final String ESTADO_ANALISADO = "estado_analisado";

    private static final long DURACAO_ENTRADA_MS = 450L;
    private static final long DURACAO_SAIDA_MS = 300L;

    private ActivityScannerMensagensBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TelaUtil.ativarTelaCheia(this);
        binding = ActivityScannerMensagensBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Log.d(TAG, "onCreate");
        TelaUtil.aplicarEspacamentoDasBarras(binding.getRoot());

        String apelido = getIntent().getStringExtra(Extras.APELIDO);
        if (!Validacao.apelidoValido(apelido)) {
            Toast.makeText(this, R.string.erro_dados_sessao, Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        binding.toolbarScanner.setSubtitle(getString(R.string.jogador_atual, apelido));
        binding.toolbarScanner.setNavigationOnClickListener(view -> finish());

        binding.btnAnalisar.setOnClickListener(view -> analisar(true));
        binding.btnLimpar.setOnClickListener(view -> limpar());
        binding.barraNavegacao.btnVoltarCentral.setOnClickListener(view -> finish());
        binding.barraNavegacao.btnEncerrarSessao.setOnClickListener(view ->
                Navegacao.encerrarSessao(this, R.string.modulo_scanner));

        if (savedInstanceState != null && savedInstanceState.getBoolean(ESTADO_ANALISADO)) {
            analisar(false);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
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

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(ESTADO_ANALISADO,
                binding.cardResultadoScanner.getVisibility() == View.VISIBLE);
    }

    private void analisar(boolean animar) {
        String mensagem = Validacao.textoDe(binding.etMensagem.getText());
        if (mensagem.isEmpty()) {
            mostrarErro(R.string.erro_mensagem_vazia);
            return;
        }
        if (!Validacao.temMinimo(mensagem, Validacao.MINIMO_MENSAGEM)) {
            mostrarErro(R.string.erro_mensagem_curta);
            return;
        }
        binding.tilMensagem.setError(null);
        TelaUtil.esconderTeclado(this);

        AnalisadorTexto.Resultado resultado = AnalisadorTexto.analisar(mensagem);
        binding.tvResultadoCaracteres.setText(String.valueOf(resultado.caracteresSemEspacos));
        binding.tvResultadoPalavras.setText(String.valueOf(resultado.palavras));
        binding.tvResultadoVogais.setText(String.valueOf(resultado.vogais));
        binding.tvResultadoInvertido.setText(resultado.textoInvertido);
        binding.tvResultadoPalindromo.setText(resultado.palindromo ? R.string.sim : R.string.nao);

        mostrarPainelResultado(animar);
    }

    private void mostrarErro(int mensagemResId) {
        binding.tilMensagem.setError(getString(mensagemResId));
        binding.tilMensagem.startAnimation(AnimationUtils.loadAnimation(this, R.anim.tremor_erro));
    }

    /** Painel de resultado entra subindo e surgindo (ViewPropertyAnimator). */
    private void mostrarPainelResultado(boolean animar) {
        View painel = binding.cardResultadoScanner;
        painel.animate().setListener(null).cancel();
        painel.setVisibility(View.VISIBLE);
        if (!animar) {
            painel.setAlpha(1f);
            painel.setTranslationY(0f);
            return;
        }
        painel.setAlpha(0f);
        painel.setTranslationY(getResources().getDimension(R.dimen.deslocamento_animacao));
        painel.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(DURACAO_ENTRADA_MS)
                .setInterpolator(new DecelerateInterpolator())
                .start();
    }

    private void limpar() {
        binding.etMensagem.setText(null);
        binding.tilMensagem.setError(null);
        View painel = binding.cardResultadoScanner;
        if (painel.getVisibility() != View.VISIBLE) {
            return;
        }
        painel.animate()
                .alpha(0f)
                .setDuration(DURACAO_SAIDA_MS)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        painel.animate().setListener(null);
                        painel.setVisibility(View.GONE);
                    }
                })
                .start();
    }
}
