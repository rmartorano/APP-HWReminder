package com.cursoandroid.app_hwreminder.ui.aluno;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.TextView;

import com.cursoandroid.app_hwreminder.R;
import com.cursoandroid.app_hwreminder.adapter.AdapterAlunosMaisFazem;
import com.cursoandroid.app_hwreminder.config.ConfiguracaoFirebase;
import com.cursoandroid.app_hwreminder.config.Date;
import com.cursoandroid.app_hwreminder.model.Aluno;
import com.cursoandroid.app_hwreminder.model.Tarefa;
import com.cursoandroid.app_hwreminder.ui.home.HomeFragment;
import com.github.mikephil.charting.charts.PieChart;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.cursoandroid.app_hwreminder.ui.home.HomeFragment.getListAlunos;

public class InfoAlunoActivity extends AppCompatActivity {

    private Aluno aluno;
    private final List<Aluno> listAlunos = HomeFragment.getListAlunos(), alunosFiltro = new ArrayList<>();
    private List<Tarefa> listTarefas = HomeFragment.getListTarefas();
    private Spinner spinner, secondSpinner, thirdSpinner, spinnerOrdenar;
    private boolean hasSecondSpinnerIdChanged = false, hasFirstSpinnerIdChanged = false;
    private boolean firstTimeLoading = true;
    private int firstSpinnerLastPos = 0, secondSpinnerLastPos, thirdSpinnerLastPos;
    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDatabase();
    private ProgressBar indeterminateBar;
    private AdapterAlunosMaisFazem adapterAlunosMaisFazem;

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

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info_aluno);
        setTitle("Acompanhamento mensal da turma");

        spinner = findViewById(R.id.spinnerFiltroInfoAluno);
        secondSpinner = findViewById(R.id.secondSpinnerInfoAluno);
        thirdSpinner = findViewById(R.id.thirdSpinnerInfoAluno);
        indeterminateBar = findViewById(R.id.indeterminateBarInfoAluno);
        indeterminateBar.setIndeterminateTintList(ColorStateList.valueOf(getResources().getColor(R.color.red_light)));
        //TextView textViewTurma = findViewById(R.id.textViewTurmaActivityInfoAluno);

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

        //textViewTurma.setText("Turma: "+aluno.getTurma());

        spinnerOrdenar = findViewById(R.id.spinnerOrdenaraAcompanhamentoFizeram);
        ArrayList<String> spinnerArray = new ArrayList<>();
        spinnerArray.add("Nome");
        spinnerArray.add("Tarefas feitas");
        spinnerArray.add("Tarefas não feitas");
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<>(this, R.layout.spinner_item, spinnerArray);
        spinnerOrdenar.setAdapter(spinnerArrayAdapter);
        SearchView searchAluno = findViewById(R.id.searchViewAlunoAcompanhamento);
        adapterAlunosMaisFazem = new AdapterAlunosMaisFazem
                (alunosFiltro, HomeFragment.getListTarefas(), this);

        RecyclerView recyclerView = findViewById(R.id.recyclerViewMaisFizeram);
        RecyclerView.LayoutManager layoutManagerAluno = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManagerAluno);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(adapterAlunosMaisFazem);

        final String[] lastText = {""};
        filtrarAlunos("", adapterAlunosMaisFazem);
        searchAluno.setIconified(false);
        searchAluno.clearFocus();
        searchAluno.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filtrarAlunos(newText, adapterAlunosMaisFazem);
                lastText[0] = newText;
                return true;
            }
        });

        spinnerOrdenar.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                filtrarAlunos(lastText[0], adapterAlunosMaisFazem);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        //prenche os spinners com valores iniciais, na config de semanalmente
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);

        //configura spinners
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.array_filtro_info_aluno, R.layout.spinner_item);
        spinner.setAdapter(adapter);

        adapter = ArrayAdapter.createFromResource(this,
                R.array.array_filtro_info_aluno_meses, R.layout.spinner_item);
        secondSpinner.setAdapter(adapter);

        switch (new SimpleDateFormat("MMMM", new java.util.Locale("pt","BR")).format(calendar.getTime()).toLowerCase()) {
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

    @RequiresApi(api = Build.VERSION_CODES.N)
    void filtrarAlunos(String query, AdapterAlunosMaisFazem adapterAlunosMaisFazem){

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

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void generateChart(int periodo) {

        indeterminateBar.setVisibility(View.VISIBLE);
        fillSpinners(periodo);
        if (firstTimeLoading) {
            setListeners();
            firstTimeLoading = false;
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void fillSpinners(int periodo) {

        switch (periodo) {

            case SEMANAL: { //preenche terceiro spinner com os intervalos da semana
                Log.i("Teste", "semanal");
                ArrayList<String> spinnerArray = new ArrayList<>();
                Calendar calendar = Calendar.getInstance();
                Date date = new Date();
                if (hasSecondSpinnerIdChanged || hasFirstSpinnerIdChanged || firstTimeLoading) {
                    if (secondSpinner.getCount() < 12) {
                        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                                R.array.array_filtro_info_aluno_meses, R.layout.spinner_item);
                        secondSpinner.setAdapter(adapter);
                        secondSpinner.setSelection(calendar.get(Calendar.MONTH));
                    }
                    listTarefas = HomeFragment.getListTarefas();
                    calendar.set(Calendar.MONTH, secondSpinner.getSelectedItemPosition());
                    Map<String, String> mapIntervals = date.getAllWeekIntervals(secondSpinner.getSelectedItemPosition()); //recupera todos os intervalos da semana do mês
                    for (int i = 0; i < mapIntervals.size(); i++) {
                        spinnerArray.add(mapIntervals.get("Semana " + (i + 1)));
                    }
                    ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<>(
                            this, R.layout.spinner_item,
                            spinnerArray);
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
                                R.array.array_filtro_info_aluno_meses, R.layout.spinner_item);
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
                                this, R.layout.spinner_item,
                                spinnerArray);
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

                    firebaseRef
                            .child(ConfiguracaoFirebase.getFirebaseAutenticacao().getCurrentUser().getUid())
                            .child("tarefa").addListenerForSingleValueEvent(new ValueEventListener() {
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
                                        getApplicationContext(), R.layout.spinner_item,
                                        spinnerArray);
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

        Log.i("Teste", "tarefas size: " + listTarefas.size());

        for(Aluno aluno : alunosFiltro) {
            int qtdTarefasFeitas = 0, qtdTarefasNaoFeitas = 0;
            for (Tarefa tarefa : listTarefas) {
                try {
                    calendarTarefa.setTime(new SimpleDateFormat("dd/MM/yyyy", new java.util.Locale("pt", "BR")).parse(tarefa.getDataEntrega()));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                calendarTarefa.setMinimalDaysInFirstWeek(7);
                calendarTarefa.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
                Log.i("Teste", "semana: " + semana + " semana tarefa: " + calendarTarefa.get(Calendar.DAY_OF_WEEK_IN_MONTH));
                if (calendarTarefa.get(Calendar.DAY_OF_WEEK_IN_MONTH) == semana && periodo == SEMANAL && calendarTarefa.get(Calendar.MONTH) + 1 == mes) {
                    if (tarefa.getListAlunosFizeram().contains(aluno.getNome())) {
                        qtdTarefasFeitas++;
                    }
                    if (tarefa.getListAlunosNaoFizeram().contains(aluno.getNome())) {
                        qtdTarefasNaoFeitas++;
                    }
                } else if (calendarTarefa.get(Calendar.MONTH) + 1 == mes && periodo >= MENSAL && calendarTarefa.get(Calendar.YEAR) == ano) {
                    if (tarefa.getListAlunosFizeram().contains(aluno.getNome()))
                        qtdTarefasFeitas++;
                    if (tarefa.getListAlunosNaoFizeram().contains(aluno.getNome()))
                        qtdTarefasNaoFeitas++;
                } else if (calendarTarefa.get(Calendar.MONTH) + 1 >= mes - 5 && calendarTarefa.get(Calendar.YEAR) == calendar.get(Calendar.YEAR) && periodo >= SEMESTRAL) {
                    if (tarefa.getListAlunosFizeram().contains(aluno.getNome()))
                        qtdTarefasFeitas++;
                    if (tarefa.getListAlunosNaoFizeram().contains(aluno.getNome()))
                        qtdTarefasNaoFeitas++;
                } else if (calendarTarefa.get(Calendar.YEAR) == ano && periodo >= ANUAL) {
                    if (tarefa.getListAlunosFizeram().contains(aluno.getNome()))
                        qtdTarefasFeitas++;
                    if (tarefa.getListAlunosNaoFizeram().contains(aluno.getNome()))
                        qtdTarefasNaoFeitas++;
                }
            }
            aluno.setQtdProgressBarTarefasFeitas(qtdTarefasFeitas);
            aluno.setQtdProgressBarTarefasNaoFeitas(qtdTarefasNaoFeitas);
            adapterAlunosMaisFazem.notifyDataSetChanged();
            Log.i("Teste","qtd fez> "+qtdTarefasFeitas+" n: "+qtdTarefasNaoFeitas);
            indeterminateBar.setVisibility(View.GONE);
        }

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
            firebaseRef
                    .child(ConfiguracaoFirebase.getFirebaseAutenticacao().getCurrentUser().getUid())
                    .child("tarefa")
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @RequiresApi(api = Build.VERSION_CODES.N)
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {

                            String ano;
                            if(spinner.getSelectedItemPosition() == ANUAL)
                                ano = secondSpinner.getSelectedItem().toString();
                            else
                                ano = new SimpleDateFormat("yyyy", new java.util.Locale("pt","BR")).format(calendar.getTime());

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