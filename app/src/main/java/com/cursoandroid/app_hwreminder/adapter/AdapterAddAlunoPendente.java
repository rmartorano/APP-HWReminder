package com.cursoandroid.app_hwreminder.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.cursoandroid.app_hwreminder.R;
import com.cursoandroid.app_hwreminder.model.Aluno;
import com.cursoandroid.app_hwreminder.model.AlunoAddPendente;
import com.cursoandroid.app_hwreminder.model.Tarefa;

import java.util.List;

public class AdapterAddAlunoPendente extends RecyclerView.Adapter<AdapterAddAlunoPendente.MyViewHolder> {

    List<AlunoAddPendente> alunos;
    Context context;

    public AdapterAddAlunoPendente(List<AlunoAddPendente> alunos, Context context) {
        this.alunos = alunos;
        this.context = context;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemLista = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_aluno_pendente, parent, false);
        return new MyViewHolder(itemLista);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        AlunoAddPendente aluno = alunos.get(position);

        holder.nome.setText(aluno.getNome());

    }


    @Override
    public int getItemCount() {
        return alunos.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        TextView nome;

        public MyViewHolder(View itemView) {
            super(itemView);

            nome = itemView.findViewById(R.id.textAdapterAlunoPendenteNome);
        }

    }

}
