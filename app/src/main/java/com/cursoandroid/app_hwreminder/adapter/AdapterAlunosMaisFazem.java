package com.cursoandroid.app_hwreminder.adapter;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.cursoandroid.app_hwreminder.R;
import com.cursoandroid.app_hwreminder.model.Aluno;
import com.cursoandroid.app_hwreminder.model.Tarefa;

import java.util.List;

public class AdapterAlunosMaisFazem extends RecyclerView.Adapter<AdapterAlunosMaisFazem.MyViewHolder> {

    private List<Aluno> listAlunos;
    private List<Tarefa> listTarefas;
    private Context context;

    public AdapterAlunosMaisFazem(List<Aluno> listaAlunos, List<Tarefa> listaTarefas, Context context) {
        this.listAlunos = listaAlunos;
        this.listTarefas = listaTarefas;
        this.context = context;
    }

    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View itemLista = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.adapter_alunos_mais_fazem, parent, false);
        return new MyViewHolder(itemLista);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        Aluno aluno = listAlunos.get(position);
        holder.nome.setText(aluno.getNome());
        holder.fotoPerfil.setImageResource(aluno.getFotoPerfil());
        holder.progressBarFez.setMax(listTarefas.size());
        holder.progressBarNaoFez.setMax(listTarefas.size());
        holder.progressBarNaoFez.setProgressTintList(ColorStateList.valueOf(Color.RED));
        holder.progressBarFez.setProgress(0);
        holder.progressBarNaoFez.setProgress(0);

        for(Tarefa tarefa : listTarefas){
            if(tarefa.getListAlunosFizeram().contains(aluno.getNome()))
                holder.progressBarFez.setProgress(holder.progressBarFez.getProgress()+1);
            //deixar esse else if pois pode ter aluno que entrou dps e não queremos contar tarefas de quando ele não estava presente ainda
            else if(tarefa.getListAlunosNaoFizeram().contains(aluno.getNome()))
                holder.progressBarNaoFez.setProgress(holder.progressBarNaoFez.getProgress()+1);
        }

        aluno.setQtdProgressBarTarefasFeitas(holder.progressBarFez.getProgress());
        aluno.setQtdProgressBarTarefasNaoFeitas(holder.progressBarNaoFez.getProgress());

        holder.qtdFez.setText(""+holder.progressBarFez.getProgress());
        holder.qtdNaoFez.setText(""+holder.progressBarNaoFez.getProgress());
        holder.total1.setText("/"+listTarefas.size());
        holder.total2.setText("/"+listTarefas.size());

    }

    @Override
    public int getItemCount() {
        return listAlunos.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{

        TextView nome, qtdFez, qtdNaoFez, total1, total2;
        ProgressBar progressBarFez, progressBarNaoFez;
        ImageView fotoPerfil;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            nome = itemView.findViewById(R.id.textViewNomeMaisFazem);
            fotoPerfil = itemView.findViewById(R.id.roundedImageViewAdapterAlunosMaisFazem);
            qtdFez = itemView.findViewById(R.id.textViewPBarFez);
            qtdNaoFez = itemView.findViewById(R.id.textViewPBarNaoFez);
            progressBarFez = itemView.findViewById(R.id.progressBarMaisFazemFez);
            progressBarNaoFez = itemView.findViewById(R.id.progressBarMaisFazemNaoFez);
            total1 = itemView.findViewById(R.id.textViewTotalTarefasAlunosMaisFizeram1);
            total2 = itemView.findViewById(R.id.textViewTotalTarefasAlunosMaisFizeram2);

        }
    }

}
