package com.cursoandroid.app_hwreminder.ui.home;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cursoandroid.app_hwreminder.R;
import com.cursoandroid.app_hwreminder.activity.MainActivity;
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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

public class HomeFragment extends Fragment {

    private RecyclerView recyclerViewHome, recyclerViewAluno;
    private AdapterTarefa adapterTarefa;
    private AdapterAluno adapterAluno;
    private List<Tarefa> tarefas = new ArrayList<>();
    private List<Aluno> alunos = new ArrayList<>();

    private TextView textViewTituloLista, textDiaSemana, textViewSegunda,
            textViewTerca, textViewQuarta, textViewQuinta, textViewSexta,
            textViewSemanaHome;

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

        FragmentManager fragmentManager = getParentFragmentManager();

        textDiaSemana = view.findViewById(R.id.textViewDiaSemana);
        textViewSemanaHome = view.findViewById(R.id.textViewSemanaHome);

        //recyclerViewHome = view.findViewById(R.id.recyclerViewHome);
        textViewTituloLista = view.findViewById(R.id.textViewTituloLista);
        recyclerViewAluno = view.findViewById(R.id.recyclerViewAluno);

        //Change title from month to month, day every day and set currently week interval from monday to friday
        DecimalFormat mFormat= new DecimalFormat("00");
        Date date = new Date();
        Calendar c = Calendar.getInstance();
        textViewTituloLista.setText("Dever de casa - "+new SimpleDateFormat("MMMM").format(c.getTime()));
        textDiaSemana.setText(StringUtils.capitalize(new SimpleDateFormat("EEEE").format(c.getTime())));
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

        //Config RecyclerView tarefas
        //RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        //recyclerViewHome.setLayoutManager(layoutManager);
        //recyclerViewHome.setHasFixedSize(true);
        //recyclerViewHome.setAdapter(adapterTarefa);
        //recyclerViewHome.addItemDecoration(new DividerItemDecoration(recyclerViewHome.getContext(), DividerItemDecoration.VERTICAL));

        //Config RecyclerView alunos
        RecyclerView.LayoutManager layoutManagerAluno = new LinearLayoutManager(getContext());
        recyclerViewAluno.setLayoutManager(layoutManagerAluno);
        recyclerViewAluno.setHasFixedSize(true);
        recyclerViewAluno.setAdapter(adapterAluno);
        recyclerViewAluno.addItemDecoration(new DividerItemDecoration(recyclerViewAluno.getContext(), DividerItemDecoration.VERTICAL));

        fragmentManager.addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
            @Override
            public void onBackStackChanged() {
                startActivity(new Intent(getContext(), MainActivity.class));
            }
        });

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

                                switch (diaSemana){
                                    case "seg":
                                        textViewSegunda = view.findViewById(R.id.textViewSegunda);
                                        textViewSegunda.setText(tarefa.getDescricao());
                                        break;
                                    case "ter":
                                        textViewTerca = view.findViewById(R.id.textViewTerca);
                                        textViewTerca.setText(tarefa.getDescricao());
                                        break;
                                    case "qua":
                                        textViewQuarta = view.findViewById(R.id.textViewQuarta);
                                        textViewQuarta.setText(tarefa.getDescricao());
                                        break;
                                    case "qui":
                                        textViewQuinta = view.findViewById(R.id.textViewQuinta);
                                        textViewQuinta.setText(tarefa.getDescricao());
                                        break;
                                    case "sex":
                                        textViewSexta = view.findViewById(R.id.textViewSexta);
                                        textViewSexta.setText(tarefa.getDescricao());
                                        break;
                                    default:
                                        throw new IllegalStateException("Unexpected value: " + diaSemana);
                                }
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