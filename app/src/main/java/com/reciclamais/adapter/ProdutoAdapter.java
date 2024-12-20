package com.reciclamais.adapter;

import android.content.Context;
import android.content.Intent;
import android.media.Image;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.reciclamais.R;
import com.reciclamais.activity.ProdutoDetalhadoActivity;
import com.reciclamais.model.Produto;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ProdutoAdapter extends RecyclerView.Adapter<ProdutoAdapter.MyViewHolder> {
    private List<Produto> produtos;
    private List<Produto> produtosFiltrados;
    private Context context;


    public ProdutoAdapter(List<Produto> listaProdutos, Context context) {
        this.produtos = (listaProdutos != null) ? listaProdutos : new ArrayList<>();
        this.produtosFiltrados = new ArrayList<>(this.produtos);
        this.context = context;
    }

    public void filtrarPorTag(String tag) {
        produtosFiltrados.clear();
        if (tag.equals("tudo")) {
            produtosFiltrados.addAll(produtos);
        } else {
            for (Produto produto : produtos) {
                if (produto.getTags() != null && produto.getTags().contains(tag.toLowerCase())) {
                    produtosFiltrados.add(produto);
                }
            }
        }
        notifyDataSetChanged();
    }

    public void ordenaPorDificul(){
        // Define a ordem de dificuldade
        Comparator<Produto> comparator = new Comparator<Produto>() {
            @Override
            public int compare(Produto p1, Produto p2) {
                return getDificuldadeValue(p1.getNivel()) - getDificuldadeValue(p2.getNivel());
            }
        };

        // Ordena a lista de produtos filtrados
        Collections.sort(produtosFiltrados, comparator);
        notifyDataSetChanged();

    }

    public void ordenaPorMediaAvaliacoes() {
        Collections.sort(produtosFiltrados, (p1, p2) -> {
            double media1 = p1.getMedia_avaliacoes();
            double media2 = p2.getMedia_avaliacoes();
            return Double.compare(media2, media1); // Ordenação decrescente
        });
        notifyDataSetChanged();
    }


    private int getDificuldadeValue(String nivel) {
        switch (nivel.toLowerCase()) {
            case "fácil":
                return 1;
            case "médio":
                return 2;
            case "difícil":
                return 3;
            default:
                return 0; // Caso não haja um nível definido, coloque no final
        }
    }

    public void atualizarLista(List<Produto> novosProdutos) {
        this.produtos = novosProdutos;
        this.produtosFiltrados = new ArrayList<>(this.produtos);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ProdutoAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemLista = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_produto, parent, false);
        return new MyViewHolder(itemLista);
    }

    @Override
    public void onBindViewHolder(@NonNull ProdutoAdapter.MyViewHolder holder, int position) {
        Produto produto = produtosFiltrados.get(position);
        holder.textNome.setText(produto.getNome());
        holder.textNivel.setText(produto.getNivel());


        // Carregar a imagem com Glide a partir da URL
        Glide.with(context)
                .load(produto.getImagem())
                .placeholder(R.drawable.loading) // Imagem de placeholder enquanto carrega
                .error(R.drawable.error_image) // Imagem de erro caso falhe o carregamento
                .into(holder.imagemProduto);

        // Definir o ícone de favorito
        if (produto.isFavoritado()) {
            holder.imagemFavoritar.setImageResource(R.drawable.ic_heart_filled); // Ícone preenchido
        } else {
            holder.imagemFavoritar.setImageResource(R.drawable.ic_heart_outline); // Ícone vazio
        }

        // Adicionar listener para o botão de favoritar
        holder.imagemFavoritar.setOnClickListener(v -> {
            boolean novoEstadoFavorito = !produto.isFavoritado();
            produto.setFavoritado(novoEstadoFavorito);

            // Atualiza o estado no Firebase
            DatabaseReference produtoRef = FirebaseDatabase.getInstance()
                    .getReference()
                    .child("produtos")
                    .child(produto.getKey());

            produtoRef.child("favoritado").setValue(novoEstadoFavorito)
                    .addOnSuccessListener(aVoid -> {
                        // Atualiza o ícone
                        if (novoEstadoFavorito) {
                            holder.imagemFavoritar.setImageResource(R.drawable.ic_heart_filled);
                        } else {
                            holder.imagemFavoritar.setImageResource(R.drawable.ic_heart_outline);
                        }
                        notifyItemChanged(position);
                    })
                    .addOnFailureListener(e -> {
                        // Reverte o estado em caso de falha
                        produto.setFavoritado(!novoEstadoFavorito);
                        notifyItemChanged(position);
                    });
        });


        // Configurar o clique para abrir detalhes do produto
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ProdutoDetalhadoActivity.class);
            intent.putExtra("produtoKey", produto.getKey()); // Adicione a chave do produto
            intent.putExtra("nome", produto.getNome());
            intent.putExtra("nivel", produto.getNivel());
            intent.putExtra("imagem", produto.getImagem());
            intent.putStringArrayListExtra("passos", new ArrayList<>(produto.getPassos()));
            intent.putStringArrayListExtra("materiais", new ArrayList<>(produto.getMateriais()));
            intent.putStringArrayListExtra("tags", new ArrayList<>(produto.getTags()));
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return produtosFiltrados.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        ImageView imagemProduto, imagemFavoritar;
        TextView textNome, textNivel;


        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            textNome = itemView.findViewById(R.id.textNome);
            textNivel = itemView.findViewById(R.id.textNivel);
            imagemProduto = itemView.findViewById(R.id.imageProduto);
            imagemFavoritar = itemView.findViewById(R.id.btnFavoritar);
        }
    }

    public void filtrarPorNome(String texto) {
        produtosFiltrados.clear();
        if (texto.isEmpty()) {
            produtosFiltrados.addAll(produtos);
        } else {
            String textoLowerCase = texto.toLowerCase();
            for (Produto produto : produtos) {
                if (produto.getNome().toLowerCase().contains(textoLowerCase)) {
                    produtosFiltrados.add(produto);
                }
            }
        }
        notifyDataSetChanged();
    }
}