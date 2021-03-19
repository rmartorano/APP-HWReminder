package com.cursoandroid.app_hwreminder.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.cursoandroid.app_hwreminder.Date;
import com.cursoandroid.app_hwreminder.R;
import com.cursoandroid.app_hwreminder.config.ConfiguracaoFirebase;
import com.cursoandroid.app_hwreminder.model.Aluno;
import com.cursoandroid.app_hwreminder.model.Tarefa;
import com.cursoandroid.app_hwreminder.ui.home.HomeFragment;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

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
        //atualiza seleção das checkBoxes
        Date date = new Date();
        String week = date.getWeekIntervalAsChildString();
        String month = date.getMonthString();
        String year = date .getYearString();
        ConfiguracaoFirebase.getFirebaseDatabase().child("aluno").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(!(Boolean) snapshot.child(aluno.getNome()).child("frequencia").child(year).child(month).child(week).child("checkedBoxSegunda").getValue())
                    holder.checkBoxSegunda.setChecked(false);
                if(!(Boolean) snapshot.child(aluno.getNome()).child("frequencia").child(year).child(month).child(week).child("checkedBoxTerca").getValue())
                    holder.checkBoxTerca.setChecked(false);
                if(!(Boolean) snapshot.child(aluno.getNome()).child("frequencia").child(year).child(month).child(week).child("checkedBoxQuarta").getValue())
                    holder.checkBoxQuarta.setChecked(false);
                if(!(Boolean) snapshot.child(aluno.getNome()).child("frequencia").child(year).child(month).child(week).child("checkedBoxQuinta").getValue())
                    holder.checkBoxQuinta.setChecked(false);
                if(!(Boolean) snapshot.child(aluno.getNome()).child("frequencia").child(year).child(month).child(week).child("checkedBoxSexta").getValue())
                    holder.checkBoxSexta.setChecked(false);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
        //add um listener pra cada checkBox
        holder.checkBoxSegunda.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                aluno.setCheckBoxSegunda(isChecked);
                aluno.salvarCheckBox();
            }
        });
        holder.checkBoxTerca.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                aluno.setCheckBoxTerca(isChecked);
                aluno.salvarCheckBox();
            }
        });
        holder.checkBoxQuarta.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                aluno.setCheckBoxQuarta(isChecked);
                aluno.salvarCheckBox();
            }
        });
        holder.checkBoxQuinta.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                aluno.setCheckBoxQuinta(isChecked);
                aluno.salvarCheckBox();
            }
        });
        holder.checkBoxSexta.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                aluno.setCheckBoxSexta(isChecked);
                aluno.salvarCheckBox();
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
