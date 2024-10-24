package com.reciclamais.activity;


import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import androidx.activity.EdgeToEdge;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.reciclamais.R;
import com.reciclamais.adapter.ProdutoAdapter;
import com.reciclamais.model.Produto;

import android.view.View;

import java.util.ArrayList;
import java.util.List;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class MainActivity extends AppCompatActivity {
    private RecyclerView recyclerProdutos;
    private List<Produto> produtos = new ArrayList<>();
    private BottomNavigationView bottomNavigationView;
    private ProdutoAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        recyclerProdutos = findViewById(R.id.recyclerProdutos);
        bottomNavigationView = findViewById(R.id.bottomNavigationView);

        // Configurar cor statusbar
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.cinza));

        // Definir layout
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerProdutos.setLayoutManager(layoutManager);

        // Define adapter
        adapter = new ProdutoAdapter(produtos, this);
        recyclerProdutos.setAdapter(adapter);

        this.prepararProdutos();
    }

    // Métodos de filtro
    public void filtrarTudo(View view) {
        adapter.filtrarPorTag("tudo");
    }

    public void filtrarPapel(View view) {
        adapter.filtrarPorTag("papel");
    }

    public void filtrarPlastico(View view) {
        adapter.filtrarPorTag("plastico");
    }

    public void filtrarVidro(View view) {
        adapter.filtrarPorTag("vidro");
    }

    public void filtrarMetal(View view) {
        adapter.filtrarPorTag("metal");
    }

    //Metodos de ordenacao
    public void ordenaDificuldade(View view){
        adapter.ordenaPorDificul();
    }
    public void prepararProdutos() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("produtos");

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                produtos.clear();
                for (DataSnapshot produtoSnapshot : dataSnapshot.getChildren()) {
                    String nome = produtoSnapshot.child("nome").getValue(String.class);
                    String nivel = produtoSnapshot.child("nivel").getValue(String.class);
                    String imagem = produtoSnapshot.child("imagem").getValue(String.class);

                    List<String> passos = new ArrayList<>();
                    for (DataSnapshot passoSnapshot : produtoSnapshot.child("passos").getChildren()) {
                        passos.add(passoSnapshot.getValue(String.class));
                    }

                    List<String> materiais = new ArrayList<>();
                    for (DataSnapshot materialSnapshot : produtoSnapshot.child("materiais").getChildren()) {
                        materiais.add(materialSnapshot.getValue(String.class));
                    }

                    List<String> tags = new ArrayList<>();
                    for (DataSnapshot tagSnapshot : produtoSnapshot.child("tags").getChildren()) {
                        tags.add(tagSnapshot.getValue(String.class));
                    }

                    int imagemId = getResources().getIdentifier(imagem, "drawable", getPackageName());

                    Produto produto = new Produto(nome, nivel, imagemId, passos, materiais, tags);
                    produtos.add(produto);
                }

                adapter.atualizarLista(produtos);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("Firebase", "Erro ao carregar dados", databaseError.toException());
            }
        });
    }
}