package com.cursoandroid.app_hwreminder.ui.home;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cursoandroid.app_hwreminder.R;
import com.cursoandroid.app_hwreminder.adapter.AdapterAluno;
import com.cursoandroid.app_hwreminder.adapter.AdapterTarefa;
import com.cursoandroid.app_hwreminder.model.Aluno;
import com.cursoandroid.app_hwreminder.model.Tarefa;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.apache.commons.lang3.StringUtils;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class HomeFragment extends Fragment {

    private RecyclerView recyclerViewAluno;
    private AdapterTarefa adapterTarefa;
    private AdapterAluno adapterAluno;
    private List<Tarefa> tarefas = new ArrayList<>();
    private List<Aluno> alunos = new ArrayList<>();
    private Button buttonSalvarAlteracoes;

    private TextView textViewTituloLista, textDiaSemana,
            textViewSemanaHome, textViewDescricao;

    public HomeFragment() {
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        textDiaSemana = view.findViewById(R.id.textViewDiaSemana);
        textViewSemanaHome = view.findViewById(R.id.textViewSemanaHome);
        textViewTituloLista = view.findViewById(R.id.textViewTituloLista);
        recyclerViewAluno = view.findViewById(R.id.recyclerViewAluno);
        buttonSalvarAlteracoes = view.findViewById(R.id.buttonSalvarAlteracoes);

        //Change title from month to month, day every day and set currently week interval from monday to friday
        DecimalFormat mFormat= new DecimalFormat("00");
        Date date = new Date();
        Calendar c = Calendar.getInstance();
        textViewTituloLista.setText("Dever de casa - "+new SimpleDateFormat("MMMM").format(c.getTime()));
        textDiaSemana.setText(StringUtils.capitalize(new SimpleDateFormat("EEEE").format(c.getTime())));
        formatDayColorDaily(view); // change color of today to focus on that
        final int week = c.get(Calendar.WEEK_OF_MONTH); // week of the month
        c.set(Calendar.DAY_OF_WEEK, c.getFirstDayOfWeek());
        c.setTimeInMillis(c.getTimeInMillis()+Long.parseLong("86400000")); // set first day of week to monday not sunday
        long timeMili = c.getTimeInMillis();
        long sextaMili = timeMili+Long.parseLong("345600000"); // monday in millisecs + 4 days in millisecs
        Calendar sexta = Calendar.getInstance();
        sexta.setTimeInMillis(sextaMili);
        textViewSemanaHome.setText("Semana "+mFormat.format(Double.valueOf(c.get(Calendar.DAY_OF_MONTH)))+" / "+mFormat.format(Double.valueOf(c.get(Calendar.MONTH)))+" a "+mFormat.format(Double.valueOf(sexta.get(Calendar.DAY_OF_MONTH)))+" / "+mFormat.format(Double.valueOf(sexta.get(Calendar.MONTH))));

        //Config adapters
        adapterTarefa = new AdapterTarefa(tarefas, getContext());
        adapterAluno = new AdapterAluno(alunos, getContext());

        //Config RecyclerView alunos
        RecyclerView.LayoutManager layoutManagerAluno = new LinearLayoutManager(getContext());
        recyclerViewAluno.setLayoutManager(layoutManagerAluno);
        recyclerViewAluno.setHasFixedSize(true);
        recyclerViewAluno.setAdapter(adapterAluno);
        recyclerViewAluno.addItemDecoration(new DividerItemDecoration(recyclerViewAluno.getContext(), DividerItemDecoration.VERTICAL));

        //List 'tarefas' in the home screen
        FirebaseDatabase.getInstance().getReference().child("tarefa")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yy");
                        Date date = null;
                        String diaSemana = new String();
                        for(DataSnapshot dados: snapshot.getChildren()){

                            Tarefa tarefa = dados.getValue(Tarefa.class);
                            try {
                                c.setTime(new SimpleDateFormat("dd/MM/yy").parse(tarefa.getDataEntrega()));
                            } catch (ParseException e) {
                                e.printStackTrace();
                            };

                            if(c.get(Calendar.WEEK_OF_MONTH) == week){ // only shows currently week tarefas

                                //get weekDay string from tarefa
                                try {
                                    date = new SimpleDateFormat("dd/MM/yy").parse(tarefa.getDataEntrega());
                                    diaSemana = new SimpleDateFormat("EE").format(date);
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }

                                //Instanciar alertDialog
                                AlertDialog.Builder dialog = new AlertDialog.Builder(getContext());
                                switch (diaSemana){
                                    case "seg":
                                        textViewDescricao = view.findViewById(R.id.textViewSegunda);
                                        break;
                                    case "ter":
                                        textViewDescricao = view.findViewById(R.id.textViewTerca);
                                        break;
                                    case "qua":
                                        textViewDescricao = view.findViewById(R.id.textViewQuarta);
                                        break;
                                    case "qui":
                                        textViewDescricao = view.findViewById(R.id.textViewQuinta);
                                        break;
                                    case "sex":
                                        textViewDescricao = view.findViewById(R.id.textViewSexta);
                                        break;
                                    default:
                                        throw new IllegalStateException("Unexpected value: " + diaSemana);
                                }

                                if(tarefa.getDescricao().length()>40){
                                    String tmp = tarefa.getDescricao().substring(0,40)+"...";
                                    textViewDescricao.setText(tmp);
                                }
                                else
                                    textViewDescricao.setText(tarefa.getDescricao());


                                dialog.setTitle(tarefa.getTitulo());
                                dialog.setMessage(tarefa.getDescricao());
                                dialog.setIcon(R.drawable.ic_menu_book);
                                //Config ações para sim ou não
                                dialog.setPositiveButton("Editar", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int which) { //on clicking to edit, will open a new dialogAlert
                                        AlertDialog.Builder dialogEditDescricao = new AlertDialog.Builder(getContext());
                                        dialogEditDescricao.setTitle("Editar "+tarefa.getTitulo());
                                        dialogEditDescricao.setMessage("Digite a nova descrição");
                                        //Config input view
                                        final EditText input = new EditText(getContext());
                                        input.setInputType(InputType.TYPE_CLASS_TEXT);
                                        dialogEditDescricao.setView(input);

                                        input.setHint(tarefa.getDescricao());
                                        dialogEditDescricao.setPositiveButton("Salvar", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int which) {
                                                tarefa.setDescricao(input.getText().toString());
                                                textViewDescricao.setText(input.getText().toString());
                                                dialog.setMessage(tarefa.getDescricao());
                                            }
                                        });
                                        dialogEditDescricao.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.cancel();
                                            }
                                        });
                                        dialogEditDescricao.create();
                                        dialogEditDescricao.show();
                                    }
                                });

                                dialog.setNegativeButton("Remover", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        AlertDialog.Builder dialogConfirmRemoval = new AlertDialog.Builder(getContext());
                                        dialogConfirmRemoval.setMessage("Deseja mesmo remover '"+tarefa.getTitulo()+"'?");
                                        dialogConfirmRemoval.setPositiveButton("Remover", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                Toast.makeText(getContext(), "Tarefa "+tarefa.getTitulo()+" removida", Toast.LENGTH_SHORT).show();
                                                tarefa.deletarTarefa();
                                                textViewDescricao.setText("Sem tarefa");
                                                textViewDescricao.setOnClickListener(null); // remove the click listener
                                            }
                                        });
                                        dialogConfirmRemoval.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.cancel();
                                            }
                                        });
                                        dialogConfirmRemoval.create();
                                        dialogConfirmRemoval.show();
                                    }
                                });

                                //set click listener to open dialog when clicking the description of tarefa
                                textViewDescricao.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        //Criar e exibir AlertDialog
                                        dialog.create();
                                        dialog.show();
                                    }
                                });

                                tarefas.add(tarefa);
                                adapterTarefa.notifyDataSetChanged();
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        //List 'alunos' in the home screen
        FirebaseDatabase.getInstance().getReference().child("aluno")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for(DataSnapshot dados: snapshot.getChildren()){
                            Aluno aluno = dados.getValue(Aluno.class);
                            alunos.add(aluno);
                            adapterAluno.notifyDataSetChanged();
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        //Botão para salvar alterações na tela inicial
        buttonSalvarAlteracoes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                salvarAlteracoesCheckBoxes();
            }
        });

    }

    private void formatDayColorDaily(View view){
        Calendar c = Calendar.getInstance();
        String today = new SimpleDateFormat("EE").format(c.getTime());
        TextView textView = null;
        switch (today){
            case "ter":
                textView = view.findViewById(R.id.textViewTercaFix);
                break;
            case "qua":
                textView = view.findViewById(R.id.textViewQuartaFix);
                break;
            case "qui":
                textView = view.findViewById(R.id.textViewQuintaFix);
                break;
            case "sex":
                textView = view.findViewById(R.id.textViewSextaFix);
                break;
            default:
                textView = view.findViewById(R.id.textViewSegundaFix);
                break;
        }
        textView.setTextColor(getResources().getColor(R.color.teal_200));
    }

    private void salvarAlteracoesCheckBoxes() {
        FirebaseDatabase.getInstance().getReference().child("aluno").
                addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot dados : snapshot.getChildren()) {
                            Aluno aluno = dados.getValue(Aluno.class);
                            Log.i("Teste", dados.child("checkedBoxSegunda").getKey()+": "+dados.child("checkedBoxSegunda").getValue());
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

}