package com.cursoandroid.app_hwreminder.ui.home;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.EditText;
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
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import org.apache.commons.lang3.StringUtils;

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
    private ChildEventListener tarefaChildEventListener, alunoEventListener;

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
        //FirebaseDatabase.getInstance().setLogLevel(Logger.Level.DEBUG); //usar para debug

        //Change title from month to month, day every day and set currently week interval from monday to friday

        com.cursoandroid.app_hwreminder.config.Date data = new com.cursoandroid.app_hwreminder.config.Date();

        Calendar calendar = data.getCalendar();

        textViewTituloLista.setText("Dever de casa - " + data.getMonthString());
        textDiaSemana.setText(StringUtils.capitalize(new SimpleDateFormat("EEEE", new java.util.Locale("pt","BR"))
                .format(Calendar.getInstance().getTime())));
        formatDayColorDaily(view); // change color of today to focus on that
        week = calendar.get(Calendar.DAY_OF_WEEK_IN_MONTH); // week of the year
        textViewSemanaHome.setText(data.getWeekInterval()); // retorna a string formata do intervalo da semana

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
                if(event.getAction() == MotionEvent.ACTION_DOWN){
                    Calendar calendarProject = dateFromProject.getCalendar();
                    if(textViewSemanaHome.getRight() - event.getRawX() <= 200){ // clicando na direita
                        calendarProject.set(Calendar.DAY_OF_WEEK_IN_MONTH, calendarProject.get(Calendar.DAY_OF_WEEK_IN_MONTH)+1);
                        dateFromProject.setSextaInMili(dateFromProject.getSexta().getTimeInMillis()+604800000);
                        weekIntervalLastTarefaModified = dateFromProject.getWeekIntervalAsChildString();
                        monthLastTarefaModified = dateFromProject.getMonthString();
                        textViewSemanaHome.setText(dateFromProject.getWeekInterval());
                        HomeFragmentConfigs.salvarConfigs();
                        week = calendarProject.get(Calendar.DAY_OF_WEEK_IN_MONTH);
                        textViewTituloLista.setText("Dever de casa - " + dateFromProject.getMonthString());
                        onStart();
                        return true;
                    }
                    else if(event.getRawX() >= (textViewSemanaHome.getLeft() -
                            textViewSemanaHome.getCompoundDrawables()[DRAWABLE_LEFT].getBounds().width()) &&
                            (textViewSemanaHome.getRight() - event.getRawX()) > 600){ //clicando na esquerda
                        calendarProject.set(Calendar.DAY_OF_WEEK_IN_MONTH, calendarProject.get(Calendar.DAY_OF_WEEK_IN_MONTH)-1);
                        dateFromProject.setSextaInMili(dateFromProject.getSexta().getTimeInMillis()-604800000);
                        weekIntervalLastTarefaModified = dateFromProject.getWeekIntervalAsChildString();
                        monthLastTarefaModified = dateFromProject.getMonthString();
                        textViewSemanaHome.setText(dateFromProject.getWeekInterval());
                        HomeFragmentConfigs.salvarConfigs();
                        week = calendarProject.get(Calendar.DAY_OF_WEEK_IN_MONTH);
                        textViewTituloLista.setText("Dever de casa - " + dateFromProject.getMonthString());
                        onStart();
                        return true;
                    }
                    else {//meio
                        DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                                // TODO Auto-generated method stub
                                calendarProject.set(Calendar.YEAR, year);
                                calendarProject.set(Calendar.MONTH, month);
                                calendarProject.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                                calendarProject.add(Calendar.DAY_OF_WEEK, -calendarProject.get(Calendar.DAY_OF_WEEK)+Calendar.MONDAY);
                                Log.i("Teste", "date week: "+calendarProject.get(Calendar.DAY_OF_WEEK_IN_MONTH));
                                long sextaMili = calendarProject.getTimeInMillis() + Long.parseLong("345600000");
                                dateFromProject.setSextaInMili(sextaMili);
                                weekIntervalLastTarefaModified = dateFromProject.getWeekIntervalAsChildString();
                                monthLastTarefaModified = dateFromProject.getMonthString();
                                textViewSemanaHome.setText(dateFromProject.getWeekInterval());
                                HomeFragmentConfigs.salvarConfigs();
                                week = calendarProject.get(Calendar.DAY_OF_WEEK_IN_MONTH);
                                textViewTituloLista.setText("Dever de casa - " + dateFromProject.getMonthString());
                                onStart();
                            }
                        };

                        new DatePickerDialog(getContext(), date, calendarProject
                                .get(Calendar.YEAR), calendarProject.get(Calendar.MONTH),
                                calendarProject.get(Calendar.DAY_OF_MONTH)).show();

                        return  true;
                    }

                }
                return false;
            }
        });

    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onStart() {
        super.onStart();
        if(alunoEventListener != null)
            removerListeners();
        recuperarConfigs();
    }

    public void removerListeners(){
        Log.i("Teste"," removemndo listeners");
        firebaseRef.removeEventListener(alunoEventListener);
        firebaseRef.removeEventListener(tarefaChildEventListener);
    }

    @Override
    public void onPause() {
        super.onPause();
        HomeFragmentConfigs.salvarConfigs();
    }

    public void recuperarConfigs() {
        firebaseRef.child("Configurações HomeFragment").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() == null) {
                    Log.i("Teste", "null");
                    HomeFragment.setYearLastTarefaModified(" ");
                    HomeFragment.setMonthLastTarefaModified(" "); //mudar pra janeiro dps
                    HomeFragment.setWeekIntervalLastTarefaModified(" ");
                    HomeFragment.setLastTurmaModified(" ");
                    return;
                }
                HomeFragment.setYearLastTarefaModified(snapshot.child("geral").child("ano").getValue().toString());
                HomeFragment.setMonthLastTarefaModified(snapshot.child("geral").child("mes").getValue().toString());
                HomeFragment.setWeekIntervalLastTarefaModified(snapshot.child("geral").child("intervalo da semana").getValue().toString());
                HomeFragment.setLastTurmaModified(snapshot.child("geral").child("turma").getValue().toString());
                recuperarListaAlunos();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void addListeners() {

        Calendar calendar = Calendar.getInstance();
        //listener para quando um aluo for alterado
        alunoEventListener = firebaseRef.child("aluno").child(String.valueOf(calendar.get(Calendar.YEAR))).child(lastTurmaModified).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if (!tarefas.isEmpty()) {
                    Log.i("Teste", "onChildChanged");
                    Aluno aluno = snapshot.getValue(Aluno.class);
                    String diaSemana = (String) snapshot.child("checkBoxes").child("diaSemana").getValue();
                    aluno.setDiaSemana(diaSemana);

                    try {
                        String diaSemanaTarefa = "";
                        for (Tarefa tarefaTmp : getListTarefas()) {
                            java.util.Date date;
                            try {
                                date = new SimpleDateFormat("dd/MM/yyyy", new java.util.Locale("pt","BR")).parse(tarefaTmp.getDataEntrega());
                                diaSemanaTarefa = new SimpleDateFormat("EE", new java.util.Locale("pt","BR")).format(date).replaceAll("\\.", "");
                            } catch (ParseException parseException) {
                                parseException.printStackTrace();
                            }
                            com.cursoandroid.app_hwreminder.config.Date dateProject = new com.cursoandroid.app_hwreminder.config.Date();
                            if (diaSemanaTarefa.equals(aluno.getDiaSemana()) && tarefaTmp.getWeekIntervalAsChildString().equals(dateProject.getWeekIntervalAsChildString())) {
                                updateFrequenciaTarefa(tarefaTmp, aluno, false);
                                break;
                            }
                        }
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                recuperarListaAlunos();
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        //listener para quando uma tarefa for adicionada
        tarefaChildEventListener = firebaseRef.child("tarefa").child(yearLastTarefaModified).child(monthLastTarefaModified).child(weekIntervalLastTarefaModified).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if (!alunos.isEmpty()) {
                    Tarefa tarefa = snapshot.getValue(Tarefa.class);
                    tarefa.setKey(snapshot.getKey());
                    com.cursoandroid.app_hwreminder.config.Date date = new com.cursoandroid.app_hwreminder.config.Date();
                    if (weekIntervalLastTarefaModified.equals(date.getWeekIntervalAsChildString())) {
                        try {
                            Log.i("Teste", "tarefa in update added");
                            updateFrequenciaTarefa(tarefa, alunos.get(0), true);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    } else {
                        List<String> listTmpFizeram = new ArrayList();
                        for (Aluno aluno : getListAlunos()) {
                            listTmpFizeram.add(aluno.getNome());
                        }
                        tarefa.setListAlunosFizeram(listTmpFizeram);
                        try {
                            tarefa.salvarListas();
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
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
        Log.i("Teste", "called recuperar tarefas");
        firebaseRef.child("tarefa").addListenerForSingleValueEvent(new ValueEventListener() {
           @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
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
                            calendar.setTime(new SimpleDateFormat("dd/MM/yyyy", new java.util.Locale("pt","BR")).parse(tarefa.getDataEntrega()));
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        calendar.set(Calendar.DAY_OF_WEEK, calendar.getFirstDayOfWeek());
                        calendar.setTimeInMillis(calendar.getTimeInMillis() + Long.parseLong("86400000")); // configura pro primeiro dia da semana ser segunda
                        Log.i("Teste", "calendar week: " + calendar.get(Calendar.DAY_OF_WEEK_IN_MONTH) + " week: " + week + " tarefa: " + tarefa.getTitulo());
                        if (calendar.get(Calendar.DAY_OF_WEEK_IN_MONTH) == week) { // only shows currently week tarefas

                            //get weekDay string from tarefa
                            try {
                                date = new SimpleDateFormat("dd/MM/yyyy", new java.util.Locale("pt","BR")).parse(tarefa.getDataEntrega());
                                diaSemana = new SimpleDateFormat("EE", new java.util.Locale("pt","BR")).format(date).replaceAll("\\.", "");
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
                                                date = new SimpleDateFormat("dd/MM/yyyy", new java.util.Locale("pt","BR")).parse(tarefa.getDataEntrega());
                                                diaSemana = new SimpleDateFormat("EE", new java.util.Locale("pt","BR")).format(date).replaceAll("\\.", "");
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
                                                firebaseRef.child("tarefa")
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

                        ArrayList listTmpFizeram = (ArrayList) dados.child("Alunos que fizeram").getValue();

                        ArrayList listTmpNaoFizeram = (ArrayList) dados.child("Alunos que não fizeram").getValue();

                        if (listTmpFizeram == null) {
                            listTmpFizeram = new ArrayList();
                            for (Aluno aluno : getListAlunos()) {
                                listTmpFizeram.add(aluno.getNome());
                            }
                            tarefa.setListAlunosFizeram(listTmpFizeram);
                        } else
                            tarefa.setListAlunosFizeram(listTmpFizeram);

                        if (listTmpNaoFizeram != null)
                            tarefa.setListAlunosNaoFizeram(listTmpNaoFizeram);
                        tarefas.add(tarefa);
                    }
                }
                adapterTarefa.notifyDataSetChanged();
                addListeners();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void recuperarListaAlunos() {
        Calendar calendar = Calendar.getInstance();
        firebaseRef.child("aluno").child(String.valueOf(calendar.get(Calendar.YEAR))).child(lastTurmaModified).addListenerForSingleValueEvent(new ValueEventListener() {
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
        String today = new SimpleDateFormat("EE", new java.util.Locale("pt","BR")).format(c.getTime()).replaceAll("\\.", "");
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
    public void updateFrequenciaTarefa(Tarefa tarefa, Aluno aluno, boolean updateTodosAlunos) throws ParseException {
        String diaSemana = new String();
        Date date;
        //get weekDay string from tarefa
        try {
            date = new SimpleDateFormat("dd/MM/yyyy", new java.util.Locale("pt","BR")).parse(tarefa.getDataEntrega());
            diaSemana = new SimpleDateFormat("EE", new java.util.Locale("pt","BR")).format(date).replaceAll("\\.", "");
        } catch (ParseException e) {
            e.printStackTrace();
        }
        String finalDiaSemana = diaSemana;
        String yearString = tarefa.getYearString();
        String nomeAluno = aluno.getNome();
        firebaseRef.child("aluno")
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
                                    validarAddFrequencia(tarefa, nomeAluno, (boolean) snapshot.child("checkBoxes").child("checkedBoxSegunda").getValue());
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                                break;
                            case "ter":
                                try {
                                    validarAddFrequencia(tarefa, nomeAluno, (boolean) snapshot.child("checkBoxes").child("checkedBoxTerca").getValue());
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                                break;
                            case "qua":
                                try {
                                    validarAddFrequencia(tarefa, nomeAluno, (boolean) snapshot.child("checkBoxes").child("checkedBoxQuarta").getValue());
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                                break;
                            case "qui":
                                try {
                                    validarAddFrequencia(tarefa, nomeAluno, (boolean) snapshot.child("checkBoxes").child("checkedBoxQuinta").getValue());
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                                break;
                            case "sex":
                                try {
                                    validarAddFrequencia(tarefa, nomeAluno, (boolean) snapshot.child("checkBoxes").child("checkedBoxSexta").getValue());
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
        if (updateTodosAlunos) { //se essa opcao tiver marcada, chama a funcao de novo com o aluno do próximo index
            contadorRecursao++;
            if (alunos.size() > contadorRecursao) {
                updateFrequenciaTarefa(tarefa, alunos.get(contadorRecursao), true);
            } else
                contadorRecursao = 0;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void validarAddFrequencia(Tarefa tarefa, String nomeAluno, boolean fezTarefa) throws ParseException {
        List<String> listFizeram = tarefa.getListAlunosFizeram();
        List<String> listNaoFizeram = tarefa.getListAlunosNaoFizeram();
        boolean anyChange = false;

        if (listFizeram.contains(nomeAluno)) {
            if (!fezTarefa) {
                tarefa.removeFromListAlunosFizeram(nomeAluno);
                anyChange = true;
            }
        }
        if (!listFizeram.contains(nomeAluno)) {
            if (fezTarefa) {
                tarefa.addToListAlunosFizeram(nomeAluno);
                anyChange = true;
            }
        }

        if (listNaoFizeram.contains(nomeAluno)) {
            if (fezTarefa) {
                tarefa.removeFromListAlunosNaoFizeram(nomeAluno);
                anyChange = true;
            }
        }
        if (!listNaoFizeram.contains(nomeAluno)) {
            if (!fezTarefa) {
                tarefa.addToListAlunosNaoFizeram(nomeAluno);
                anyChange = true;
            }
        }
        Log.i("Teste", "Any change: " + anyChange);
        if (anyChange)
            tarefa.salvarListas();
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
        HomeFragment.lastTurmaModified = turma;
    }
}