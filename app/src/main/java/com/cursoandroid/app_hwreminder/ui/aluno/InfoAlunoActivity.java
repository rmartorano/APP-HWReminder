package com.cursoandroid.app_hwreminder.ui.aluno;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.cursoandroid.app_hwreminder.R;
import com.cursoandroid.app_hwreminder.config.Date;
import com.cursoandroid.app_hwreminder.model.Aluno;
import com.cursoandroid.app_hwreminder.model.Tarefa;
import com.cursoandroid.app_hwreminder.ui.home.HomeFragment;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InfoAlunoActivity extends AppCompatActivity {

    private TextView textViewNome;
    private Aluno aluno;
    private final List<Aluno> listAlunos = HomeFragment.getListAlunos();
    private final List<Tarefa> listTarefas = HomeFragment.getListTarefas();
    private PieChart pieChart;
    private Map<String, Integer> mapQtdTarefas = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info_aluno);

        textViewNome = findViewById(R.id.textViewInfoNomeAluno);
        pieChart = findViewById(R.id.pieChart);
        String nomeFromExtra = getIntent().getStringExtra("nomeAluno");
        getSupportActionBar().setTitle("Frequência do aluno");

        for(Aluno a : listAlunos){
            if(a.getNome().equals(nomeFromExtra)){
                aluno = a;
                break;
            }
        }

        if(aluno.getNome() == null){
            textViewNome.setText(nomeFromExtra+" não encontrado!");
            return;
        }


        textViewNome.setText(nomeFromExtra);

        getQtdTarefas();
        List<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(mapQtdTarefas.get("qtdFizeramNaSemana"), "Tarefas feitas"));
        entries.add(new PieEntry(mapQtdTarefas.get("qtdNaoFizeramNaSemana"), "Tarefas não feitas"));

        PieDataSet dataSet = new PieDataSet(entries,"");
        dataSet.setColors(Color.GREEN, Color.RED);
        dataSet.setValueTextSize(14);
        dataSet.setSliceSpace(3);
        dataSet.setValueLineColor(Color.BLACK);

        int qtdTotalSemana = mapQtdTarefas.get("qtdFizeramNaSemana") + mapQtdTarefas.get("qtdNaoFizeramNaSemana");
        int qtdTotalMes = mapQtdTarefas.get("qtdFizeramNoMes") + mapQtdTarefas.get("qtdNaoFizeramNoMes");

        pieChart.setCenterText("Quantidade total de tarefas\n\nNeste mês: "+qtdTotalMes+"\nNesta semana: "+qtdTotalSemana);
        Legend legend = pieChart.getLegend();
        legend.setTextSize(14);

        Description description = new Description();
        description.setText("Descrição: Frequência do aluno");
        description.setTextSize(10);
        pieChart.setDescription(description);

        PieData pieData = new PieData(dataSet);
        pieChart.setData(pieData);
        pieChart.invalidate();

    }

    private void getQtdTarefas(){

        Date date = new Date();

        Calendar calendar = Calendar.getInstance();
        int week = date.getCalendar().get(Calendar.WEEK_OF_MONTH); //semana atual do mês
        int month = date.getCalendar().get(Calendar.MONTH);

        mapQtdTarefas.put("qtdFizeramNaSemana", 0);
        mapQtdTarefas.put("qtdNaoFizeramNaSemana", 0);
        mapQtdTarefas.put("qtdFizeramNoMes", 0);
        mapQtdTarefas.put("qtdNaoFizeramNoMes", 0);

        for(Tarefa tarefa : listTarefas){
            try {
                calendar.setTime(new SimpleDateFormat("dd/MM/yy").parse(tarefa.getDataEntrega()));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            if(calendar.get(Calendar.WEEK_OF_MONTH) == week) {
                if (tarefa.getListAlunosFizeram().contains(aluno.getNome()))
                    mapQtdTarefas.put("qtdFizeramNaSemana", mapQtdTarefas.get("qtdFizeramNaSemana") + 1);
                if (tarefa.getListAlunosNaoFizeram().contains(aluno.getNome()))
                    mapQtdTarefas.put("qtdNaoFizeramNaSemana", mapQtdTarefas.get("qtdNaoFizeramNaSemana") + 1);
            }
            if(calendar.get(Calendar.MONTH) == month){
                if (tarefa.getListAlunosFizeram().contains(aluno.getNome()))
                    mapQtdTarefas.put("qtdFizeramNoMes", mapQtdTarefas.get("qtdFizeramNoMes") + 1);
                if (tarefa.getListAlunosNaoFizeram().contains(aluno.getNome()))
                    mapQtdTarefas.put("qtdNaoFizeramNoMes", mapQtdTarefas.get("qtdNaoFizeramNoMes") + 1);
            }
        }

    }

}