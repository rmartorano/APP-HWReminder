package com.cursoandroid.app_hwreminder.ui.acompanhamento;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;

import com.cursoandroid.app_hwreminder.R;
import com.cursoandroid.app_hwreminder.adapter.AdapterAlunosMaisFazem;
import com.cursoandroid.app_hwreminder.config.Date;
import com.cursoandroid.app_hwreminder.ui.home.HomeFragment;

public class MaisFizeramActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mais_fizeram);
        setTitle("Alunos que mais fizeram tarefas");

        Date date = new Date();

        TextView textMes = findViewById(R.id.textViewMesMaisFizeram);
        textMes.setText("Tarefas de "+date.getMonthString());

        AdapterAlunosMaisFazem adapterAlunosMaisFazem = new AdapterAlunosMaisFazem
                (HomeFragment.getListAlunos(), HomeFragment.getListTarefas(), this);

        RecyclerView recyclerView = findViewById(R.id.recyclerViewMaisFizeram);
        RecyclerView.LayoutManager layoutManagerAluno = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManagerAluno);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(adapterAlunosMaisFazem);

    }
}