package com.cursoandroid.app_hwreminder.ui.home;

import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import java.util.ArrayList;
import java.util.Calendar;
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
    private static String monthLastTarefaModified, yearLastTarefaModified, weekIntervalLastTarefaModified;

    private TextView textViewDescricao;

    public HomeFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView textDiaSemana = view.findViewById(R.id.textViewDiaSemana);
        TextView textViewSemanaHome = view.findViewById(R.id.textViewSemanaHome);
        TextView textViewTituloLista = view.findViewById(R.id.textViewTituloLista);
        RecyclerView recyclerViewAluno = view.findViewById(R.id.recyclerViewAluno);
        //FirebaseDatabase.getInstance().setLogLevel(Logger.Level.DEBUG); //usar para debug

        //Change title from month to month, day every day and set currently week interval from monday to friday

        com.cursoandroid.app_hwreminder.config.Date data = new com.cursoandroid.app_hwreminder.config.Date();

        Calendar calendar = data.getCalendar();

        textViewTituloLista.setText("Dever de casa - " + data.getMonthString());
        textDiaSemana.setText(StringUtils.capitalize(new SimpleDateFormat("EEEE").format(calendar.getTime())));
        formatDayColorDaily(view); // change color of today to focus on that
        week = calendar.get(Calendar.WEEK_OF_MONTH); // week of the month
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

    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onStart() {
        super.onStart();
        recuperarConfigs();
        recuperarListaAlunos();
        recuperarTarefas();
    }

    @Override
    public void onStop() {
        super.onStop();
        HomeFragmentConfigs.salvarConfigs();
    }

    public void recuperarConfigs() {
        ValueEventListener listener = firebaseRef.child("Configurações HomeFragment").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() == null) {
                    HomeFragment.setYearLastTarefaModified(" ");
                    HomeFragment.setMonthLastTarefaModified(" "); //mudar pra janeiro dps
                    HomeFragment.setWeekIntervalLastTarefaModified(" ");
                    return;
                }
                HomeFragment.setYearLastTarefaModified(snapshot.child("geral").child("ano").getValue().toString());
                HomeFragment.setMonthLastTarefaModified(snapshot.child("geral").child("mes").getValue().toString());
                HomeFragment.setWeekIntervalLastTarefaModified(snapshot.child("geral").child("intervalo da semana").getValue().toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        firebaseRef.removeEventListener(listener);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void addListeners() {

        //listener para quando um aluo for alterado
        com.cursoandroid.app_hwreminder.config.Date data = new com.cursoandroid.app_hwreminder.config.Date();
        firebaseRef.child("aluno").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if (!tarefas.isEmpty()) {
                    Log.i("Teste", "onChildChanged");
                    Aluno aluno = snapshot.getValue(Aluno.class);
                    String diaSemana = (String) snapshot.child("frequencia").child(data.getYearString()).child(data.getMonthString()).child(data.getWeekIntervalAsChildString()).child("diaSemana").getValue();
                    aluno.setDiaSemana(diaSemana);
                    updateFrequenciaTarefa(null, aluno, false, true);
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                Aluno aluno = snapshot.getValue(Aluno.class);
                for (Tarefa tarefa : tarefas) {
                    if (tarefa.getListAlunosFizeram().contains(aluno.getNome())) {
                        tarefa.removeFromListAlunosFizeram(aluno.getNome());
                        try {
                            tarefa.salvarListas();
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }
                    if (tarefa.getListAlunosNaoFizeram().contains(aluno.getNome())) {
                        tarefa.removeFromListAlunosNaoFizeram(aluno.getNome());
                        try {
                            tarefa.salvarListas();
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }
                }
                alunos.remove(aluno);
                recuperarListaAlunos();
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        //listener para quando uma tarefa for alterada
        firebaseRef.child("tarefa").child(yearLastTarefaModified).child(monthLastTarefaModified).child(weekIntervalLastTarefaModified).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if (!alunos.isEmpty()) {
                    Tarefa tarefa = snapshot.getValue(Tarefa.class);
                    tarefa.setKey(snapshot.getKey());
                    updateFrequenciaTarefa(tarefa, alunos.get(0), true, false);
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
         firebaseRef.child("tarefa").addListenerForSingleValueEvent(new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Calendar calendar = Calendar.getInstance();
                Date date = null;
                String diaSemana = new String();
                tarefas.clear();
                for (DataSnapshot tmpDados : snapshot.child(yearLastTarefaModified).child(monthLastTarefaModified).getChildren()) { // recupera apenas as tarefas dentro de 1 mês
                    for (DataSnapshot dados : tmpDados.getChildren()) {
                        Tarefa tarefa = dados.getValue(Tarefa.class);
                        tarefa.setKey(dados.getKey());
                        try {
                            calendar.setTime(new SimpleDateFormat("dd/MM/yyyy").parse(tarefa.getDataEntrega()));
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }

                        if (calendar.get(Calendar.WEEK_OF_MONTH) == week) { // only shows currently week tarefas

                            //get weekDay string from tarefa
                            try {
                                date = new SimpleDateFormat("dd/MM/yyyy").parse(tarefa.getDataEntrega());
                                diaSemana = new SimpleDateFormat("EE").format(date);
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
                            for(Aluno aluno : getListAlunos()){
                                listTmpFizeram.add(aluno.getNome());
                            }
                            tarefa.setListAlunosFizeram(listTmpFizeram);
                            try {
                                tarefa.salvarListas();
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                        }
                        else
                            tarefa.setListAlunosFizeram(listTmpFizeram);

                        if (listTmpNaoFizeram != null)
                            tarefa.setListAlunosNaoFizeram(listTmpNaoFizeram);

                        tarefas.add(tarefa);
                    }
                }
                addListeners();
                adapterTarefa.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void recuperarListaAlunos() {
        firebaseRef.child("aluno").addListenerForSingleValueEvent(new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                alunos.clear();
                for (DataSnapshot dados : snapshot.getChildren()) {
                    Aluno aluno = dados.getValue(Aluno.class);
                    alunos.add(aluno);
                }
                adapterAluno.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void formatDayColorDaily(View view) {
        Calendar c = Calendar.getInstance();
        String today = new SimpleDateFormat("EE").format(c.getTime());
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
    public void updateFrequenciaTarefa(Tarefa tarefa, Aluno aluno, boolean updateTodosAlunos, boolean updateTodasTarefas) {
        Tarefa tarefaDoDia = null;
        Calendar calendar = Calendar.getInstance();
        String diaSemana = new String();
        if (tarefa == null) { //se passar tarefa como null, procura pela tarefa da semana ou todas se updateTodasTarefas for true
            int count = 0;
            for (Tarefa tarefaIn : tarefas) {
                Date date;
                //get weekDay string from tarefa
                try {
                    date = new SimpleDateFormat("dd/MM/yyyy").parse(tarefaIn.getDataEntrega());
                    diaSemana = new SimpleDateFormat("EE").format(date);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                if (updateTodasTarefas) { //atualiza todas as tarefas
                    tarefaDoDia = tarefas.get(0);
                    break;
                } else if (diaSemana.equals(aluno.getDiaSemana()) && calendar.get(Calendar.WEEK_OF_MONTH) == week && !tarefaIn.getDescricao().equals("Sem tarefa")) {
                    tarefaDoDia = tarefaIn;
                    break;
                } else if (count == tarefas.size()) //se chegar no final da lista e não achar nada, sai da função e não faz nada
                    return;
                count++;
            }
        } else {
            Date date = null;
            //get weekDay string from tarefa
            try {
                date = new SimpleDateFormat("dd/MM/yyyy").parse(tarefa.getDataEntrega());
                diaSemana = new SimpleDateFormat("EE").format(date);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            int weekTmp = calendar.get(Calendar.WEEK_OF_MONTH);
            calendar.setTime(date);
            if(!(calendar.get(Calendar.WEEK_OF_MONTH) == weekTmp))
                return;
            tarefaDoDia = tarefa;
        }
        String finalDiaSemana = diaSemana;
        Tarefa finalTarefaDoDia = tarefaDoDia;
        com.cursoandroid.app_hwreminder.config.Date date = new com.cursoandroid.app_hwreminder.config.Date();
        String yearString = date.getYearString();
        String monthString = date.getMonthString();
        String weekIntervalAsChild = date.getWeekIntervalAsChildString();
        String nomeAluno = aluno.getNome();
        firebaseRef.child("aluno").child(nomeAluno).child("frequencia")
                .child(yearString).child(monthString).child(weekIntervalAsChild).
                addListenerForSingleValueEvent(new ValueEventListener() {
                    @RequiresApi(api = Build.VERSION_CODES.N)
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        switch (finalDiaSemana) {
                            case "seg":
                                try {
                                    validarAddFrequencia(finalTarefaDoDia, nomeAluno, (Boolean) snapshot.child("checkedBoxSegunda").getValue());
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                                break;
                            case "ter":
                                try {
                                    validarAddFrequencia(finalTarefaDoDia, nomeAluno, (Boolean) snapshot.child("checkedBoxTerca").getValue());
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                                break;
                            case "qua":
                                try {
                                    validarAddFrequencia(finalTarefaDoDia, nomeAluno, (Boolean) snapshot.child("checkedBoxQuarta").getValue());
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                                break;
                            case "qui":
                                try {
                                    validarAddFrequencia(finalTarefaDoDia, nomeAluno, (Boolean) snapshot.child("checkedBoxQuinta").getValue());
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                                break;
                            case "sex":
                                try {
                                    validarAddFrequencia(finalTarefaDoDia, nomeAluno, (Boolean) snapshot.child("checkedBoxSexta").getValue());
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
                updateFrequenciaTarefa(finalTarefaDoDia, alunos.get(contadorRecursao), true, false);
            } else
                contadorRecursao = 0;
        } else if (updateTodasTarefas) {
            contadorRecursao++;
            if (tarefas.size() > contadorRecursao) {
                updateFrequenciaTarefa(tarefas.get(contadorRecursao), aluno, false, true);
            } else
                contadorRecursao = 0;
        } else
            contadorRecursao = 0;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void validarAddFrequencia(Tarefa tarefa, String nomeAluno, boolean fezTarefa) throws ParseException {
        List<String> listFizeram = tarefa.getListAlunosFizeram();
        List<String> listNaoFizeram = tarefa.getListAlunosNaoFizeram();

        if (listFizeram.contains(nomeAluno)) {
            if (!fezTarefa) {
                tarefa.removeFromListAlunosFizeram(nomeAluno);
            }
        }
        if (!listFizeram.contains(nomeAluno)) {
            if (fezTarefa)
                tarefa.addToListAlunosFizeram(nomeAluno);
        }

        if (listNaoFizeram.contains(nomeAluno)) {
            if (fezTarefa) {
                tarefa.removeFromListAlunosNaoFizeram(nomeAluno);
            }
        }
        if (!listNaoFizeram.contains(nomeAluno)) {
            if (!fezTarefa)
                tarefa.addToListAlunosNaoFizeram(nomeAluno);
        }
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
}