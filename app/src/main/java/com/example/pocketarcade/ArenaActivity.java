package com.example.pocketarcade;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.Toast;

import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.pocketarcade.databinding.ActivityArenaBinding;
import com.example.pocketarcade.model.Jogada;
import com.example.pocketarcade.util.Extras;
import com.example.pocketarcade.util.Navegacao;
import com.example.pocketarcade.util.Sons;
import com.example.pocketarcade.util.TelaUtil;
import com.example.pocketarcade.util.Validacao;

import java.util.Random;

/** Tela 4 · Arena de Pedra, Papel e Tesoura (melhor de três). */
public class ArenaActivity extends AppCompatActivity {

    private static final String TAG = "ArenaActivity";

    private static final String ESTADO_PONTOS_JOGADOR = "estado_pontos_jogador";
    private static final String ESTADO_PONTOS_DISPOSITIVO = "estado_pontos_dispositivo";
    private static final String ESTADO_JOGADA_JOGADOR = "estado_jogada_jogador";
    private static final String ESTADO_JOGADA_DISPOSITIVO = "estado_jogada_dispositivo";

    private static final int VITORIAS_PARA_VENCER = 2;
    private static final int SEM_JOGADA = -1;

    private static final long DURACAO_ESCOLHA_JOGADOR_MS = 400L;
    private static final long DURACAO_ESCOLHA_DISPOSITIVO_MS = 550L;
    private static final long DURACAO_MENSAGEM_MS = 350L;
    private static final long DURACAO_RESULTADO_FINAL_MS = 650L;
    private static final float ESCALA_INICIAL = 0.3f;
    private static final float GIRO_INICIAL_DISPOSITIVO = 90f;

    private ActivityArenaBinding binding;
    private final Random random = new Random();

    // Criados uma única vez no onCreate() e reutilizados a cada rodada.
    @Nullable
    private MediaPlayer somVitoria;
    @Nullable
    private MediaPlayer somDerrota;
    @Nullable
    private MediaPlayer somEmpate;

    private int pontosJogador;
    private int pontosDispositivo;
    @Nullable
    private Jogada ultimaJogadaJogador;
    @Nullable
    private Jogada ultimaJogadaDispositivo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TelaUtil.ativarTelaCheia(this);
        binding = ActivityArenaBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Log.d(TAG, "onCreate");
        TelaUtil.aplicarEspacamentoDasBarras(binding.getRoot());

