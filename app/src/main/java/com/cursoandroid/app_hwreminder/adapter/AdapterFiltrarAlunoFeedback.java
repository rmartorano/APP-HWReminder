package com.cursoandroid.app_hwreminder.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.cursoandroid.app_hwreminder.R;
import com.cursoandroid.app_hwreminder.model.Aluno;

import java.util.List;

public class AdapterFiltrarAlunoFeedback extends RecyclerView.Adapter<AdapterFiltrarAlunoFeedback.MyViewHolder> {

    private List<Aluno> listAlunos;
    private Context context;

    public AdapterFiltrarAlunoFeedback(List<Aluno> lista, Context context) {
        this.listAlunos = lista;
        this.context = context;
    }

    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View itemLista = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.adapter_filtrar_aluno_feedback, parent, false);
        return new MyViewHolder(itemLista);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        Aluno aluno = listAlunos.get(position);
        holder.nome.setText(aluno.getNome());
        holder.nome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("Teste","Nome clicado: "+aluno.getNome());
            }
        });
    }

    @Override
    public int getItemCount() {
        return listAlunos.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{

        TextView nome;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            nome = itemView.findViewById(R.id.textViewNomeAlunoFiltro);

        }
    }

}
