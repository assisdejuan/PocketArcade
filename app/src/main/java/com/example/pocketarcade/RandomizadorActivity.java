package com.example.pocketarcade;

import android.content.res.TypedArray;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.OvershootInterpolator;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.os.BundleCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.pocketarcade.adapter.PalavraAdapter;
import com.example.pocketarcade.databinding.ActivityRandomizadorBinding;
import com.example.pocketarcade.model.Palavra;
import com.example.pocketarcade.util.Extras;
import com.example.pocketarcade.util.Navegacao;
import com.example.pocketarcade.util.Sons;
import com.example.pocketarcade.util.TelaUtil;
import com.example.pocketarcade.util.Validacao;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/** Tela 6 · Randomizador de Palavras: cadastra por categoria e sorteia com RecyclerView. */
public class RandomizadorActivity extends AppCompatActivity implements PalavraAdapter.OnPalavraListener {

    private static final String TAG = "RandomizadorActivity";

    private static final String ESTADO_PALAVRAS = "estado_palavras";
    private static final String ESTADO_MODO_GRADE = "estado_modo_grade";
    private static final String ESTADO_SORTEADA = "estado_sorteada";

    private static final int COLUNAS_GRADE = 2;
    private static final int NENHUMA = -1;
    private static final long DURACAO_TROCA_LAYOUT_MS = 400L;
    private static final long DURACAO_SORTEIO_MS = 550L;
    private static final float ESCALA_INICIAL_SORTEIO = 0.3f;
    private static final float GIRO_DADO = -360f;

    private ActivityRandomizadorBinding binding;
    private final Random random = new Random();

    /** Criado uma única vez no onCreate() e reutilizado a cada sorteio. */
    @Nullable
    private MediaPlayer somSorteio;

    private ArrayList<Palavra> palavras;
    private PalavraAdapter adapter;
    private boolean modoGrade;
    private String[] categorias;
    private int[] iconesCategorias;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TelaUtil.ativarTelaCheia(this);
        binding = ActivityRandomizadorBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Log.d(TAG, "onCreate");
        TelaUtil.aplicarEspacamentoDasBarras(binding.getRoot());

