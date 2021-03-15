package com.cursoandroid.app_hwreminder.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.cursoandroid.app_hwreminder.R;
import com.cursoandroid.app_hwreminder.model.Aluno;
import com.cursoandroid.app_hwreminder.model.Tarefa;

import java.util.List;

public class AdapterAluno extends RecyclerView.Adapter<AdapterAluno.MyViewHolder> {

    List<Aluno> alunos;
    Context context;

    public AdapterAluno(List<Aluno> alunos, Context context) {
        this.alunos = alunos;
        this.context = context;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemLista = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_aluno, parent, false);
        return new MyViewHolder(itemLista);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        Aluno aluno = alunos.get(position);
        holder.nome.setText(aluno.getNome());
        //add um listener pra cada checkBox
        holder.checkBoxSegunda.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                aluno.setCheckBoxSegunda(isChecked);
                aluno.update();
                Log.i("Teste","Bind view: "+aluno.isCheckedBoxSegunda());
            }
        });
        holder.checkBoxTerca.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                aluno.setCheckBoxTerca(isChecked);
            }
        });
        holder.checkBoxQuarta.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                aluno.setCheckBoxQuarta(isChecked);
            }
        });
        holder.checkBoxQuinta.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                aluno.setCheckBoxQuinta(isChecked);
            }
        });
        holder.checkBoxSexta.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                aluno.setCheckBoxSexta(isChecked);
            }
        });
    }


    @Override
    public int getItemCount() {
        return alunos.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        TextView nome;
        CheckBox checkBoxSegunda, checkBoxTerca, checkBoxQuarta, checkBoxQuinta, checkBoxSexta;

        public MyViewHolder(View itemView) {
            super(itemView);
            nome = itemView.findViewById(R.id.textAdapterAlunoNome);
            checkBoxSegunda = itemView.findViewById(R.id.checkBoxSeg);
            checkBoxTerca = itemView.findViewById(R.id.checkBoxTer);
            checkBoxQuarta = itemView.findViewById(R.id.checkBoxQua);
            checkBoxQuinta = itemView.findViewById(R.id.checkBoxQui);
            checkBoxSexta = itemView.findViewById(R.id.checkBoxSex);
        }

    }

}
