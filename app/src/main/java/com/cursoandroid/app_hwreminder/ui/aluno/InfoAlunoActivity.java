package com.cursoandroid.app_hwreminder.ui.aluno;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
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
import java.util.Locale;
import java.util.Map;

public class InfoAlunoActivity extends AppCompatActivity {

    private TextView textViewNome;
    private Aluno aluno;
    private final List<Aluno> listAlunos = HomeFragment.getListAlunos();
    private final List<Tarefa> listTarefas = HomeFragment.getListTarefas();
    private PieChart pieChart;
    private Map<String, Integer> mapQtdTarefas = new HashMap<>();
    private Spinner spinner, secondSpinner, thirdSpinner;

    //Enums
    private final int SEMANAL = 0;
    private final int MENSAL = 1;
    private final int SEMESTRAL = 2;
    private final int ANUAL = 3;

    private final int JANEIRO = 0;
    private final int FEVEREIRO = 1;
    private final int MARCO = 2;
    private final int ABRIL = 3;
    private final int MAIO = 4;
    private final int JUNHO = 5;
    private final int JULHO = 6;
    private final int AGOSTO = 7;
    private final int SETEMBRO = 8;
    private final int OUTUBRO = 9;
    private final int NOVEMBRO = 10;
    private final int DEZEMBRO = 11;

