package com.example.pocketarcade.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pocketarcade.R;
import com.example.pocketarcade.databinding.ItemPalavraGradeBinding;
import com.example.pocketarcade.databinding.ItemPalavraListaBinding;
import com.example.pocketarcade.model.Palavra;
import com.google.android.material.card.MaterialCardView;

import java.util.List;

/** Adapter do Randomizador: fornece as views; o ViewHolder as guarda para reutilização. */
public class PalavraAdapter extends RecyclerView.Adapter<PalavraAdapter.PalavraViewHolder> {

    public interface OnPalavraListener {
        void aoClicarLongo(@NonNull Palavra palavra);
    }

    private static final int TIPO_LISTA = 0;
    private static final int TIPO_GRADE = 1;

    private final List<Palavra> palavras;
    private final OnPalavraListener listener;
    private boolean modoGrade;
    /** Último item sorteado, destacado com borda amarela. */
    @Nullable
    private Palavra destacada;

    public PalavraAdapter(@NonNull List<Palavra> palavras, @NonNull OnPalavraListener listener) {
        this.palavras = palavras;
        this.listener = listener;
    }

    public void setModoGrade(boolean modoGrade) {
        if (this.modoGrade == modoGrade) {
            return;
        }
        this.modoGrade = modoGrade;
        notifyItemRangeChanged(0, getItemCount());
    }

    public void destacar(@Nullable Palavra palavra) {
        int anterior = destacada == null ? -1 : palavras.indexOf(destacada);
        destacada = palavra;
        if (anterior != -1) {
            notifyItemChanged(anterior);
        }
        int atual = palavra == null ? -1 : palavras.indexOf(palavra);
        if (atual != -1) {
            notifyItemChanged(atual);
        }
    }

    @Nullable
    public Palavra getDestacada() {
        return destacada;
    }

    @Override
    public int getItemViewType(int position) {
        return modoGrade ? TIPO_GRADE : TIPO_LISTA;
    }

    @NonNull
    @Override
    public PalavraViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == TIPO_GRADE) {
            ItemPalavraGradeBinding binding = ItemPalavraGradeBinding.inflate(inflater, parent, false);
            return new PalavraViewHolder(binding.getRoot(),
                    binding.imgCategoria, binding.tvPalavra, binding.tvCategoria);
        }
        ItemPalavraListaBinding binding = ItemPalavraListaBinding.inflate(inflater, parent, false);
        return new PalavraViewHolder(binding.getRoot(),
                binding.imgCategoria, binding.tvPalavra, binding.tvCategoria);
    }

    @Override
    public void onBindViewHolder(@NonNull PalavraViewHolder holder, int position) {
        holder.vincular(palavras.get(position));
    }

    @Override
    public int getItemCount() {
        return palavras.size();
    }

    class PalavraViewHolder extends RecyclerView.ViewHolder {

        private final MaterialCardView card;
        private final ImageView imgCategoria;
        private final TextView tvPalavra;
        private final TextView tvCategoria;

        PalavraViewHolder(@NonNull MaterialCardView card,
                          @NonNull ImageView imgCategoria,
                          @NonNull TextView tvPalavra,
                          @NonNull TextView tvCategoria) {
            super(card);
            this.card = card;
            this.imgCategoria = imgCategoria;
            this.tvPalavra = tvPalavra;
            this.tvCategoria = tvCategoria;

            card.setOnLongClickListener(view -> {
                int posicao = getBindingAdapterPosition();
                if (posicao == RecyclerView.NO_POSITION) {
                    return false;
                }
                listener.aoClicarLongo(palavras.get(posicao));
                return true;
            });
        }

        void vincular(@NonNull Palavra palavra) {
            Context context = itemView.getContext();
            imgCategoria.setImageResource(palavra.getIconeCategoriaResId());
            imgCategoria.setContentDescription(
                    context.getString(R.string.cd_icone_categoria, palavra.getCategoria()));
            tvPalavra.setText(palavra.getTexto());
            tvCategoria.setText(palavra.getCategoria());

            boolean emDestaque = palavra == destacada;
            card.setStrokeColor(ContextCompat.getColor(context,
                    emDestaque ? R.color.neon_amarelo : R.color.borda_painel));
            card.setStrokeWidth(context.getResources().getDimensionPixelSize(
                    emDestaque ? R.dimen.borda_destaque : R.dimen.borda_normal));
        }
    }
}
