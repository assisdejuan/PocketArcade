package com.example.pocketarcade;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.pocketarcade.databinding.ActivityCentralDesafiosBinding;
import com.example.pocketarcade.util.Extras;
import com.example.pocketarcade.util.Navegacao;
import com.example.pocketarcade.util.TelaUtil;
import com.example.pocketarcade.util.Validacao;

/**
 * Tela 2 · Central de Desafios: menu rolável com quatro painéis.
 * A entrada em cascata dos painéis vem de android:layoutAnimation (res/anim).
 */
public class CentralDesafiosActivity extends AppCompatActivity {

    private static final String TAG = "CentralDesafiosActivity";

    private ActivityCentralDesafiosBinding binding;
    private String apelido;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TelaUtil.ativarTelaCheia(this);
        binding = ActivityCentralDesafiosBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Log.d(TAG, "onCreate");
        TelaUtil.aplicarEspacamentoDasBarras(binding.getRoot());

        apelido = getIntent().getStringExtra(Extras.APELIDO);
        if (!Validacao.apelidoValido(apelido)) {
            Toast.makeText(this, R.string.erro_dados_sessao, Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        binding.tvSaudacao.setText(getString(R.string.saudacao, apelido));

        binding.btnAbrirScanner.setOnClickListener(view ->
                Navegacao.abrirDesafio(this, ScannerMensagensActivity.class, apelido));
        binding.btnAbrirArena.setOnClickListener(view ->
                Navegacao.abrirDesafio(this, ArenaActivity.class, apelido));
        binding.btnAbrirCofre.setOnClickListener(view ->
                Navegacao.abrirDesafio(this, CodigoSecretoActivity.class, apelido));
        binding.btnAbrirRandomizador.setOnClickListener(view ->
                Navegacao.abrirDesafio(this, RandomizadorActivity.class, apelido));
        binding.btnSairCentral.setOnClickListener(view -> finish());
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
}
