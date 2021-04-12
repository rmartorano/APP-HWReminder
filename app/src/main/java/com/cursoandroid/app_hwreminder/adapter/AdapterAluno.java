package com.cursoandroid.app_hwreminder.adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.cursoandroid.app_hwreminder.config.Date;
import com.cursoandroid.app_hwreminder.R;
import com.cursoandroid.app_hwreminder.config.ConfiguracaoFirebase;
import com.cursoandroid.app_hwreminder.model.Aluno;
import com.cursoandroid.app_hwreminder.model.Tarefa;
import com.cursoandroid.app_hwreminder.ui.aluno.InfoAlunoActivity;
import com.cursoandroid.app_hwreminder.ui.home.HomeFragment;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.util.ArrayList;
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
        DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDatabase();
        Aluno aluno = alunos.get(position);
        holder.nome.setText(aluno.getNome());
        //atualiza seleção das checkBoxes
        Date date = new Date();
        String year = date.getYearString();
        ConfiguracaoFirebase.getFirebaseDatabase()
                .child(ConfiguracaoFirebase.getFirebaseAutenticacao().getCurrentUser().getEmail().replace(".", "-"))
                .child("aluno")
                .child(year)
                .child(aluno.getTurma())
                .child(aluno.getNome()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.child("checkBoxes").child("checkedBoxSegunda").getValue() == null){
                    aluno.salvarCheckBox();
                }
                else {
                    if (!(Boolean) snapshot.child("checkBoxes").child("checkedBoxSegunda").getValue())
                        holder.checkBoxSegunda.setChecked(false);
                    if (!(Boolean) snapshot.child("checkBoxes").child("checkedBoxTerca").getValue())
                        holder.checkBoxTerca.setChecked(false);
                    if (!(Boolean) snapshot.child("checkBoxes").child("checkedBoxQuarta").getValue())
                        holder.checkBoxQuarta.setChecked(false);
                    if (!(Boolean) snapshot.child("checkBoxes").child("checkedBoxQuinta").getValue()) {
                        holder.checkBoxQuinta.setChecked(false);
                    }
                    if (!(Boolean) snapshot.child("checkBoxes").child("checkedBoxSexta").getValue())
                        holder.checkBoxSexta.setChecked(false);
                }
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

        holder.nome.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
            @Override
            public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {

                Log.i("Teste","Criando context menu");
                menu.setHeaderTitle(aluno.getNome());

                //Histórico de atividades
                menu.add(0, v.getId(), 0, "Histórico de atividades")
                        .setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        Intent newActivity = new Intent(context.getApplicationContext(), InfoAlunoActivity.class);
                        newActivity.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        newActivity.putExtra("nomeAluno", aluno.getNome());
                        context.getApplicationContext().startActivity(newActivity);
                        return false;
                    }
                });

                //Excluir aluno
                menu.add(0, v.getId(), 0, "Excluir").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        //Instanciar alertDialog
                        AlertDialog.Builder dialog = new AlertDialog.Builder(v.getContext());
                        dialog.setTitle("Confirmar exclusão?");
                        dialog.setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {
                            int countDado = 0, countSemana = 0, countMes = 0, countAno = 0;
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String user = ConfiguracaoFirebase.getFirebaseAutenticacao().getCurrentUser().getEmail().replace(".", "-");
                                firebaseRef.child(user).child("aluno").child(aluno.getNome()).removeValue();
                                for(int mes = 0 ; mes < 11 ; mes++){
                                    firebaseRef.child(user).child("tarefa").addListenerForSingleValueEvent(new ValueEventListener() {
                                        @RequiresApi(api = Build.VERSION_CODES.N)
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                             for(DataSnapshot ano : snapshot.getChildren()){
                                                 if(countAno == snapshot.getChildrenCount())
                                                     break;
                                                 else
                                                     countMes = 0;
                                                 for(DataSnapshot mes : ano.getChildren()){
                                                     if(countMes == ano.getChildrenCount())
                                                         break;
                                                     else
                                                         countSemana = 0;
                                                     for(DataSnapshot semana : mes.getChildren()){
                                                         if(countSemana == mes.getChildrenCount())
                                                             break;
                                                         else
                                                             countDado = 0;
                                                         for(DataSnapshot dado : semana.getChildren()){
                                                             if(countDado == semana.getChildrenCount())
                                                                 break;
                                                             countDado++;
                                                             Log.i("Teste", "dado : "+semana.getKey());
                                                             Tarefa tarefa = dado.getValue(Tarefa.class);
                                                             tarefa.setKey(dado.getKey());
                                                             ArrayList listFizeram = (ArrayList) dado.child("Alunos que fizeram").getValue();
                                                             ArrayList listNaoFizeram = (ArrayList) dado.child("Alunos que não fizeram").getValue();
                                                             List<Tarefa> listTarefas = new ArrayList<>();
                                                             listTarefas.addAll(HomeFragment.getListTarefas());
                                                             if(listFizeram != null) {
                                                                 if (listFizeram.contains(aluno.getNome())) {
                                                                     listFizeram.remove(aluno.getNome());
                                                                     listTarefas.remove(listTarefas.indexOf(aluno.getNome()));
                                                                     tarefa.setListAlunosFizeram(listFizeram);
                                                                     try {
                                                                         tarefa.salvarListas();
                                                                     } catch (ParseException e) {
                                                                         e.printStackTrace();
                                                                     }
                                                                 }
                                                             }
                                                             if(listNaoFizeram != null) {
                                                                 if (listNaoFizeram.contains(aluno.getNome())) {
                                                                     listNaoFizeram.remove(aluno.getNome());
                                                                     listTarefas.remove(listTarefas.indexOf(aluno.getNome()));
                                                                     tarefa.setListAlunosNaoFizeram(listNaoFizeram);
                                                                     try {
                                                                         tarefa.salvar();
                                                                     } catch (ParseException e) {
                                                                         e.printStackTrace();
                                                                     }
                                                                 }
                                                             }
                                                             HomeFragment.setTarefas(listTarefas);
                                                         }
                                                         countSemana++;
                                                     }
                                                     countMes++;
                                                 }
                                                 countAno++;
                                             }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {

                                        }
                                    });
                                }
                                Toast.makeText(context, "Aluno(a) "+aluno.getNome()+" removido(a)", Toast.LENGTH_SHORT).show();
                            }
                        });
                        dialog.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                        dialog.create().show();
                        return false;
                    }
                });

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
