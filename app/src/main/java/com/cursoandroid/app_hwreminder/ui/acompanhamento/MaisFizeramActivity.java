package com.cursoandroid.app_hwreminder.ui.acompanhamento;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.TextView;

import com.cursoandroid.app_hwreminder.R;
import com.cursoandroid.app_hwreminder.adapter.AdapterAlunosMaisFazem;
import com.cursoandroid.app_hwreminder.adapter.AdapterFiltrarAlunoFeedback;
import com.cursoandroid.app_hwreminder.config.Date;
import com.cursoandroid.app_hwreminder.model.Aluno;
import com.cursoandroid.app_hwreminder.ui.home.HomeFragment;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class MaisFizeramActivity extends AppCompatActivity {

    private final List<Aluno> listAlunos = HomeFragment.getListAlunos();
    List<Aluno> listTmp = new ArrayList<>();
    Spinner spinnerOrdenar;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mais_fizeram);
        setTitle("Alunos que mais fizeram tarefas");

        Date date = new Date();

        TextView textMes = findViewById(R.id.textViewMesMaisFizeram);
        textMes.setText("Tarefas de "+date.getMonthString());
        spinnerOrdenar = findViewById(R.id.spinnerOrdenaraAcompanhamentoFizeram);
        SearchView searchAluno = findViewById(R.id.searchViewAlunoAcompanhamento);

        AdapterAlunosMaisFazem adapterAlunosMaisFazem = new AdapterAlunosMaisFazem
                (listTmp, HomeFragment.getListTarefas(), this);

        RecyclerView recyclerView = findViewById(R.id.recyclerViewMaisFizeram);
        RecyclerView.LayoutManager layoutManagerAluno = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManagerAluno);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(adapterAlunosMaisFazem);

        final String[] lastText = {""};
        filtrarAlunos("", adapterAlunosMaisFazem, listTmp);
        searchAluno.setIconified(false);
        searchAluno.clearFocus();
        searchAluno.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public boolean onQueryTextChange(String newText) {
                filtrarAlunos(newText, adapterAlunosMaisFazem, listTmp);
                lastText[0] = newText;
                return true;
            }
        });

        spinnerOrdenar.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                filtrarAlunos(lastText[0], adapterAlunosMaisFazem, listTmp);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    void filtrarAlunos(String query, AdapterAlunosMaisFazem adapterAlunosMaisFazem, List<Aluno> alunosFiltro){

        alunosFiltro.clear();
        if(query.equals("")) {
            alunosFiltro.addAll(listAlunos); //mostra todos os alunos
        }
        else{
            for(Aluno aluno : listAlunos){
                if(aluno.getNome().toLowerCase().contains(query.toLowerCase()))
                    alunosFiltro.add(aluno); //mostra só os que contém os caracteres digitados
            }
        }

        switch (spinnerOrdenar.getSelectedItem().toString().toLowerCase()){

            case "nome":{
                alunosFiltro.sort(Comparator.comparing(Aluno::getNome));
                break;
            }

            case "tarefas feitas":{
                alunosFiltro.sort(Comparator.comparing(Aluno::getQtdProgressBarTarefasFeitas).reversed());
                break;
            }

            case "tarefas não feitas":{
                alunosFiltro.sort(Comparator.comparing(Aluno::getQtdProgressBarTarefasNaoFeitas).reversed());
                break;
            }

        }
        adapterAlunosMaisFazem.notifyDataSetChanged();
    }

}