        String apelido = getIntent().getStringExtra(Extras.APELIDO);
        if (!Validacao.apelidoValido(apelido)) {
            Toast.makeText(this, R.string.erro_dados_sessao, Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        somSorteio = MediaPlayer.create(this, R.raw.som_sorteio);

        binding.toolbarRandomizador.setSubtitle(getString(R.string.jogador_atual, apelido));
        binding.toolbarRandomizador.setNavigationOnClickListener(view -> finish());

        categorias = getResources().getStringArray(R.array.categorias_sorteio);
        iconesCategorias = carregarIconesCategorias();
        restaurarOuCriarLista(savedInstanceState);

        configurarSpinner();
        adapter = new PalavraAdapter(palavras, this);
        binding.rvPalavras.setAdapter(adapter);
        aplicarLayoutManager();

        binding.btnAdicionar.setOnClickListener(view -> adicionar());
        binding.btnSortear.setOnClickListener(view -> sortear());
        binding.btnAlternarLayout.setOnClickListener(view -> alternarListaGrade());
        binding.barraNavegacao.btnVoltarCentral.setOnClickListener(view -> finish());
        binding.barraNavegacao.btnEncerrarSessao.setOnClickListener(view ->
                Navegacao.encerrarSessao(this, R.string.modulo_randomizador));

        if (savedInstanceState != null) {
            int sorteada = savedInstanceState.getInt(ESTADO_SORTEADA, NENHUMA);
            if (sorteada != NENHUMA && sorteada < palavras.size()) {
                mostrarSorteio(palavras.get(sorteada), false);
            }
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
        Sons.pausar(somSorteio);
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop");
    }

    /** release() libera os recursos nativos e leva o player ao estado End. */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
        if (somSorteio != null) {
            somSorteio.release();
            somSorteio = null;
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(ESTADO_PALAVRAS, palavras);
        outState.putBoolean(ESTADO_MODO_GRADE, modoGrade);
        Palavra destacada = adapter.getDestacada();
        outState.putInt(ESTADO_SORTEADA, destacada == null ? NENHUMA : palavras.indexOf(destacada));
    }

    @SuppressWarnings("unchecked")
    private void restaurarOuCriarLista(@Nullable Bundle estado) {
        ArrayList<Palavra> salvas = null;
        if (estado != null) {
            salvas = BundleCompat.getSerializable(estado, ESTADO_PALAVRAS, ArrayList.class);
            modoGrade = estado.getBoolean(ESTADO_MODO_GRADE, false);
        }
        palavras = salvas != null ? salvas : criarItensIniciais();
    }

    private int[] carregarIconesCategorias() {
        TypedArray icones = getResources().obtainTypedArray(R.array.icones_categorias);
        int[] ids = new int[icones.length()];
        for (int i = 0; i < ids.length; i++) {
            ids[i] = icones.getResourceId(i, R.drawable.ic_dado);
        }
        icones.recycle();
        return ids;
    }

    private ArrayList<Palavra> criarItensIniciais() {
        ArrayList<Palavra> iniciais = new ArrayList<>();
        TypedArray itensPorCategoria = getResources().obtainTypedArray(R.array.itens_por_categoria);
        for (int categoria = 0; categoria < itensPorCategoria.length(); categoria++) {
            int arrayId = itensPorCategoria.getResourceId(categoria, 0);
            if (arrayId == 0) {
                continue;
            }
            for (String texto : getResources().getStringArray(arrayId)) {
                iniciais.add(new Palavra(texto, categorias[categoria], iconesCategorias[categoria]));
            }
        }
        itensPorCategoria.recycle();
        return iniciais;
    }

    private void configurarSpinner() {
        ArrayAdapter<CharSequence> adapterCategorias = ArrayAdapter.createFromResource(
                this, R.array.categorias_sorteio, R.layout.item_spinner);
        adapterCategorias.setDropDownViewResource(R.layout.item_spinner_dropdown);
        binding.spCategoria.setAdapter(adapterCategorias);
    }

    private void aplicarLayoutManager() {
        if (modoGrade) {
            binding.rvPalavras.setLayoutManager(new GridLayoutManager(this, COLUNAS_GRADE));
            binding.btnAlternarLayout.setIconResource(R.drawable.ic_lista);
            binding.btnAlternarLayout.setText(R.string.botao_lista);
            binding.btnAlternarLayout.setContentDescription(getString(R.string.cd_alternar_lista));
        } else {
            binding.rvPalavras.setLayoutManager(new LinearLayoutManager(this));
            binding.btnAlternarLayout.setIconResource(R.drawable.ic_grade);
            binding.btnAlternarLayout.setText(R.string.botao_grade);
            binding.btnAlternarLayout.setContentDescription(getString(R.string.cd_alternar_grade));
        }
        adapter.setModoGrade(modoGrade);
    }

    private void alternarListaGrade() {
        modoGrade = !modoGrade;
        aplicarLayoutManager();
        binding.rvPalavras.setAlpha(0f);
        binding.rvPalavras.animate()
                .alpha(1f)
                .setDuration(DURACAO_TROCA_LAYOUT_MS)
                .start();
    }

    private void adicionar() {
        String valor = Validacao.textoDe(binding.etValor.getText());
        if (!Validacao.temMinimo(valor, Validacao.MINIMO_PALAVRA)) {
            binding.tilValor.setError(getString(R.string.erro_valor));
            binding.tilValor.startAnimation(AnimationUtils.loadAnimation(this, R.anim.tremor_erro));
            return;
        }
        binding.tilValor.setError(null);

        int categoria = binding.spCategoria.getSelectedItemPosition();
        Palavra nova = new Palavra(valor, categorias[categoria], iconesCategorias[categoria]);
        palavras.add(nova);
        int posicao = palavras.size() - 1;
        adapter.notifyItemInserted(posicao);
        binding.rvPalavras.smoothScrollToPosition(posicao);

        binding.etValor.setText(null);
        TelaUtil.esconderTeclado(this);
        Toast.makeText(this, getString(R.string.msg_item_adicionado, valor, categorias[categoria]),
                Toast.LENGTH_SHORT).show();
    }

    /** Sorteia apenas entre os itens da categoria selecionada no Spinner. */
    private void sortear() {
        String categoria = categorias[binding.spCategoria.getSelectedItemPosition()];
        List<Palavra> candidatas = new ArrayList<>();
        for (Palavra palavra : palavras) {
            if (palavra.getCategoria().equals(categoria)) {
                candidatas.add(palavra);
            }
        }
        if (candidatas.isEmpty()) {
            Toast.makeText(this, getString(R.string.msg_categoria_vazia, categoria), Toast.LENGTH_SHORT).show();
            binding.cardSorteio.startAnimation(AnimationUtils.loadAnimation(this, R.anim.tremor_erro));
            return;
        }
        Palavra sorteada = candidatas.get(random.nextInt(candidatas.size()));
        Sons.tocar(somSorteio);
        mostrarSorteio(sorteada, true);
        binding.rvPalavras.smoothScrollToPosition(palavras.indexOf(sorteada));
    }

    private void mostrarSorteio(@NonNull Palavra sorteada, boolean animar) {
        adapter.destacar(sorteada);
        binding.tvTituloSorteio.setText(getString(R.string.sorteio_titulo, sorteada.getCategoria()));
        binding.tvItemSorteado.setText(sorteada.getTexto());
        binding.tvItemSorteado.setVisibility(View.VISIBLE);
        binding.imgCategoriaSorteada.setImageResource(sorteada.getIconeCategoriaResId());
        binding.imgCategoriaSorteada.setContentDescription(
                getString(R.string.cd_icone_categoria, sorteada.getCategoria()));
        if (!animar) {
            return;
        }
        View item = binding.tvItemSorteado;
        item.animate().cancel();
        item.setAlpha(0f);
        item.setScaleX(ESCALA_INICIAL_SORTEIO);
        item.setScaleY(ESCALA_INICIAL_SORTEIO);
        item.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(DURACAO_SORTEIO_MS)
                .setInterpolator(new OvershootInterpolator())
                .start();

        View icone = binding.imgCategoriaSorteada;
        icone.animate().cancel();
        icone.setRotation(GIRO_DADO);
        icone.animate()
                .rotation(0f)
                .setDuration(DURACAO_SORTEIO_MS)
                .start();
    }

    /** Clique longo exclui o item com notifyItemRemoved(). */
    @Override
    public void aoClicarLongo(@NonNull Palavra palavra) {
        int posicao = palavras.indexOf(palavra);
        if (posicao == -1) {
            return;
        }
        if (palavra == adapter.getDestacada()) {
            adapter.destacar(null);
        }
        palavras.remove(posicao);
        adapter.notifyItemRemoved(posicao);
        Toast.makeText(this, getString(R.string.msg_item_removido, palavra.getTexto()),
                Toast.LENGTH_SHORT).show();
    }
}
