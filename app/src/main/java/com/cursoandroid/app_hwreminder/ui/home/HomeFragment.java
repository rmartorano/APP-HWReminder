package com.cursoandroid.app_hwreminder.ui.home;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cursoandroid.app_hwreminder.R;
import com.cursoandroid.app_hwreminder.activity.MainActivity;
import com.cursoandroid.app_hwreminder.adapter.AdapterAluno;
import com.cursoandroid.app_hwreminder.adapter.AdapterTarefa;
import com.cursoandroid.app_hwreminder.config.ConfiguracaoFirebase;
import com.cursoandroid.app_hwreminder.config.HomeFragmentConfigs;
import com.cursoandroid.app_hwreminder.model.Aluno;
import com.cursoandroid.app_hwreminder.model.Tarefa;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Text;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public final class HomeFragment extends Fragment {

    private AdapterTarefa adapterTarefa;
    private AdapterAluno adapterAluno;
    private static List<Tarefa> tarefas = new ArrayList<>();
    private static List<Aluno> alunos = new ArrayList<>();
    private final DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDatabase();
    private int week;
    private static String monthLastTarefaModified = " ", yearLastTarefaModified = " ", weekIntervalLastTarefaModified = " ", lastTurmaModified = " ";
    private com.cursoandroid.app_hwreminder.config.Date dateFromProject = new com.cursoandroid.app_hwreminder.config.Date();
    private ChildEventListener tarefaChildEventListener;
    private String user = ConfiguracaoFirebase.getFirebaseAutenticacao().getCurrentUser().getUid();
    private ProgressBar indeterminateBar;
    private static Button buttonSalvarAlteracoes, buttonCancelarAlteracoes;
    private static boolean anyChange = false, firstLoading = true;
    private boolean finishedUpdate = false;

    private TextView textViewDescricao;

    public HomeFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @SuppressLint("ClickableViewAccessibility")
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Log.i("Teste", "onViewCreated");

        TextView textDiaSemana = view.findViewById(R.id.textViewDiaSemana);
        TextView textViewSemanaHome = view.findViewById(R.id.textViewSemanaHome);
        TextView textViewTituloLista = view.findViewById(R.id.textViewTituloLista);
        RecyclerView recyclerViewAluno = view.findViewById(R.id.recyclerViewAluno);
        buttonSalvarAlteracoes = view.findViewById(R.id.buttonSalvarAlteracoes);
        buttonCancelarAlteracoes = view.findViewById(R.id.buttonCancelarAlteracoes);
        //FirebaseDatabase.getInstance().setLogLevel(Logger.Level.DEBUG); //usar para debug

        //Change title from month to month, day every day and set currently week interval from monday to friday

        Calendar calendar = dateFromProject.getCalendar();

        textViewTituloLista.setText("Dever de casa - " + dateFromProject.getMonthString());
        textDiaSemana.setText(StringUtils.capitalize(new SimpleDateFormat("EEEE", new java.util.Locale("pt", "BR"))
                .format(Calendar.getInstance().getTime())));
        formatDayColorDaily(view); // change color of today to focus on that
        week = calendar.get(Calendar.DAY_OF_WEEK_IN_MONTH); // week of the year
        textViewSemanaHome.setText(dateFromProject.getWeekInterval()); // retorna a string formata do intervalo da semana

        //Config adapters
        adapterTarefa = new AdapterTarefa(tarefas, getContext());
        adapterAluno = new AdapterAluno(alunos, getContext());

        //Config RecyclerView alunos
        RecyclerView.LayoutManager layoutManagerAluno = new LinearLayoutManager(getContext());
        recyclerViewAluno.setLayoutManager(layoutManagerAluno);
        recyclerViewAluno.setHasFixedSize(true);
        recyclerViewAluno.setAdapter(adapterAluno);
        recyclerViewAluno.addItemDecoration(new DividerItemDecoration(recyclerViewAluno.getContext(), DividerItemDecoration.VERTICAL));

        textViewSemanaHome.setOnTouchListener(new View.OnTouchListener() {
            final int DRAWABLE_LEFT = 0;
            final int DRAWABLE_TOP = 1;
            final int DRAWABLE_RIGHT = 2;
            final int DRAWABLE_BOTTOM = 3;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    Calendar calendarProject = dateFromProject.getCalendar();
                    if (textViewSemanaHome.getRight() - event.getRawX() <= 200) { // clicando na direita
                        salvarListTarefasEResetBotaoSalvar();
                        calendarProject.set(Calendar.DAY_OF_WEEK_IN_MONTH, calendarProject.get(Calendar.DAY_OF_WEEK_IN_MONTH) + 1);
                        dateFromProject.setSextaInMili(dateFromProject.getSexta().getTimeInMillis() + 604800000);
                        dateFromProject.setCalendarTime(calendarProject.getTime());
                        saveWeekChangeData(textViewSemanaHome, textViewTituloLista, calendarProject);
                        return true;
                    } else if (event.getRawX() >= (textViewSemanaHome.getLeft() -
                            textViewSemanaHome.getCompoundDrawables()[DRAWABLE_LEFT].getBounds().width()) &&
                            (textViewSemanaHome.getRight() - event.getRawX()) > 600) { //clicando na esquerda
                        salvarListTarefasEResetBotaoSalvar();
                        calendarProject.set(Calendar.DAY_OF_WEEK_IN_MONTH, calendarProject.get(Calendar.DAY_OF_WEEK_IN_MONTH) - 1);
                        dateFromProject.setSextaInMili(dateFromProject.getSexta().getTimeInMillis() - 604800000);
                        dateFromProject.setCalendarTime(calendarProject.getTime());
                        saveWeekChangeData(textViewSemanaHome, textViewTituloLista, calendarProject);
                        return true;
                    } else {//meio
                        DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                                salvarListTarefasEResetBotaoSalvar();
                                calendarProject.set(Calendar.YEAR, year);
                                calendarProject.set(Calendar.MONTH, month);
                                calendarProject.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                                calendarProject.add(Calendar.DAY_OF_WEEK, -calendarProject.get(Calendar.DAY_OF_WEEK) + Calendar.MONDAY);
                                long sextaMili = calendarProject.getTimeInMillis() + Long.parseLong("345600000");
                                dateFromProject.setSextaInMili(sextaMili);
                                textViewTituloLista.setText("Dever de casa - " + dateFromProject.getMonthString());
                                dateFromProject.setCalendarTime(calendarProject.getTime());
                                saveWeekChangeData(textViewSemanaHome, textViewTituloLista, calendarProject);
                            }
                        };

                        new DatePickerDialog(getContext(), date, calendarProject
                                .get(Calendar.YEAR), calendarProject.get(Calendar.MONTH),
                                calendarProject.get(Calendar.DAY_OF_MONTH)).show();
                        return true;
                    }
                }
                return false;
            }
        });

        buttonSalvarAlteracoes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                salvarListTarefasEResetBotaoSalvar();
            }
        });

        buttonCancelarAlteracoes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                anyChange = false;
                buttonSalvarAlteracoes.setVisibility(View.INVISIBLE);
                buttonCancelarAlteracoes.setVisibility(View.INVISIBLE);
                onStart();
            }
        });

    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onStart() {
        super.onStart();
        firstLoading = true;
        indeterminateBar = getView().findViewById(R.id.indeterminateBarFragmentHome);
        indeterminateBar.setIndeterminateTintList(ColorStateList.valueOf(Color.RED));
        indeterminateBar.setVisibility(View.VISIBLE);
        if (tarefaChildEventListener != null)
            removerListeners();
        recuperarConfigs();
        final Handler handler = new Handler(); //checa depois de 2s se as listas estão vazias, caso positivo remove o progressbar
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (alunos.isEmpty() && tarefas.isEmpty())
                    indeterminateBar.setVisibility(View.INVISIBLE);
            }
        }, 2000);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void saveWeekChangeData(TextView textViewSemanaHome, TextView textViewTituloLista, Calendar calendarProject) {
        indeterminateBar.setVisibility(View.VISIBLE);
        if (finishedUpdate || !anyChange) {
            weekIntervalLastTarefaModified = dateFromProject.getWeekIntervalAsChildString();
            monthLastTarefaModified = dateFromProject.getMonthString();
            textViewSemanaHome.setText(dateFromProject.getWeekInterval());
            week = calendarProject.get(Calendar.DAY_OF_WEEK_IN_MONTH);
            textViewTituloLista.setText("Dever de casa - " + dateFromProject.getMonthString());
            HomeFragmentConfigs.salvarConfigs();
            finishedUpdate = false;
            Log.i("Teste", "finished saving");
            onStart();
        }
        else { //chama o método novamente pra checar se já terminou de salvar as alterações
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    saveWeekChangeData(textViewSemanaHome, textViewTituloLista, calendarProject);
                }
            }, 1000);
        }
    }

    public void removerListeners() {
        //firebaseRef.removeEventListener(alunoEventListener);
        firebaseRef.removeEventListener(tarefaChildEventListener);
    }

    @Override
    public void onPause() {
        super.onPause();
        HomeFragmentConfigs.salvarConfigs();
    }

    public void recuperarConfigs() {
        firebaseRef.child(user).child("Configurações HomeFragment").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() == null) {
                    HomeFragment.setYearLastTarefaModified(" ");
                    HomeFragment.setMonthLastTarefaModified(" ");
                    HomeFragment.setWeekIntervalLastTarefaModified(" ");
                    HomeFragment.setLastTurmaModified(" ");
                    return;
                }
                HomeFragment.setYearLastTarefaModified(snapshot.child("geral").child("ano").getValue().toString());
                HomeFragment.setMonthLastTarefaModified(snapshot.child("geral").child("mes").getValue().toString());
                HomeFragment.setWeekIntervalLastTarefaModified(snapshot.child("geral").child("intervalo da semana").getValue().toString());
                HomeFragment.setLastTurmaModified(snapshot.child("geral").child("turma").getValue().toString());
                recuperarTurmas();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void recuperarTurmas() {
        Spinner spinnerTurmaHome = getView().findViewById(R.id.spinnerTurmaHome);
        List<String> spinnerArray = new ArrayList<String>();
        firebaseRef.child(user).child("Configurações HomeFragment").child("turmas").child(dateFromProject.getYearString()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot turma : snapshot.getChildren()) {
                    Log.i("Teste", "turma: " + turma.getValue());
                    if (!turma.getValue().equals("Selecionar turma")) {
                        spinnerArray.add(turma.getValue().toString());
                    }
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                        getContext(), android.R.layout.simple_spinner_item, spinnerArray);
                spinnerTurmaHome.setAdapter(adapter);
                if (!lastTurmaModified.equals("")) {
                    for (int i = 0; i < spinnerTurmaHome.getCount(); i++) {
                        if (spinnerTurmaHome.getItemAtPosition(i).toString().equals(lastTurmaModified)) {
                            spinnerTurmaHome.setSelection(i);
                            break;
                        }
                    }
                }
                spinnerTurmaHome.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @RequiresApi(api = Build.VERSION_CODES.N)
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        if (spinnerTurmaHome.getSelectedItem().toString().equalsIgnoreCase(lastTurmaModified))
                            return;
                        lastTurmaModified = spinnerTurmaHome.getSelectedItem().toString();
                        HomeFragmentConfigs.salvarConfigs();
                        onStart();
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });
                recuperarListaAlunos();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void addListeners() {

        //listener para quando uma tarefa for adicionada
        tarefaChildEventListener = firebaseRef.child(user).child("tarefa").child(yearLastTarefaModified).child(monthLastTarefaModified).child(weekIntervalLastTarefaModified).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if (!alunos.isEmpty()) {
                    Tarefa tarefa = snapshot.getValue(Tarefa.class);
                    tarefa.setKey(snapshot.getKey());
                    List<String> listTmpFizeram = (List<String>) snapshot.child("Alunos que fizeram").getValue();
                    List<String> listTmpNaoFizeram = (List<String>) snapshot.child("Alunos que não fizeram").getValue();

                    if (listTmpFizeram == null && listTmpNaoFizeram == null) {
                        for (Aluno aluno : alunos) {
                            tarefa.addToListAlunosFizeram(aluno.getNome());
                        }
                        try {
                            tarefa.salvarListas();
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    } else {
                        if (listTmpFizeram != null) {
                            tarefa.setListAlunosFizeram(listTmpFizeram);
                        }
                        if (listTmpNaoFizeram != null)
                            tarefa.setListAlunosNaoFizeram(listTmpNaoFizeram);
                    }
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void recuperarTarefas() {
        firebaseRef.child(user).child("tarefa").addListenerForSingleValueEvent(new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                tarefas.clear();
                Log.i("Teste", "recuperar tarefas on change");
                Calendar calendar = Calendar.getInstance();
                Date date;
                String diaSemana = new String();
                TextView textViewDiaSemana = getView().findViewById(R.id.textViewSegunda);
                textViewDiaSemana.setText("Sem tarefa");
                textViewDiaSemana = getView().findViewById(R.id.textViewTerca);
                textViewDiaSemana.setText("Sem tarefa");
                textViewDiaSemana = getView().findViewById(R.id.textViewQuarta);
                textViewDiaSemana.setText("Sem tarefa");
                textViewDiaSemana = getView().findViewById(R.id.textViewQuinta);
                textViewDiaSemana.setText("Sem tarefa");
                textViewDiaSemana = getView().findViewById(R.id.textViewSexta);
                textViewDiaSemana.setText("Sem tarefa");
                tarefas.clear();
                for (DataSnapshot tmpDados : snapshot.child(dateFromProject.getYearString()).child(dateFromProject.getMonthString()).getChildren()) { // recupera apenas as tarefas dentro de 1 mês
                    for (DataSnapshot dados : tmpDados.getChildren()) {
                        Tarefa tarefa = dados.getValue(Tarefa.class);
                        tarefa.setKey(dados.getKey());
                        try {
                            calendar.setTime(new SimpleDateFormat("dd/MM/yyyy", new java.util.Locale("pt", "BR")).parse(tarefa.getDataEntrega()));
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        calendar.set(Calendar.DAY_OF_WEEK, calendar.getFirstDayOfWeek());
                        calendar.setTimeInMillis(calendar.getTimeInMillis() + Long.parseLong("86400000")); // configura pro primeiro dia da semana ser segunda
                        //Log.i("Teste", "calendar week: " + calendar.get(Calendar.DAY_OF_WEEK_IN_MONTH) + " week: " + week + " tarefa: " + tarefa.getTitulo());
                        if (calendar.get(Calendar.DAY_OF_WEEK_IN_MONTH) == week && tarefa.getTurma().equals(lastTurmaModified)) { // only shows currently week tarefas

                            //get weekDay string from tarefa
                            try {
                                date = new SimpleDateFormat("dd/MM/yyyy", new java.util.Locale("pt", "BR")).parse(tarefa.getDataEntrega());
                                diaSemana = new SimpleDateFormat("EE", new java.util.Locale("pt", "BR")).format(date).replaceAll("\\.", "");
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }

                            //Instanciar alertDialog
                            AlertDialog.Builder dialog = new AlertDialog.Builder(getContext());
                            switch (diaSemana.toLowerCase()) {
                                case "seg":
                                    textViewDescricao = getView().findViewById(R.id.textViewSegunda);
                                    break;
                                case "ter":
                                    textViewDescricao = getView().findViewById(R.id.textViewTerca);
                                    break;
                                case "qua":
                                    textViewDescricao = getView().findViewById(R.id.textViewQuarta);
                                    break;
                                case "qui":
                                    textViewDescricao = getView().findViewById(R.id.textViewQuinta);
                                    break;
                                case "sex":
                                    textViewDescricao = getView().findViewById(R.id.textViewSexta);
                                    break;
                                default:
                                    throw new IllegalStateException("Unexpected value: " + diaSemana);
                            }

                            if (tarefa.getDescricao().length() > 40) {
                                String tmp = tarefa.getDescricao().substring(0, 40) + "...";
                                textViewDescricao.setText(tmp);
                            } else
                                textViewDescricao.setText(tarefa.getDescricao());


                            dialog.setTitle(tarefa.getTitulo());
                            dialog.setMessage(tarefa.getDescricao());
                            dialog.setIcon(R.drawable.ic_menu_book);
                            //Config ações para sim ou não
                            dialog.setPositiveButton("Editar", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int which) { //on clicking to edit, will open a new dialogAlert
                                    AlertDialog.Builder dialogEditDescricao = new AlertDialog.Builder(getContext());
                                    dialogEditDescricao.setTitle("Editar " + tarefa.getTitulo());
                                    dialogEditDescricao.setMessage("Digite a nova descrição");
                                    //Config input view
                                    final EditText input = new EditText(getContext());
                                    input.setInputType(InputType.TYPE_CLASS_TEXT);
                                    dialogEditDescricao.setView(input);

                                    if (tarefa.getDescricao().length() > 40) {
                                        String tmp = tarefa.getDescricao().substring(0, 40) + "...";
                                        input.setHint(tmp);
                                    } else
                                        input.setHint(tarefa.getDescricao());
                                    dialogEditDescricao.setIcon(R.drawable.ic_pencil_24);
                                    dialogEditDescricao.setPositiveButton("Salvar", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int which) {
                                            Date date = new Date();
                                            String diaSemana = "";
                                            try {
                                                date = new SimpleDateFormat("dd/MM/yyyy", new java.util.Locale("pt", "BR")).parse(tarefa.getDataEntrega());
                                                diaSemana = new SimpleDateFormat("EE", new java.util.Locale("pt", "BR")).format(date).replaceAll("\\.", "");
                                            } catch (ParseException e) {
                                                e.printStackTrace();
                                            }

                                            //Instanciar alertDialog
                                            AlertDialog.Builder dialog = new AlertDialog.Builder(getContext());
                                            switch (diaSemana) {
                                                case "seg":
                                                    textViewDescricao = getView().findViewById(R.id.textViewSegunda);
                                                    break;
                                                case "ter":
                                                    textViewDescricao = getView().findViewById(R.id.textViewTerca);
                                                    break;
                                                case "qua":
                                                    textViewDescricao = getView().findViewById(R.id.textViewQuarta);
                                                    break;
                                                case "qui":
                                                    textViewDescricao = getView().findViewById(R.id.textViewQuinta);
                                                    break;
                                                case "sex":
                                                    textViewDescricao = getView().findViewById(R.id.textViewSexta);
                                                    break;
                                                default:
                                                    throw new IllegalStateException("Unexpected value: " + diaSemana);
                                            }
                                            try {
                                                firebaseRef.child(user).child("tarefa")
                                                        .child(tarefa.getYearString())
                                                        .child(tarefa.getMonthString())
                                                        .child(tarefa.getWeekIntervalAsChildString())
                                                        .child(tarefa.getKey())
                                                        .child("descricao")
                                                        .setValue(input.getText().toString());
                                            } catch (ParseException e) {
                                                e.printStackTrace();
                                            }
                                            tarefa.setDescricao(input.getText().toString());
                                            textViewDescricao.setText(input.getText().toString());
                                            dialog.setMessage(tarefa.getDescricao());
                                        }
                                    });
                                    dialogEditDescricao.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.cancel();
                                        }
                                    });
                                    dialogEditDescricao.create();
                                    dialogEditDescricao.show();
                                }
                            });
                            dialog.setNegativeButton("Remover", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    AlertDialog.Builder dialogConfirmRemoval = new AlertDialog.Builder(getContext());
                                    dialogConfirmRemoval.setMessage("Deseja mesmo remover '" + tarefa.getTitulo() + "'?");
                                    dialogConfirmRemoval.setPositiveButton("Remover", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            Toast.makeText(getContext(), "Tarefa " + tarefa.getTitulo() + " removida", Toast.LENGTH_SHORT).show();
                                            try {
                                                tarefa.deletarTarefa();
                                            } catch (ParseException e) {
                                                e.printStackTrace();
                                            }
                                            textViewDescricao.setText("Sem tarefa");
                                            textViewDescricao.setOnClickListener(null); // remove the click listener
                                        }
                                    });
                                    dialogConfirmRemoval.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.cancel();
                                        }
                                    });
                                    dialogConfirmRemoval.create();
                                    dialogConfirmRemoval.show();
                                }
                            });

                            //set click listener to open dialog when clicking the description of tarefa
                            textViewDescricao.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    //Criar e exibir AlertDialog
                                    dialog.create();
                                    dialog.show();
                                }
                            });

                        }
                        tarefas.add(tarefa);
                        List<String> listTmpFizeram = (List<String>) dados.child("Alunos que fizeram").getValue();
                        List<String> listTmpNaoFizeram = (List<String>) dados.child("Alunos que não fizeram").getValue();

                        if (listTmpFizeram == null && listTmpNaoFizeram == null) {
                            for (Aluno aluno : alunos) {
                                tarefa.addToListAlunosFizeram(aluno.getNome());
                            }
                            try {
                                tarefa.salvarListas();
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                        } else {
                            if (listTmpFizeram != null) {
                                tarefa.setListAlunosFizeram(listTmpFizeram);
                            }
                            if (listTmpNaoFizeram != null)
                                tarefa.setListAlunosNaoFizeram(listTmpNaoFizeram);
                        }
                    }
                }
                adapterTarefa.notifyDataSetChanged();
                addListeners();
                indeterminateBar.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void recuperarListaAlunos() {
        Calendar calendar = Calendar.getInstance();
        firebaseRef.child(user).child("aluno").child(String.valueOf(calendar.get(Calendar.YEAR))).child(lastTurmaModified).addListenerForSingleValueEvent(new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                alunos.clear();
                for (DataSnapshot dados : snapshot.getChildren()) {
                    Aluno aluno = dados.getValue(Aluno.class);
                    alunos.add(aluno);
                }
                alunos.sort(Comparator.comparing(Aluno::getNome));
                adapterAluno.notifyDataSetChanged();
                recuperarTarefas();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void formatDayColorDaily(View view) {
        Calendar c = Calendar.getInstance();
        String today = new SimpleDateFormat("EE", new java.util.Locale("pt", "BR")).format(c.getTime()).replaceAll("\\.", "");
        TextView textView = null;
        switch (today) {
            case "ter":
                textView = view.findViewById(R.id.textViewTercaFix);
                break;
            case "qua":
                textView = view.findViewById(R.id.textViewQuartaFix);
                break;
            case "qui":
                textView = view.findViewById(R.id.textViewQuintaFix);
                break;
            case "sex":
                textView = view.findViewById(R.id.textViewSextaFix);
                break;
            default:
                textView = view.findViewById(R.id.textViewSegundaFix);
                break;
        }
        textView.setTextColor(getResources().getColor(R.color.teal_200));
    }

    static int contadorRecursao = 0;

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void updateFrequenciaTarefa(Tarefa tarefa, Aluno aluno) throws ParseException {
        String diaSemana = new String();
        Date date = null;
        //get weekDay string from tarefa
        try {
            date = new SimpleDateFormat("dd/MM/yyyy", new java.util.Locale("pt", "BR")).parse(tarefa.getDataEntrega());
            diaSemana = new SimpleDateFormat("EE", new java.util.Locale("pt", "BR")).format(date).replaceAll("\\.", "");
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.setMinimalDaysInFirstWeek(7);
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        if (dateFromProject.getCalendar().get(Calendar.DAY_OF_WEEK_IN_MONTH) != calendar.get(Calendar.DAY_OF_WEEK_IN_MONTH))
            return;
        String finalDiaSemana = diaSemana;
        String yearString = tarefa.getYearString();
        String nomeAluno = aluno.getNome();
        firebaseRef.child(user).child("aluno")
                .child(yearString)
                .child(aluno.getTurma())
                .child(nomeAluno)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @RequiresApi(api = Build.VERSION_CODES.N)
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        switch (finalDiaSemana) {
                            case "seg":
                                try {
                                    validarAddFrequencia(tarefa, nomeAluno, aluno.isCheckedBoxSegunda());
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                                break;
                            case "ter":
                                try {
                                    validarAddFrequencia(tarefa, nomeAluno, aluno.isCheckedBoxTerca());
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                                break;
                            case "qua":
                                try {
                                    validarAddFrequencia(tarefa, nomeAluno, aluno.isCheckedBoxQuarta());
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                                break;
                            case "qui":
                                try {
                                    validarAddFrequencia(tarefa, nomeAluno, aluno.isCheckedBoxQuinta());
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                                break;
                            case "sex":
                                try {
                                    validarAddFrequencia(tarefa, nomeAluno, aluno.isCheckedBoxSexta());
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                                break;
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
        contadorRecursao++;
        if (alunos.size() > contadorRecursao) {
            updateFrequenciaTarefa(tarefa, alunos.get(contadorRecursao));
        } else{
            contadorRecursao = 0;
            finishedUpdate = true;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void validarAddFrequencia(Tarefa tarefa, String nomeAluno, boolean fezTarefa) throws ParseException {
        List<String> listFizeram = tarefa.getListAlunosFizeram();
        List<String> listNaoFizeram = tarefa.getListAlunosNaoFizeram();

        boolean listChanged = false;

        if (listFizeram.contains(nomeAluno)) {
            if (!fezTarefa) {
                tarefa.removeFromListAlunosFizeram(nomeAluno);
                listChanged = true;
            }
        }
        if (!listFizeram.contains(nomeAluno)) {
            if (fezTarefa) {
                tarefa.addToListAlunosFizeram(nomeAluno);
                listChanged = true;
            }
        }

        if (listNaoFizeram.contains(nomeAluno)) {
            if (fezTarefa) {
                tarefa.removeFromListAlunosNaoFizeram(nomeAluno);
                listChanged = true;
            }
        }
        if (!listNaoFizeram.contains(nomeAluno)) {
            if (!fezTarefa) {
                tarefa.addToListAlunosNaoFizeram(nomeAluno);
                listChanged = true;
            }
        }
        if (listChanged) {
            tarefa.salvarListas();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void salvarListTarefasEResetBotaoSalvar() {
        if (anyChange) {
            for (Tarefa tarefa : tarefas) {
                try {
                    updateFrequenciaTarefa(tarefa, alunos.get(0));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
            for (Aluno aluno : alunos) {
                aluno.salvarCheckBox();
            }
            buttonSalvarAlteracoes.setVisibility(View.INVISIBLE);
            buttonCancelarAlteracoes.setVisibility(View.INVISIBLE);
            anyChange = false;
        }
    }

    public static void setTarefas(List<Tarefa> tarefas) {
        HomeFragment.tarefas = tarefas;
    }

    public static void setAlunos(List<Aluno> alunos) {
        HomeFragment.alunos = alunos;
    }

    public static List<Tarefa> getListTarefas() {
        return tarefas;
    }

    public static List<Aluno> getListAlunos() {
        return alunos;
    }

    public static void setMonthLastTarefaModified(String monthLastTarefaAdded) {
        HomeFragment.monthLastTarefaModified = monthLastTarefaAdded;
    }

    public static void setYearLastTarefaModified(String yearLastTarefaAdded) {
        HomeFragment.yearLastTarefaModified = yearLastTarefaAdded;
    }

    public static void setWeekIntervalLastTarefaModified(String weekIntervalLastTarefaAdded) {
        HomeFragment.weekIntervalLastTarefaModified = weekIntervalLastTarefaAdded;
    }

    public static String getMonthLastTarefaModified() {
        return monthLastTarefaModified;
    }

    public static String getYearLastTarefaModified() {
        return yearLastTarefaModified;
    }

    public static String getWeekIntervalLastTarefaModified() {
        return weekIntervalLastTarefaModified;
    }

    public static String getLastTurmaModified() {
        return lastTurmaModified;
    }

    public static void setLastTurmaModified(String turma) {
        lastTurmaModified = turma;
    }

    public static void setAnyChange(boolean anyChange) {
        HomeFragment.anyChange = anyChange;
        if (anyChange && buttonSalvarAlteracoes.getVisibility() != View.VISIBLE && !firstLoading) {
            DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDatabase();
            FirebaseAuth auth = ConfiguracaoFirebase.getFirebaseAutenticacao();
            final boolean[] hasChanged = {false};
            for (Aluno aluno : alunos) {
                if (hasChanged[0])
                    break;
                Log.i("Teste", "Starting loop");
                firebaseRef.child(auth.getCurrentUser()
                        .getUid())
                        .child("aluno")
                        .child(com.cursoandroid.app_hwreminder.config.Date.getYearString())
                        .child(lastTurmaModified)
                        .child(aluno.getNome())
                        .child("checkBoxes")
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {

                                if (
                                        aluno.isCheckedBoxSegunda() != (Boolean) snapshot.child("checkedBoxSegunda").getValue()
                                                || aluno.isCheckedBoxTerca() != (Boolean) snapshot.child("checkedBoxTerca").getValue()
                                                || aluno.isCheckedBoxQuarta() != (Boolean) snapshot.child("checkedBoxQuarta").getValue()
                                                || aluno.isCheckedBoxQuinta() != (Boolean) snapshot.child("checkedBoxQuinta").getValue()
                                                || aluno.isCheckedBoxSexta() != (Boolean) snapshot.child("checkedBoxSexta").getValue()
                                )
                                    hasChanged[0] = true;

                                if (hasChanged[0]) {
                                    Log.i("Teste", "deve parar aqui");
                                    buttonSalvarAlteracoes.setVisibility(View.VISIBLE);
                                    buttonCancelarAlteracoes.setVisibility(View.VISIBLE);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
            }
        }
    }

    public static void setFirstLoading(boolean value) {
        HomeFragment.firstLoading = value;
    }

}