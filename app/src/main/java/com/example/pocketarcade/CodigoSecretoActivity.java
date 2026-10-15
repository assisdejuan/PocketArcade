package com.example.pocketarcade;

import android.graphics.Typeface;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.OvershootInterpolator;
import android.view.inputmethod.EditorInfo;
import android.widget.Toast;

import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.pocketarcade.databinding.ActivityCodigoSecretoBinding;
import com.example.pocketarcade.logica.CodigoSecreto;
import com.example.pocketarcade.logica.CodigoSecreto.Pista;
import com.example.pocketarcade.util.Extras;
import com.example.pocketarcade.util.Navegacao;
import com.example.pocketarcade.util.TelaUtil;
import com.example.pocketarcade.util.Validacao;

import java.util.Random;

/** Tela 5 · Cofre do Código Secreto: quatro dígitos diferentes em até oito tentativas. */
public class CodigoSecretoActivity extends AppCompatActivity {

    private static final String TAG = "CodigoSecretoActivity";

    private static final String ESTADO_CODIGO = "estado_codigo";
    private static final String ESTADO_TENTATIVAS = "estado_tentativas";
    private static final String ESTADO_INICIO = "estado_inicio";
    private static final String ESTADO_DURACAO = "estado_duracao";
    private static final String ESTADO_ENCERRADA = "estado_encerrada";
    private static final String ESTADO_VENCEU = "estado_venceu";
    private static final String ESTADO_HISTORICO = "estado_historico";

    private static final long DURACAO_CONCLUSAO_MS = 600L;
    private static final long DURACAO_CADEADO_MS = 600L;
    private static final float ESCALA_INICIAL_CONCLUSAO = 0.5f;
    private static final float GIRO_CADEADO = 360f;
    private static final float TAMANHO_RESUMO = 0.7f;
    private static final long MILIS_POR_SEGUNDO = 1000L;
    private static final long SEGUNDOS_POR_MINUTO = 60L;

    private ActivityCodigoSecretoBinding binding;
    private final Random random = new Random();
    private final SpannableStringBuilder historico = new SpannableStringBuilder();

    private String codigo;
    private int tentativasUsadas;
    /** Marcado com System.currentTimeMillis() no início; a diferença só é calculada no fim. */
    private long inicioMillis;
    private long duracaoMillis;
    private boolean encerrada;
    private boolean venceu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TelaUtil.ativarTelaCheia(this);
        binding = ActivityCodigoSecretoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Log.d(TAG, "onCreate");
        TelaUtil.aplicarEspacamentoDasBarras(binding.getRoot());

