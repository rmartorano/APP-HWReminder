package com.cursoandroid.app_hwreminder.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.cursoandroid.app_hwreminder.R;
import com.cursoandroid.app_hwreminder.config.ConfiguracaoFirebase;
import com.cursoandroid.app_hwreminder.config.Date;
import com.cursoandroid.app_hwreminder.model.Aluno;
import com.cursoandroid.app_hwreminder.model.Tarefa;
import com.cursoandroid.app_hwreminder.ui.aluno.InfoAlunoActivity;
import com.cursoandroid.app_hwreminder.ui.home.HomeFragment;
import com.github.mikephil.charting.charts.PieChart;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

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
        holder.progressBarFez.setMax(listTarefas.size());
        holder.progressBarNaoFez.setMax(listTarefas.size());
        holder.progressBarNaoFez.setProgressTintList(ColorStateList.valueOf(Color.RED));

        for(Tarefa tarefa : listTarefas){
            if(tarefa.getListAlunosFizeram().contains(aluno.getNome()))
                holder.progressBarFez.setProgress(holder.progressBarFez.getProgress()+1);
            //deixar esse else if pois pode ter aluno que entrou dps e não queremos contar tarefas de quando ele não estava presente ainda
            else if(tarefa.getListAlunosNaoFizeram().contains(aluno.getNome()))
                holder.progressBarNaoFez.setProgress(holder.progressBarNaoFez.getProgress()+1);
        }

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

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            nome = itemView.findViewById(R.id.textViewNomeMaisFazem);
            qtdFez = itemView.findViewById(R.id.textViewPBarFez);
            qtdNaoFez = itemView.findViewById(R.id.textViewPBarNaoFez);
            progressBarFez = itemView.findViewById(R.id.progressBarMaisFazemFez);
            progressBarNaoFez = itemView.findViewById(R.id.progressBarMaisFazemNaoFez);
            total1 = itemView.findViewById(R.id.textViewTotal1);
            total2 = itemView.findViewById(R.id.textViewTotal2);

        }
    }

}
