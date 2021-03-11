package com.cursoandroid.app_hwreminder.ui.home;

import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class HomeFragment extends Fragment {

    private RecyclerView recyclerViewHome, recyclerViewAluno;
    private AdapterTarefa adapterTarefa;
    private AdapterAluno adapterAluno;
    private List<Tarefa> tarefas = new ArrayList<>();
    private List<Aluno> alunos = new ArrayList<>();
    private TextView textViewTituloLista, textDiaSemana;

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
        recyclerViewHome = view.findViewById(R.id.recyclerViewHome);
        textViewTituloLista = view.findViewById(R.id.textViewTituloLista);
        recyclerViewAluno = view.findViewById(R.id.recyclerViewAluno);

        //Change title from month to month
        Calendar cal = Calendar.getInstance();
        textViewTituloLista.setText("Dever de casa - "+new SimpleDateFormat("MMMM").format(cal.getTime()));

        //Config adapters
        adapterTarefa = new AdapterTarefa(tarefas, getContext());
        adapterAluno = new AdapterAluno(alunos, getContext());

        //Config RecyclerView tarefas
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerViewHome.setLayoutManager(layoutManager);
        recyclerViewHome.setHasFixedSize(true);
        recyclerViewHome.setAdapter(adapterTarefa);
        recyclerViewHome.addItemDecoration(new DividerItemDecoration(recyclerViewHome.getContext(), DividerItemDecoration.VERTICAL));

        //Config RecyclerView alunos
        RecyclerView.LayoutManager layoutManagerAluno = new LinearLayoutManager(getContext());
        recyclerViewAluno.setLayoutManager(layoutManagerAluno);
        recyclerViewAluno.setHasFixedSize(true);
        recyclerViewAluno.setAdapter(adapterAluno);
        recyclerViewAluno.addItemDecoration(new DividerItemDecoration(recyclerViewAluno.getContext(), DividerItemDecoration.VERTICAL));

        //List 'tarefas' in the home screen
        FirebaseDatabase.getInstance().getReference().child("tarefa")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @RequiresApi(api = Build.VERSION_CODES.O)
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yy");
                        Date date = null;
                        String diaSemana = new String();
                        for(DataSnapshot dados: snapshot.getChildren()){
                            Tarefa tarefa = dados.getValue(Tarefa.class);
                            long diasEntre = ChronoUnit.DAYS.between(LocalDate.now(), LocalDate.parse(tarefa.getDataEntrega(), dtf));
                            if(diasEntre <= 7){

                                //get weekDay string
                                try {
                                    date = new SimpleDateFormat("dd/MM/yyyy").parse(tarefa.getDataEntrega());
                                    diaSemana = new SimpleDateFormat("EE").format(date);
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                                Log.i("Teste", "Dia: "+ diaSemana);

                                switch (diaSemana){
                                    case "seg":
                                        textDiaSemana = view.findViewById(R.id.textViewS);
                                        break;
                                    case "ter":
                                        textDiaSemana = view.findViewById(R.id.textViewT);
                                        break;
                                    case "qua":
                                        textDiaSemana = view.findViewById(R.id.textViewQ);
                                        break;
                                    case "qui":
                                        textDiaSemana = view.findViewById(R.id.textViewQui);
                                        break;
                                    case "sex":
                                        textDiaSemana = view.findViewById(R.id.textViewSex);
                                        break;
                                }
                                //textDiaSemana.setText(tarefa.getDescricao().toString());
                                //Log.i("Teste", textDiaSemana.getText().toString());
                                Log.i("Teste", "Hoje: 11/03"+" Data no database: "+tarefa.getDataEntrega()+" Data diff: "+ diasEntre);
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

    }
}