        String apelido = getIntent().getStringExtra(Extras.APELIDO);
        if (!Validacao.apelidoValido(apelido)) {
            Toast.makeText(this, R.string.erro_dados_sessao, Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        binding.toolbarCofre.setSubtitle(getString(R.string.jogador_atual, apelido));
        binding.toolbarCofre.setNavigationOnClickListener(view -> finish());

        binding.btnTentar.setOnClickListener(view -> tentar());
        binding.etPalpite.setOnEditorActionListener((view, acao, evento) -> {
            if (acao == EditorInfo.IME_ACTION_DONE) {
                tentar();
                return true;
            }
            return false;
        });
        binding.btnNovaPartidaCofre.setOnClickListener(view -> novaPartida());
        binding.barraNavegacao.btnVoltarCentral.setOnClickListener(view -> finish());
        binding.barraNavegacao.btnEncerrarSessao.setOnClickListener(view ->
                Navegacao.encerrarSessao(this, R.string.modulo_cofre));

        if (savedInstanceState != null) {
            restaurarEstado(savedInstanceState);
        } else {
            novaPartida();
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
        outState.putString(ESTADO_CODIGO, codigo);
        outState.putInt(ESTADO_TENTATIVAS, tentativasUsadas);
        outState.putLong(ESTADO_INICIO, inicioMillis);
        outState.putLong(ESTADO_DURACAO, duracaoMillis);
        outState.putBoolean(ESTADO_ENCERRADA, encerrada);
        outState.putBoolean(ESTADO_VENCEU, venceu);
        outState.putCharSequence(ESTADO_HISTORICO, historico);
    }

    private void restaurarEstado(@NonNull Bundle estado) {
        codigo = estado.getString(ESTADO_CODIGO);
        tentativasUsadas = estado.getInt(ESTADO_TENTATIVAS);
        inicioMillis = estado.getLong(ESTADO_INICIO);
        duracaoMillis = estado.getLong(ESTADO_DURACAO);
        encerrada = estado.getBoolean(ESTADO_ENCERRADA);
        venceu = estado.getBoolean(ESTADO_VENCEU);
        CharSequence historicoSalvo = estado.getCharSequence(ESTADO_HISTORICO);
        if (historicoSalvo != null && historicoSalvo.length() > 0) {
            historico.append(historicoSalvo);
            binding.tvHistorico.setText(historico);
        }
        atualizarTentativasRestantes();
        if (encerrada) {
            mostrarConclusao(false);
        }
    }

    private void novaPartida() {
        codigo = CodigoSecreto.gerar(random);
        Log.d(TAG, "Código sorteado (depuração): " + codigo);
        tentativasUsadas = 0;
        encerrada = false;
        venceu = false;
        duracaoMillis = 0L;
        inicioMillis = System.currentTimeMillis();

        historico.clear();
        binding.tvHistorico.setText(R.string.historico_vazio);
        binding.tvCodigoOculto.setText(R.string.codigo_oculto);
        binding.cardConclusaoCofre.animate().cancel();
        binding.cardConclusaoCofre.clearAnimation();
        binding.cardConclusaoCofre.setVisibility(View.GONE);
        binding.imgCadeado.setRotation(0f);
        binding.tilPalpite.setError(null);
        binding.etPalpite.setText(null);
        habilitarEntrada(true);
        atualizarTentativasRestantes();
    }

    private void tentar() {
        if (encerrada) {
            return;
        }
        String palpite = Validacao.textoDe(binding.etPalpite.getText());
        CodigoSecreto.ErroPalpite erro = CodigoSecreto.validar(palpite);
        if (erro != CodigoSecreto.ErroPalpite.NENHUM) {
            mostrarErro(erro);
            return;
        }
        binding.tilPalpite.setError(null);
        binding.etPalpite.setText(null);

        tentativasUsadas++;
        Pista[] pistas = CodigoSecreto.avaliar(codigo, palpite);
        registrarNoHistorico(palpite, pistas);
        atualizarTentativasRestantes();

        if (CodigoSecreto.acertou(pistas)) {
            concluir(true);
        } else if (tentativasUsadas >= CodigoSecreto.MAXIMO_TENTATIVAS) {
            concluir(false);
        }
    }

    private void mostrarErro(CodigoSecreto.ErroPalpite erro) {
        int mensagem;
        if (erro == CodigoSecreto.ErroPalpite.VAZIO) {
            mensagem = R.string.erro_palpite_vazio;
        } else if (erro == CodigoSecreto.ErroPalpite.DIGITOS_REPETIDOS) {
            mensagem = R.string.erro_palpite_repetido;
        } else {
            mensagem = R.string.erro_palpite_tamanho;
        }
        binding.tilPalpite.setError(getString(mensagem));
        binding.tilPalpite.startAnimation(AnimationUtils.loadAnimation(this, R.anim.tremor_erro));
    }

    /** Linha do histórico: cada dígito pintado com a cor da sua pista + um resumo textual. */
    private void registrarNoHistorico(String palpite, Pista[] pistas) {
        if (historico.length() > 0) {
            historico.append('\n');
        }
        historico.append(getString(R.string.linha_historico, tentativasUsadas)).append("  ");
        for (int i = 0; i < palpite.length(); i++) {
            int inicio = historico.length();
            historico.append(' ').append(palpite.charAt(i)).append(' ');
            int fim = historico.length();
            historico.setSpan(new BackgroundColorSpan(cor(corDaPista(pistas[i]))),
                    inicio, fim, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            historico.setSpan(new ForegroundColorSpan(cor(R.color.texto_sobre_pista)),
                    inicio, fim, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            historico.setSpan(new StyleSpan(Typeface.BOLD),
                    inicio, fim, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            historico.append(' ');
        }

        historico.append('\n');
        int inicioResumo = historico.length();
        historico.append(getString(R.string.resumo_pistas,
                CodigoSecreto.contar(pistas, Pista.VERDE),
                CodigoSecreto.contar(pistas, Pista.AMARELA),
                CodigoSecreto.contar(pistas, Pista.CINZA)));
        historico.setSpan(new RelativeSizeSpan(TAMANHO_RESUMO),
                inicioResumo, historico.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        historico.setSpan(new ForegroundColorSpan(cor(R.color.texto_suave)),
                inicioResumo, historico.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        binding.tvHistorico.setText(historico);
    }

    private void concluir(boolean acertou) {
        encerrada = true;
        venceu = acertou;
        duracaoMillis = System.currentTimeMillis() - inicioMillis;
        TelaUtil.esconderTeclado(this);
        mostrarConclusao(true);
    }

    /** Vitória ou derrota, tentativas usadas e tempo decorrido, na própria tela. */
    private void mostrarConclusao(boolean animar) {
        habilitarEntrada(false);
        binding.tvCodigoOculto.setText(formatarCodigo(codigo));

        binding.tvConclusaoTitulo.setText(venceu ? R.string.cofre_vitoria : R.string.cofre_derrota);
        binding.tvConclusaoTitulo.setTextColor(cor(venceu ? R.color.resultado_vitoria : R.color.resultado_derrota));
        String detalhes = getResources().getQuantityString(
                R.plurals.cofre_detalhes_tentativas, tentativasUsadas, tentativasUsadas)
                + '\n' + getString(R.string.cofre_detalhes_tempo, formatarTempo(duracaoMillis))
                + '\n' + getString(R.string.cofre_detalhes_codigo, codigo);
        binding.tvConclusaoDetalhes.setText(detalhes);
        binding.cardConclusaoCofre.setStrokeColor(cor(venceu ? R.color.resultado_vitoria : R.color.resultado_derrota));
        binding.cardConclusaoCofre.setVisibility(View.VISIBLE);

        View card = binding.cardConclusaoCofre;
        card.animate().cancel();
        if (!animar) {
            card.setAlpha(1f);
            card.setScaleX(1f);
            card.setScaleY(1f);
            return;
        }
        if (venceu) {
            // Sucesso: o cartão "salta" e o cadeado gira.
            card.setAlpha(0f);
            card.setScaleX(ESCALA_INICIAL_CONCLUSAO);
            card.setScaleY(ESCALA_INICIAL_CONCLUSAO);
            card.animate()
                    .alpha(1f)
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(DURACAO_CONCLUSAO_MS)
                    .setInterpolator(new OvershootInterpolator())
                    .start();
            binding.imgCadeado.animate()
                    .rotationBy(GIRO_CADEADO)
                    .setDuration(DURACAO_CADEADO_MS)
                    .start();
        } else {
            // Fracasso: o cartão surge e estremece (animação XML).
            card.setAlpha(0f);
            card.setScaleX(1f);
            card.setScaleY(1f);
            card.animate()
                    .alpha(1f)
                    .setDuration(DURACAO_CONCLUSAO_MS)
                    .start();
            card.startAnimation(AnimationUtils.loadAnimation(this, R.anim.tremor_erro));
        }
        binding.scrollCofre.smoothScrollTo(0, 0);
    }

    private void habilitarEntrada(boolean habilitar) {
        binding.etPalpite.setEnabled(habilitar);
        binding.btnTentar.setEnabled(habilitar);
    }

    private void atualizarTentativasRestantes() {
        int restantes = CodigoSecreto.MAXIMO_TENTATIVAS - tentativasUsadas;
        binding.tvTentativasRestantes.setText(getResources()
                .getQuantityString(R.plurals.tentativas_restantes, restantes, restantes));
    }

    @ColorRes
    private static int corDaPista(Pista pista) {
        switch (pista) {
            case VERDE:
                return R.color.pista_verde;
            case AMARELA:
                return R.color.pista_amarela;
            default:
                return R.color.pista_cinza;
        }
    }

    private int cor(@ColorRes int corResId) {
        return ContextCompat.getColor(this, corResId);
    }

    private static String formatarCodigo(String codigo) {
        StringBuilder espacado = new StringBuilder();
        for (int i = 0; i < codigo.length(); i++) {
            if (i > 0) {
                espacado.append(' ');
            }
            espacado.append(codigo.charAt(i));
        }
        return espacado.toString();
    }

    private String formatarTempo(long millis) {
        long segundos = millis / MILIS_POR_SEGUNDO;
        if (segundos < SEGUNDOS_POR_MINUTO) {
            return getString(R.string.tempo_segundos, (int) segundos);
        }
        return getString(R.string.tempo_minutos,
                (int) (segundos / SEGUNDOS_POR_MINUTO), (int) (segundos % SEGUNDOS_POR_MINUTO));
    }
}
