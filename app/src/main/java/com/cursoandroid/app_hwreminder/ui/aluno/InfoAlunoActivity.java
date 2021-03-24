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
import com.cursoandroid.app_hwreminder.model.Aluno;
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
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class InfoAlunoActivity extends AppCompatActivity {

    TextView textViewNome;
    Aluno aluno;
    private final List<Aluno> listAlunos = HomeFragment.getListAlunos();
    PieChart pieChart;

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
        }


        textViewNome.setText(nomeFromExtra);
        Legend legend = pieChart.getLegend();

        ValueFormatter pointFormatter = new ValueFormatter() {
            private DecimalFormat format = new DecimalFormat("0");
            @Override
            public String getPointLabel(Entry entry) {
                return format.format(entry.getX()+" %");
            }
        };

        List<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(15f, "Tarefas feitas"));
        entries.add(new PieEntry(6f, "Tarefas não feitas"));

        PieDataSet dataSet = new PieDataSet(entries,"");
        dataSet.setColors(Color.GREEN, Color.RED);
        dataSet.setValueTextSize(14);
        dataSet.setSliceSpace(3);
        dataSet.setValueLineColor(Color.BLACK);

        pieChart.setCenterText("Quantidade total de tarefas\n\nNeste mês: "+"21"+"\nNesta semana: "+"21");
        legend.setTextSize(14);

        Description description = new Description();
        description.setText("Descrição: Frequência do aluno");
        description.setTextSize(10);
        pieChart.setDescription(description);

        PieData pieData = new PieData(dataSet);
        pieChart.setData(pieData);
        pieChart.invalidate();

    }
}