        String apelido = getIntent().getStringExtra(Extras.APELIDO);
        if (!Validacao.apelidoValido(apelido)) {
            Toast.makeText(this, R.string.erro_dados_sessao, Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        somVitoria = MediaPlayer.create(this, R.raw.som_vitoria);
        somDerrota = MediaPlayer.create(this, R.raw.som_derrota);
        somEmpate = MediaPlayer.create(this, R.raw.som_empate);

        binding.toolbarArena.setSubtitle(getString(R.string.jogador_atual, apelido));
        binding.toolbarArena.setNavigationOnClickListener(view -> finish());
        binding.tvNomeJogadorPlacar.setText(apelido);

        binding.btnPedra.setOnClickListener(view -> jogar(Jogada.PEDRA));
        binding.btnPapel.setOnClickListener(view -> jogar(Jogada.PAPEL));
        binding.btnTesoura.setOnClickListener(view -> jogar(Jogada.TESOURA));
        binding.btnNovaPartidaArena.setOnClickListener(view -> novaPartida());
        binding.barraNavegacao.btnVoltarCentral.setOnClickListener(view -> finish());
        binding.barraNavegacao.btnEncerrarSessao.setOnClickListener(view ->
                Navegacao.encerrarSessao(this, R.string.modulo_arena));

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

    /** Ao sair da tela o áudio é apenas pausado (sem stop()). */
    @Override
    protected void onPause() {
        super.onPause();
        Sons.pausar(somVitoria);
        Sons.pausar(somDerrota);
        Sons.pausar(somEmpate);
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop");
    }

    /** release() libera os recursos nativos e leva cada player ao estado End. */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
        if (somVitoria != null) {
            somVitoria.release();
            somVitoria = null;
        }
        if (somDerrota != null) {
            somDerrota.release();
            somDerrota = null;
        }
        if (somEmpate != null) {
            somEmpate.release();
            somEmpate = null;
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(ESTADO_PONTOS_JOGADOR, pontosJogador);
        outState.putInt(ESTADO_PONTOS_DISPOSITIVO, pontosDispositivo);
        outState.putInt(ESTADO_JOGADA_JOGADOR,
                ultimaJogadaJogador == null ? SEM_JOGADA : ultimaJogadaJogador.ordinal());
        outState.putInt(ESTADO_JOGADA_DISPOSITIVO,
                ultimaJogadaDispositivo == null ? SEM_JOGADA : ultimaJogadaDispositivo.ordinal());
    }

    private void restaurarEstado(@NonNull Bundle estado) {
        pontosJogador = estado.getInt(ESTADO_PONTOS_JOGADOR);
        pontosDispositivo = estado.getInt(ESTADO_PONTOS_DISPOSITIVO);
        atualizarPlacar();
        int jogador = estado.getInt(ESTADO_JOGADA_JOGADOR, SEM_JOGADA);
        int dispositivo = estado.getInt(ESTADO_JOGADA_DISPOSITIVO, SEM_JOGADA);
        if (jogador != SEM_JOGADA && dispositivo != SEM_JOGADA) {
            ultimaJogadaJogador = Jogada.values()[jogador];
            ultimaJogadaDispositivo = Jogada.values()[dispositivo];
            mostrarEscolhas(ultimaJogadaJogador, ultimaJogadaDispositivo, false);
            mostrarMensagemRodada(ultimaJogadaJogador.compararCom(ultimaJogadaDispositivo), false);
        }
        if (partidaTerminou()) {
            mostrarResultadoFinal(false);
        }
    }

    private void jogar(@NonNull Jogada jogadaJogador) {
        if (partidaTerminou()) {
            return;
        }
        Jogada jogadaDispositivo = Jogada.sortear(random);
        ultimaJogadaJogador = jogadaJogador;
        ultimaJogadaDispositivo = jogadaDispositivo;

        int resultado = jogadaJogador.compararCom(jogadaDispositivo);
        if (resultado == Jogada.VITORIA) {
            pontosJogador++;
            Sons.tocar(somVitoria);
        } else if (resultado == Jogada.DERROTA) {
            pontosDispositivo++;
            Sons.tocar(somDerrota);
        } else {
            Sons.tocar(somEmpate); // Empates não somam pontos.
        }

        mostrarEscolhas(jogadaJogador, jogadaDispositivo, true);
        mostrarMensagemRodada(resultado, true);
        atualizarPlacar();
        if (partidaTerminou()) {
            mostrarResultadoFinal(true);
        }
    }

    private boolean partidaTerminou() {
        return pontosJogador >= VITORIAS_PARA_VENCER || pontosDispositivo >= VITORIAS_PARA_VENCER;
    }

    private void novaPartida() {
        pontosJogador = 0;
        pontosDispositivo = 0;
        ultimaJogadaJogador = null;
        ultimaJogadaDispositivo = null;
        atualizarPlacar();

        binding.imgEscolhaJogador.setImageResource(R.drawable.img_pendente);
        binding.imgEscolhaJogador.setContentDescription(getString(R.string.cd_escolha_pendente));
        binding.imgEscolhaDispositivo.setImageResource(R.drawable.img_pendente);
        binding.imgEscolhaDispositivo.setContentDescription(getString(R.string.cd_escolha_pendente));
        binding.tvEscolhaJogador.setText(null);
        binding.tvEscolhaDispositivo.setText(null);
        binding.tvResultadoRodada.setText(R.string.aguardando_jogada);
        binding.tvResultadoRodada.setTextColor(ContextCompat.getColor(this, R.color.texto_claro));
        binding.tvResultadoPartida.animate().cancel();
        binding.tvResultadoPartida.setVisibility(View.GONE);
        habilitarJogadas(true);
    }

    private void atualizarPlacar() {
        binding.tvPlacar.setText(getString(R.string.placar, pontosJogador, pontosDispositivo));
    }

    private void habilitarJogadas(boolean habilitar) {
        binding.btnPedra.setEnabled(habilitar);
        binding.btnPapel.setEnabled(habilitar);
        binding.btnTesoura.setEnabled(habilitar);
        float alfa = habilitar ? 1f : 0.4f;
        binding.layoutJogadas.setAlpha(alfa);
    }

    /** Mostra as duas escolhas: a do jogador cresce; a do dispositivo "vira a carta". */
    private void mostrarEscolhas(@NonNull Jogada jogador, @NonNull Jogada dispositivo, boolean animar) {
        String nomeJogador = getString(jogador.getNomeResId());
        String nomeDispositivo = getString(dispositivo.getNomeResId());

        binding.imgEscolhaJogador.setImageResource(jogador.getImagemResId());
        binding.imgEscolhaJogador.setContentDescription(getString(R.string.cd_escolha_jogador, nomeJogador));
        binding.tvEscolhaJogador.setText(nomeJogador);
        binding.imgEscolhaDispositivo.setImageResource(dispositivo.getImagemResId());
        binding.imgEscolhaDispositivo.setContentDescription(
                getString(R.string.cd_escolha_dispositivo, nomeDispositivo));
        binding.tvEscolhaDispositivo.setText(nomeDispositivo);

        if (!animar) {
            return;
        }
        View imgJogador = binding.imgEscolhaJogador;
        imgJogador.animate().cancel();
        imgJogador.setAlpha(0f);
        imgJogador.setScaleX(ESCALA_INICIAL);
        imgJogador.setScaleY(ESCALA_INICIAL);
        imgJogador.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(DURACAO_ESCOLHA_JOGADOR_MS)
                .setInterpolator(new OvershootInterpolator())
                .start();

        View imgDispositivo = binding.imgEscolhaDispositivo;
        imgDispositivo.animate().cancel();
        imgDispositivo.setAlpha(0f);
        imgDispositivo.setRotationY(GIRO_INICIAL_DISPOSITIVO);
        imgDispositivo.animate()
                .alpha(1f)
                .rotationY(0f)
                .setDuration(DURACAO_ESCOLHA_DISPOSITIVO_MS)
                .setInterpolator(new DecelerateInterpolator())
                .start();
    }

    private void mostrarMensagemRodada(int resultado, boolean animar) {
        @StringRes int mensagem;
        @ColorRes int cor;
        if (resultado == Jogada.VITORIA) {
            mensagem = R.string.rodada_vitoria;
            cor = R.color.resultado_vitoria;
        } else if (resultado == Jogada.DERROTA) {
            mensagem = R.string.rodada_derrota;
            cor = R.color.resultado_derrota;
        } else {
            mensagem = R.string.rodada_empate;
            cor = R.color.resultado_empate;
        }
        binding.tvResultadoRodada.setText(mensagem);
        binding.tvResultadoRodada.setTextColor(ContextCompat.getColor(this, cor));

        if (!animar) {
            return;
        }
        View texto = binding.tvResultadoRodada;
        texto.animate().cancel();
        texto.setAlpha(0f);
        texto.setTranslationY(getResources().getDimension(R.dimen.deslocamento_animacao));
        texto.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(DURACAO_MENSAGEM_MS)
                .setInterpolator(new DecelerateInterpolator())
                .start();
    }

    /** Placar e resultado final ficam nesta mesma Activity. */
    private void mostrarResultadoFinal(boolean animar) {
        habilitarJogadas(false);
        boolean venceu = pontosJogador > pontosDispositivo;
        binding.tvResultadoPartida.setText(getString(
                venceu ? R.string.partida_vitoria : R.string.partida_derrota,
                pontosJogador, pontosDispositivo));
        binding.tvResultadoPartida.setTextColor(ContextCompat.getColor(this,
                venceu ? R.color.resultado_vitoria : R.color.resultado_derrota));
        binding.tvResultadoPartida.setVisibility(View.VISIBLE);

        View resultado = binding.tvResultadoPartida;
        resultado.animate().cancel();
        if (!animar) {
            resultado.setAlpha(1f);
            resultado.setScaleX(1f);
            resultado.setScaleY(1f);
            return;
        }
        resultado.setAlpha(0f);
        resultado.setScaleX(ESCALA_INICIAL);
        resultado.setScaleY(ESCALA_INICIAL);
        resultado.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(DURACAO_RESULTADO_FINAL_MS)
                .setInterpolator(new OvershootInterpolator())
                .start();
    }
}
