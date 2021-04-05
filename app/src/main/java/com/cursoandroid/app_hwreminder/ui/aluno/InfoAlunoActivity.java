package com.cursoandroid.app_hwreminder.ui.aluno;

import androidx.annotation.NonNull;
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
import android.util.ArraySet;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import com.cursoandroid.app_hwreminder.R;
import com.cursoandroid.app_hwreminder.config.ConfiguracaoFirebase;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static com.cursoandroid.app_hwreminder.ui.home.HomeFragment.getListAlunos;

public class InfoAlunoActivity extends AppCompatActivity {

    private TextView textViewNome;
    private Aluno aluno;
    private final List<Aluno> listAlunos = getListAlunos();
    private List<Tarefa> listTarefas = HomeFragment.getListTarefas();
    private PieChart pieChart;
    private Map<String, Integer> mapQtdTarefas = new HashMap<>();
    private Spinner spinner, secondSpinner, thirdSpinner;
    private boolean hasThirdSpinnerIdChanged, hasSecondSpinnerIdChanged = false, hasFirstSpinnerIdChanged = false;
    private boolean firstTimeLoading = true;
    private int firstSpinnerLastPos = 0, secondSpinnerLastPos, thirdSpinnerLastPos;
    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDatabase();
    private ProgressBar indeterminateBar;

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

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info_aluno);

        textViewNome = findViewById(R.id.textViewInfoNomeAluno);
        spinner = findViewById(R.id.spinnerFiltroInfoAluno);
        secondSpinner = findViewById(R.id.secondSpinner);
        thirdSpinner = findViewById(R.id.thirdSpinner);
        indeterminateBar = findViewById(R.id.indeterminateBar);

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

        for (Aluno a : listAlunos) {
            if (a.getNome().equals(nomeFromExtra)) {
                aluno = a;
                break;
            }
        }

        if (aluno.getNome() == null) {
            textViewNome.setText(nomeFromExtra + " não encontrado!");
            return;
        }

        textViewNome.setText(nomeFromExtra);

        //prenche o gráfico com valores iniciais, na config de semanalmente
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);

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

        secondSpinnerLastPos = secondSpinner.getSelectedItemPosition();
        thirdSpinnerLastPos = calendar.get(Calendar.WEEK_OF_MONTH) - 1;


        generateChart(SEMANAL);

    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void generateChart(int periodo) {

        indeterminateBar.setVisibility(View.VISIBLE);
        pieChart.setCenterText(" ");
        fillSpinners(periodo);

    }

    private void generateChartData() {

        List<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(mapQtdTarefas.get("qtdFizeram"), "Tarefas feitas"));
        entries.add(new PieEntry(mapQtdTarefas.get("qtdNaoFizeram"), "Tarefas não feitas"));

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(Color.GREEN, Color.RED);
        dataSet.setValueTextSize(14);
        dataSet.setSliceSpace(3);
        dataSet.setValueLineColor(Color.BLACK);

        int qtdTotal = mapQtdTarefas.get("qtdFizeram") + mapQtdTarefas.get("qtdNaoFizeram");

        pieChart.setCenterText("Quantidade total de tarefas\n\n" + qtdTotal);
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

        if (firstTimeLoading) {
            setListeners();
            firstTimeLoading = false;
        }
        indeterminateBar.setVisibility(View.INVISIBLE);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void fillSpinners(int periodo) {

        switch (periodo) {

            case SEMANAL: { //preenche terceiro spinner com os intervalos da semana
                ArrayList<String> spinnerArray = new ArrayList<>();
                Calendar calendar = Calendar.getInstance();
                Date date = new Date();
                if (hasSecondSpinnerIdChanged || hasFirstSpinnerIdChanged || firstTimeLoading) {
                    listTarefas = HomeFragment.getListTarefas();
                    calendar.set(Calendar.MONTH, secondSpinner.getSelectedItemPosition());
                    Map<String, String> mapIntervals = date.getAllWeekIntervals(secondSpinner.getSelectedItemPosition()); //recupera todos os intervalos da semana do mês
                    for (int i = 0; i < mapIntervals.size(); i++) {
                        spinnerArray.add(mapIntervals.get("Semana " + (i + 1)));
                    }
                    ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<>(
                            this, android.R.layout.simple_spinner_item,
                            spinnerArray);
                    spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    thirdSpinner.setAdapter(spinnerArrayAdapter);
                    if (firstTimeLoading) {
                        Calendar calendarTeste = Calendar.getInstance();
                        calendarTeste.setMinimalDaysInFirstWeek(7);
                        calendarTeste.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
                        thirdSpinner.setSelection(calendarTeste.get(Calendar.DAY_OF_WEEK_IN_MONTH) - 1);
                    }
                    hasSecondSpinnerIdChanged = false;
                    hasFirstSpinnerIdChanged = false;
                }
                if(thirdSpinner.getVisibility()!=View.VISIBLE)
                    thirdSpinner.setVisibility(View.VISIBLE);
                if (secondSpinner.getSelectedItemPosition() != secondSpinnerLastPos) {
                    secondSpinnerLastPos = secondSpinner.getSelectedItemPosition();
                    recuperarTarefas(secondSpinner.getSelectedItemPosition());
                } else {
                    getQtdTarefas(spinner.getSelectedItemPosition());
                }
                break;
            }

            case MENSAL: {
                if (hasFirstSpinnerIdChanged || hasSecondSpinnerIdChanged) {
                    if (secondSpinner.getCount() < 12) {
                        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                                R.array.array_filtro_info_aluno_meses, android.R.layout.simple_spinner_item);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        secondSpinner.setAdapter(adapter);
                        Calendar calendar = Calendar.getInstance();
                        secondSpinner.setSelection(calendar.get(Calendar.MONTH));
                    }
                    thirdSpinner.setVisibility(View.GONE);
                    hasFirstSpinnerIdChanged = false;
                    hasSecondSpinnerIdChanged = false;
                }
                if(thirdSpinner.getVisibility()!=View.GONE)
                    thirdSpinner.setVisibility(View.GONE);
                recuperarTarefas(secondSpinner.getSelectedItemPosition());
                break;
            }

            case SEMESTRAL: {
                if (hasFirstSpinnerIdChanged || hasSecondSpinnerIdChanged) {
                    if (!secondSpinner.getItemAtPosition(0).toString().equals("1º semestre")) { //se tiver mais que 2 opções no dropdown quer dizer que está os meses configurados e não os semestres
                        ArrayList<String> spinnerArray = new ArrayList<>();
                        spinnerArray.add("1º semestre");
                        spinnerArray.add("2º semestre");
                        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<>(
                                this, android.R.layout.simple_spinner_item,
                                spinnerArray);
                        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        secondSpinner.setAdapter(spinnerArrayAdapter);
                        secondSpinnerLastPos = 0;
                    }
                    if(thirdSpinner.getVisibility()!=View.GONE)
                        thirdSpinner.setVisibility(View.GONE);
                    secondSpinnerLastPos = secondSpinner.getSelectedItemPosition();
                    recuperarTarefas(0);
                }
                break;
            }

            case ANUAL:
            {
                if(hasFirstSpinnerIdChanged || hasSecondSpinnerIdChanged){

                    firebaseRef.child("tarefa").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            Calendar calendar = Calendar.getInstance();
                            boolean isConfigured = false;
                            for(int i = 0 ; i < secondSpinner.getCount() ; i++){
                                if(secondSpinner.getItemAtPosition(i).toString().equals(String.valueOf(calendar.get(Calendar.YEAR)))) {
                                    isConfigured = true;
                                    break;
                                }
                            }
                            if(!isConfigured) {
                                ArrayList<String> spinnerArray = new ArrayList<>();
                                for (DataSnapshot ano : snapshot.getChildren()) {
                                    spinnerArray.add(ano.getKey());
                                }
                                ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<>(
                                        getApplicationContext(), android.R.layout.simple_spinner_item,
                                        spinnerArray);
                                spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                secondSpinner.setAdapter(spinnerArrayAdapter);
                                secondSpinnerLastPos = 0;
                            }
                            secondSpinnerLastPos = secondSpinner.getSelectedItemPosition();
                            recuperarTarefas(0);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

                    if(thirdSpinner.getVisibility()!=View.GONE)
                        thirdSpinner.setVisibility(View.GONE);
                }
                break;
            }

        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void getQtdTarefas(int periodo) {

        Date date = new Date();

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.MONTH, secondSpinner.getSelectedItemPosition());
        int semana = thirdSpinner.getSelectedItemPosition() + 1; //semana atual do mês
        int mes = secondSpinner.getSelectedItemPosition() + 1; //mês atual
        int ano = periodo == ANUAL ? Integer.parseInt(secondSpinner.getSelectedItem().toString()) : date.getCalendar().get(Calendar.YEAR);
        Calendar calendarTarefa = Calendar.getInstance();

        mapQtdTarefas.put("qtdFizeram", 0);
        mapQtdTarefas.put("qtdNaoFizeram", 0);

        Log.i("Teste", "tarefas size: " + listTarefas.size());

        for (Tarefa tarefa : listTarefas) {
            try {
                calendarTarefa.setTime(new SimpleDateFormat("dd/MM/yyyy").parse(tarefa.getDataEntrega()));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            calendarTarefa.setMinimalDaysInFirstWeek(7);
            calendarTarefa.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
            Log.i("Teste", "semana: " + semana + " semana tarefa: " + calendarTarefa.get(Calendar.DAY_OF_WEEK_IN_MONTH));
            if (calendarTarefa.get(Calendar.DAY_OF_WEEK_IN_MONTH) == semana && periodo == SEMANAL && calendarTarefa.get(Calendar.MONTH) + 1 == mes) {
                if (tarefa.getListAlunosFizeram().contains(aluno.getNome())) {
                    mapQtdTarefas.put("qtdFizeram", mapQtdTarefas.get("qtdFizeram") + 1);
                }
                if (tarefa.getListAlunosNaoFizeram().contains(aluno.getNome())) {
                    mapQtdTarefas.put("qtdNaoFizeram", mapQtdTarefas.get("qtdNaoFizeram") + 1);
                }
            } else if (calendarTarefa.get(Calendar.MONTH) + 1 == mes && periodo >= MENSAL && calendarTarefa.get(Calendar.YEAR) == ano) {
                if (tarefa.getListAlunosFizeram().contains(aluno.getNome()))
                    mapQtdTarefas.put("qtdFizeram", mapQtdTarefas.get("qtdFizeram") + 1);
                if (tarefa.getListAlunosNaoFizeram().contains(aluno.getNome()))
                    mapQtdTarefas.put("qtdNaoFizeram", mapQtdTarefas.get("qtdNaoFizeram") + 1);
            } else if (calendarTarefa.get(Calendar.MONTH) + 1 >= mes - 5 && calendarTarefa.get(Calendar.YEAR) == calendar.get(Calendar.YEAR) && periodo >= SEMESTRAL) {
                if (tarefa.getListAlunosFizeram().contains(aluno.getNome()))
                    mapQtdTarefas.put("qtdFizeram", mapQtdTarefas.get("qtdFizeram") + 1);
                if (tarefa.getListAlunosNaoFizeram().contains(aluno.getNome()))
                    mapQtdTarefas.put("qtdNaoFizeram", mapQtdTarefas.get("qtdNaoFizeram") + 1);
            } else if (calendarTarefa.get(Calendar.YEAR) == ano && periodo >= ANUAL) {
                if (tarefa.getListAlunosFizeram().contains(aluno.getNome()))
                    mapQtdTarefas.put("qtdFizeram", mapQtdTarefas.get("qtdFizeram") + 1);
                if (tarefa.getListAlunosNaoFizeram().contains(aluno.getNome()))
                    mapQtdTarefas.put("qtdNaoFizeram", mapQtdTarefas.get("qtdNaoFizeram") + 1);
            }
        }

        generateChartData();

    }

    public void setListeners() {
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (firstSpinnerLastPos != position) {
                    Log.i("Teste", "changed first spinner");
                    firstSpinnerLastPos = position;
                    hasFirstSpinnerIdChanged = true;
                    if (position == SEMANAL) {
                        firstTimeLoading = true;
                    }
                    generateChart(position);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        secondSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (secondSpinnerLastPos != position) {
                    Log.i("Teste", "changed second spinner");
                    hasSecondSpinnerIdChanged = true;
                    generateChart(spinner.getSelectedItemPosition());
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        thirdSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (thirdSpinnerLastPos != position) {
                    Log.i("Teste", "changed third spinner");
                    thirdSpinnerLastPos = position;
                    hasThirdSpinnerIdChanged = true;
                    generateChart(spinner.getSelectedItemPosition());
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    public void recuperarTarefas(int mes) {

        int mesFinal;
        if (secondSpinner.getSelectedItem().toString().equals("1º semestre") || secondSpinner.getSelectedItem().toString().equals("2º semestre")) { //se passar uma string quer dizer que é semestre
            if (secondSpinner.getSelectedItem().toString().equals("1º semestre")) {
                mes = 0;
                mesFinal = 5;
            } else {
                mes = 6;
                mesFinal = 11;
            }
        }
        else if(spinner.getSelectedItemPosition() == ANUAL){
            Log.i("Teste", "anual: "+secondSpinner.getSelectedItem().toString());
            mes = 0;
            mesFinal = 11;
        }
        else
            mesFinal = mes;

        listTarefas.clear();
        for (; mes <= mesFinal; mes++) {
            Calendar calendar = Calendar.getInstance();
            int finalMes = mes;
            firebaseRef.child("tarefa")
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @RequiresApi(api = Build.VERSION_CODES.N)
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {

                            String ano;
                            if(spinner.getSelectedItemPosition() == ANUAL)
                                ano = secondSpinner.getSelectedItem().toString();
                            else
                                ano = new SimpleDateFormat("yyyy").format(calendar.getTime());

                            Log.i("Teste", "ano: "+ano);

                            for (DataSnapshot tmpDado : snapshot
                                    .child(ano)
                                    .child(mapMeses.get(finalMes))
                                    .getChildren()) {
                                for (DataSnapshot dados : tmpDado.getChildren()) {
                                    Tarefa tarefa = dados.getValue(Tarefa.class);
                                    tarefa.setKey(dados.getKey());
                                    ArrayList listTmpFizeram = (ArrayList) dados.child("Alunos que fizeram").getValue();
                                    if (listTmpFizeram == null) {
                                        listTmpFizeram = new ArrayList();
                                        for (Aluno aluno : getListAlunos()) {
                                            listTmpFizeram.add(aluno.getNome());
                                        }
                                        tarefa.setListAlunosFizeram(listTmpFizeram);
                                    } else {
                                        tarefa.setListAlunosFizeram((ArrayList) dados.child("Alunos que fizeram").getValue());
                                        if (dados.child("Alunos que não fizeram").getValue() != null)
                                            tarefa.setListAlunosNaoFizeram((ArrayList) dados.child("Alunos que não fizeram").getValue());
                                    }
                                    for(Tarefa tarefaTmp : listTarefas){
                                        if(tarefa.getKey() == tarefaTmp.getKey())
                                            return;
                                    }
                                    listTarefas.add(tarefa);
                                }
                            }
                            if(finalMes == mesFinal)
                                getQtdTarefas(spinner.getSelectedItemPosition());
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
        }

    }

}