    private Map<Integer, String> mapMeses = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info_aluno);

        textViewNome = findViewById(R.id.textViewInfoNomeAluno);
        spinner = findViewById(R.id.spinnerFiltroInfoAluno);
        secondSpinner = findViewById(R.id.secondSpinner);
        thirdSpinner = findViewById(R.id.thirdSpinner);

        mapMeses.put(0, "janeiro");
        mapMeses.put(1, "fevereiro");
        mapMeses.put(2, "março");
        mapMeses.put(3, "abril");
        mapMeses.put(4, "maio");
        mapMeses.put(5, "junho");
        mapMeses.put(6, "julho");
        mapMeses.put(7, "agosto");
        mapMeses.put(8, "setembro");
        mapMeses.put(9, "outubro");
        mapMeses.put(10, "novembro");
        mapMeses.put(11, "dezembro");

        pieChart = findViewById(R.id.pieChart);
        String nomeFromExtra = getIntent().getStringExtra("nomeAluno");
        getSupportActionBar().setTitle("Frequência do aluno");

        for(Aluno a : listAlunos){
            if(a.getNome().equals(nomeFromExtra)){
                aluno = a;
                break;
            }
        }

        if(aluno.getNome() == null) {
            textViewNome.setText(nomeFromExtra + " não encontrado!");
            return;
        }

        textViewNome.setText(nomeFromExtra);

        //prenche o gráfico com valores iniciais, na config de semanalmente
        Calendar calendar = Calendar.getInstance();

        switch (new SimpleDateFormat("MMMM").format(calendar.getTime()).toLowerCase()) {
            case "janeiro":
                secondSpinner.setSelection(JANEIRO);
                break;
            case "fevereiro":
                secondSpinner.setSelection(FEVEREIRO);
                break;
            case "março":
                secondSpinner.setSelection(MARCO);
                break;
            case "abril":
                secondSpinner.setSelection(ABRIL);
                break;
            case "maio":
                secondSpinner.setSelection(MAIO);
                break;
            case "junho":
                secondSpinner.setSelection(JUNHO);
                break;
            case "julho":
                secondSpinner.setSelection(JULHO);
                break;
            case "agosto":
                secondSpinner.setSelection(AGOSTO);
                break;
            case "setembro":
                secondSpinner.setSelection(SETEMBRO);
                break;
            case "outubro":
                secondSpinner.setSelection(OUTUBRO);
                break;
            case "novembro":
                secondSpinner.setSelection(NOVEMBRO);
                break;
            case "dezembro":
                secondSpinner.setSelection(DEZEMBRO);
                break;
        } //preenche o segundo spinner com os meses

        Date date = new Date();
        ArrayList<String> spinnerArray = new ArrayList<>();
        calendar.set(Calendar.MONTH, secondSpinner.getSelectedItemPosition());
        Map<String, String> mapIntervals = date.getAllWeekIntervals(secondSpinner.getSelectedItemPosition());
        for(int i=1; i<5 ; i++){
            spinnerArray.add(mapIntervals.get("Semana "+i));
        }
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item,
                spinnerArray); //selected item will look like a spinner set from XML
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout
                .simple_spinner_dropdown_item);
        thirdSpinner.setAdapter(spinnerArrayAdapter);
        thirdSpinner.setSelection(calendar.get(Calendar.WEEK_OF_MONTH)-1);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                generateChart(position, secondSpinner.getSelectedItemPosition(), thirdSpinner.getSelectedItemPosition());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        secondSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                generateChart(spinner.getSelectedItemPosition(), position, thirdSpinner.getSelectedItemPosition());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void generateChart(int periodo, int sSpinner, int tSpinner){

        fillSpinners(periodo);
        getQtdTarefas(periodo);

        List<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(mapQtdTarefas.get("qtdFizeram"), "Tarefas feitas"));
        entries.add(new PieEntry(mapQtdTarefas.get("qtdNaoFizeram"), "Tarefas não feitas"));

        PieDataSet dataSet = new PieDataSet(entries,"");
        dataSet.setColors(Color.GREEN, Color.RED);
        dataSet.setValueTextSize(14);
        dataSet.setSliceSpace(3);
        dataSet.setValueLineColor(Color.BLACK);

        int qtdTotal = mapQtdTarefas.get("qtdFizeram") + mapQtdTarefas.get("qtdNaoFizeram");

        pieChart.setCenterText("Quantidade total de tarefas\n\n"+qtdTotal);
        pieChart.setCenterTextSize(14);
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

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void fillSpinners(int periodo){

        ArrayList<String> spinnerArray = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        Date date = new Date();

        switch (periodo){

            case MENSAL:
            case SEMANAL: { //preenche terceiro spinner com os intervalos da semana
                calendar.set(Calendar.MONTH, secondSpinner.getSelectedItemPosition());
                Map<String, String> mapIntervals = date.getAllWeekIntervals(secondSpinner.getSelectedItemPosition());
                Log.i("Teste","max : "+calendar.getActualMaximum(Calendar.WEEK_OF_MONTH));
                for(int i=1; i<calendar.getActualMaximum(Calendar.WEEK_OF_MONTH) ; i++){
                    spinnerArray.add(mapIntervals.get("Semana "+i));
                }
                ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<>(
                        this, android.R.layout.simple_spinner_item,
                        spinnerArray); //selected item will look like a spinner set from XML
                spinnerArrayAdapter.setDropDownViewResource(android.R.layout
                        .simple_spinner_dropdown_item);
                thirdSpinner.setAdapter(spinnerArrayAdapter);
                thirdSpinner.setSelection(calendar.get(Calendar.WEEK_OF_MONTH)-1);
                break;
            }

        }

    }

    private void getQtdTarefas(int periodo){

        Date date = new Date();

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.MONTH, secondSpinner.getSelectedItemPosition());
        int semana = thirdSpinner.getSelectedItemPosition()+1; //semana atual do mês
        int mes = calendar.get(Calendar.MONTH)+1;
        int ano = date.getCalendar().get(Calendar.YEAR);
        Calendar calendarTarefa = Calendar.getInstance();
        calendarTarefa.setTime(calendar.getTime());

        mapQtdTarefas.put("qtdFizeram", 0);
        mapQtdTarefas.put("qtdNaoFizeram", 0);

        for(Tarefa tarefa : listTarefas){
            try {
                calendarTarefa.setTime(new SimpleDateFormat("dd/MM/yyyy").parse(tarefa.getDataEntrega()));
            } catch (ParseException e) {
                e.printStackTrace();
            }

            if(calendarTarefa.get(Calendar.WEEK_OF_MONTH) == semana) {
                if (tarefa.getListAlunosFizeram().contains(aluno.getNome())) {
                    mapQtdTarefas.put("qtdFizeram", mapQtdTarefas.get("qtdFizeram") + 1);
                }
                if (tarefa.getListAlunosNaoFizeram().contains(aluno.getNome())) {
                    mapQtdTarefas.put("qtdNaoFizeram", mapQtdTarefas.get("qtdNaoFizeram") + 1);
                }
            }
            else if(calendarTarefa.get(Calendar.MONTH)+1 == mes && periodo >= MENSAL){
                if (tarefa.getListAlunosFizeram().contains(aluno.getNome()))
                    mapQtdTarefas.put("qtdFizeram", mapQtdTarefas.get("qtdFizeram") + 1);
                if (tarefa.getListAlunosNaoFizeram().contains(aluno.getNome()))
                    mapQtdTarefas.put("qtdNaoFizeram", mapQtdTarefas.get("qtdNaoFizeram") + 1);
            }
            else if(calendarTarefa.get(Calendar.MONTH)+1 >= mes-5 && calendarTarefa.get(Calendar.YEAR) == calendar.get(Calendar.YEAR) && periodo >= SEMESTRAL){
                if (tarefa.getListAlunosFizeram().contains(aluno.getNome()))
                    mapQtdTarefas.put("qtdFizeram", mapQtdTarefas.get("qtdFizeram") + 1);
                if (tarefa.getListAlunosNaoFizeram().contains(aluno.getNome()))
                    mapQtdTarefas.put("qtdNaoFizeram", mapQtdTarefas.get("qtdNaoFizeram") + 1);
            }
            else if(calendarTarefa.get(Calendar.YEAR) == ano && periodo >= ANUAL){
                if (tarefa.getListAlunosFizeram().contains(aluno.getNome()))
                    mapQtdTarefas.put("qtdFizeram", mapQtdTarefas.get("qtdFizeram") + 1);
                if (tarefa.getListAlunosNaoFizeram().contains(aluno.getNome()))
                    mapQtdTarefas.put("qtdNaoFizeram", mapQtdTarefas.get("qtdNaoFizeram") + 1);
            }
        }
    }

    public void recuperarTarefas(int mesInicial, int mesFinal){

    }

}