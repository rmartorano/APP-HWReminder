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
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cursoandroid.app_hwreminder.R;
import com.cursoandroid.app_hwreminder.adapter.AdapterTarefa;
import com.cursoandroid.app_hwreminder.model.Tarefa;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class HomeFragment extends Fragment {

    private RecyclerView recyclerViewHome;
    private AdapterTarefa adapterTarefa;
    private List<Tarefa> tarefas = new ArrayList<>();

    public HomeFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerViewHome = view.findViewById(R.id.recyclerViewHome);

        //Configurar adapter
        adapterTarefa = new AdapterTarefa(tarefas, getContext());

        //Configurar RecyclerView
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerViewHome.setLayoutManager(layoutManager);
        recyclerViewHome.setHasFixedSize(true);
        recyclerViewHome.setAdapter(adapterTarefa);

        //Listar tarefas na tela inicial
        FirebaseDatabase.getInstance().getReference().child("tarefa")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @RequiresApi(api = Build.VERSION_CODES.O)
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yy");
                        for(DataSnapshot dados: snapshot.getChildren()){
                            Tarefa tarefa = dados.getValue(Tarefa.class);
                            long diasEntre = ChronoUnit.DAYS.between(LocalDate.now(), LocalDate.parse(tarefa.getDataEntrega(), dtf));
                            if(diasEntre <= 7){
                                Log.i("Teste", "Hoje: 10/03"+" Data no database: "+tarefa.getDataEntrega()+" Data diff: "+ diasEntre);
                                tarefas.add(tarefa);
                                adapterTarefa.notifyDataSetChanged();
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }
}