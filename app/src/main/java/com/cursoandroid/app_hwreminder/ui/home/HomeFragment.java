package com.cursoandroid.app_hwreminder.ui.home;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.SearchView;
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
import com.cursoandroid.app_hwreminder.adapter.AdapterFiltrarAlunoFeedback;
import com.cursoandroid.app_hwreminder.adapter.AdapterTarefa;
import com.cursoandroid.app_hwreminder.config.ConfiguracaoFirebase;
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
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public final class HomeFragment extends Fragment {

    private RecyclerView recyclerViewAluno;
    private AdapterTarefa adapterTarefa;
    private AdapterAluno adapterAluno;
    private List<Tarefa> tarefas = new ArrayList<>();
    private List<Aluno> alunos = new ArrayList<>();
    private final DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDatabase();
    private ValueEventListener valueEventListenerTarefa, valueEventListenerAluno;
    private int week;

    private TextView textViewTituloLista, textDiaSemana,
            textViewSemanaHome, textViewDescricao, textViewAlunosFeedback;

    public static String diaSemanaAluno = "seg";

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

        textDiaSemana = view.findViewById(R.id.textViewDiaSemana);
        textViewSemanaHome = view.findViewById(R.id.textViewSemanaHome);
        textViewTituloLista = view.findViewById(R.id.textViewTituloLista);
        recyclerViewAluno = view.findViewById(R.id.recyclerViewAluno);
        textViewAlunosFeedback = view.findViewById(R.id.textViewListarAlunos);
        //FirebaseDatabase.getInstance().setLogLevel(Logger.Level.DEBUG); //usar para debug

        //Change title from month to month, day every day and set currently week interval from monday to friday

        com.cursoandroid.app_hwreminder.Date data = new com.cursoandroid.app_hwreminder.Date();

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

        //listener para quando um aluo for alterado
        firebaseRef.child("aluno").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if(!tarefas.isEmpty()) {
                    Aluno aluno = snapshot.getValue(Aluno.class);
                    updateFrequenciaTarefa(null, aluno, false, true);
                }
            }
            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if(!tarefas.isEmpty()) {
                    Aluno aluno = snapshot.getValue(Aluno.class);
                    String diaSemana = (String) snapshot.child("frequencia").child(data.getYearString()).child(data.getMonthString()).child(data.getWeekIntervalAsChildString()).child("diaSemana").getValue();
                    aluno.setDiaSemana(diaSemana);
                    updateFrequenciaTarefa(null, aluno, false, false);
                }
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

        //listener para quando uma tarefa for alterada
        firebaseRef.child("tarefa").child(data.getYearString()).child(data.getMonthString()).child(data.getWeekIntervalAsChildString()).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if(!alunos.isEmpty()) {
                    Tarefa tarefa = snapshot.getValue(Tarefa.class);
                    tarefa.setKey(snapshot.getKey());
                    Log.i("Teste", "tarefa: "+tarefa.getTitulo());
                    updateFrequenciaTarefa(tarefa, alunos.get(0), true, false);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName){
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

        textViewAlunosFeedback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                abrirFeedbackAlunos();
            }
        });

    }

    @Override
    public void onStart() {
        super.onStart();
        recuperarTarefas();
        recuperarListaAlunos();
    }


    @Override
    public void onStop() {
        super.onStop();
    }

    private void recuperarTarefas(){
        com.cursoandroid.app_hwreminder.Date dateProject = new com.cursoandroid.app_hwreminder.Date();
        firebaseRef.child("tarefa").child(dateProject.getYearString()).child(dateProject.getMonthString())
                .child(dateProject.getWeekIntervalAsChildString())
                .addListenerForSingleValueEvent(new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                tarefas.clear();
                Calendar calendar = Calendar.getInstance();
                Date date = null;
                String diaSemana = new String();
                for (DataSnapshot dados : snapshot.getChildren()) {
                    Tarefa tarefa = dados.getValue(Tarefa.class);
                    tarefa.setKey(dados.getKey());
                    Log.i("Teste","tarefa: "+tarefa.getKey());
                    try {
                        calendar.setTime(new SimpleDateFormat("dd/MM/yy").parse(tarefa.getDataEntrega()));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                    if (calendar.get(Calendar.WEEK_OF_MONTH) == week) { // only shows currently week tarefas

                        //get weekDay string from tarefa
                        try {
                            date = new SimpleDateFormat("dd/MM/yy").parse(tarefa.getDataEntrega());
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
                                        tarefa.deletarTarefa();
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

                        ArrayList listTmpFizeram = (ArrayList) dados.child("Alunos que fizeram").getValue();

                        ArrayList listTmpNaoFizeram = (ArrayList) dados.child("Alunos que não fizeram").getValue();

                        if(listTmpFizeram!=null)
                            tarefa.setListAlunosFizeram(listTmpFizeram);
                        if(listTmpNaoFizeram!=null)
                            tarefa.setListAlunosNaoFizeram(listTmpNaoFizeram);
                        tarefas.add(tarefa);
                    }
                }
                adapterTarefa.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void recuperarListaAlunos(){
        firebaseRef.child("aluno").addListenerForSingleValueEvent(new ValueEventListener() {
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

    private void formatDayColorDaily(View view){
        Calendar c = Calendar.getInstance();
        String today = new SimpleDateFormat("EE").format(c.getTime());
        TextView textView = null;
        switch (today){
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
        if(tarefa == null) { //se passar tarefa como null, procura pela tarefa da semana
            int count = 0;
            for (Tarefa tarefaIn : tarefas) {
                Date date;
                //get weekDay string from tarefa
                try {
                    date = new SimpleDateFormat("dd/MM/yy").parse(tarefaIn.getDataEntrega());
                    diaSemana = new SimpleDateFormat("EE").format(date);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                if(updateTodasTarefas){ //atualiza todas as tarefas
                    tarefaDoDia = tarefas.get(contadorRecursao);
                    break;
                }
                if (diaSemana.equals(aluno.getDiaSemana()) && calendar.get(Calendar.WEEK_OF_MONTH) == week && !tarefaIn.getDescricao().equals("Sem tarefa")) {
                    tarefaDoDia = tarefaIn;
                    break;
                }
                if (count == tarefas.size() - 1) //se chegar no final da lista e não achar nada, sai da função e não faz nada
                    return;
                count++;
            }
        }
        else{
            Date date;
            //get weekDay string from tarefa
            try {
                date = new SimpleDateFormat("dd/MM/yy").parse(tarefa.getDataEntrega());
                diaSemana = new SimpleDateFormat("EE").format(date);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            tarefaDoDia = tarefa;
        }
        String finalDiaSemana = diaSemana;
        Tarefa finalTarefaDoDia = tarefaDoDia;
        com.cursoandroid.app_hwreminder.Date date = new com.cursoandroid.app_hwreminder.Date();
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
                    Log.i("Teste", "Nome do aluno: "+nomeAluno);
                        switch (finalDiaSemana) {
                            case "seg":
                                validarAddFrequencia(finalTarefaDoDia, nomeAluno, (Boolean) snapshot.child("checkedBoxSegunda").getValue());
                                break;
                            case "ter":
                                validarAddFrequencia(finalTarefaDoDia, nomeAluno, (Boolean) snapshot.child("checkedBoxTerca").getValue());
                                break;
                            case "qua":
                                validarAddFrequencia(finalTarefaDoDia, nomeAluno, (Boolean) snapshot.child("checkedBoxQuarta").getValue());
                                break;
                            case "qui":
                                validarAddFrequencia(finalTarefaDoDia, nomeAluno, (Boolean) snapshot.child("checkedBoxQuinta").getValue());
                                break;
                            case "sex":
                                validarAddFrequencia(finalTarefaDoDia, nomeAluno, (Boolean) snapshot.child("checkedBoxSexta").getValue());
                                break;
                        }

                    }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        if(updateTodosAlunos) { //se essa opcao tiver marcada, chama a funcao de novo com o aluno do próximo index
            contadorRecursao++;
            if(alunos.size() > contadorRecursao) {
                updateFrequenciaTarefa(finalTarefaDoDia, alunos.get(contadorRecursao), true, false);
            }
            else
                contadorRecursao = 0;
        }
        else if(updateTodasTarefas){
            contadorRecursao++;
            if(tarefas.size() > contadorRecursao){
                updateFrequenciaTarefa(finalTarefaDoDia, aluno, false, true);
            }
            else
                contadorRecursao = 0;
        }
        else
            contadorRecursao = 0;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void validarAddFrequencia(Tarefa tarefa, String nomeAluno, boolean fezTarefa){
        List<String> listFizeram = tarefa.getListAlunosFizeram();
        List<String> listNaoFizeram = tarefa.getListAlunosNaoFizeram();

        if(listFizeram.contains(nomeAluno)) {
            if (!fezTarefa) {
                tarefa.removeFromListAlunosFizeram(nomeAluno);
            }
        }
        if(!listFizeram.contains(nomeAluno)){
            if(fezTarefa)
                tarefa.addToListAlunosFizeram(nomeAluno);
        }

        if(listNaoFizeram.contains(nomeAluno)){
            if(fezTarefa){
                tarefa.removeFromListAlunosNaoFizeram(nomeAluno);
            }
        }
        if(!listNaoFizeram.contains(nomeAluno)){
            if(!fezTarefa)
                tarefa.addToListAlunosNaoFizeram(nomeAluno);
        }
        Log.i("Teste:","listas: "+tarefa.getKey());
        tarefa.salvarListas();
    }

    private void abrirFeedbackAlunos(){ // abre uma view com a frequencia dos alunos
        Dialog dialog = new Dialog(getContext(), android.R.style.Theme_Material_Light_Dialog);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.activity_feedback_alunos);
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = 1000;
        lp.height = 2000;
        dialog.setCancelable(true);
        dialog.show();
        dialog.getWindow().setAttributes(lp);
        View view = dialog.getWindow().getDecorView();

        //Recycler alunos
        List<Aluno> alunosFiltro = new ArrayList<>();
        AdapterFiltrarAlunoFeedback adapterFiltrarAlunoFeedback = new AdapterFiltrarAlunoFeedback(alunosFiltro, getContext());
        RecyclerView recyclerView = view.findViewById(R.id.recyclerViewFeedbackAlunos);
        RecyclerView.LayoutManager layoutManagerFeedbackAluno = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManagerFeedbackAluno);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(adapterFiltrarAlunoFeedback);
        recyclerView.addItemDecoration(new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.VERTICAL));

        filtrarAlunos("", adapterFiltrarAlunoFeedback, alunosFiltro);

        //Search widget
        SearchView searchView = (SearchView) view.findViewById(R.id.searchViewFeedback);
        searchView.setBackgroundColor(getResources().getColor(R.color.teal_200));
        searchView.setIconifiedByDefault(false);
        searchView.setQueryHint("Digite o nome de um aluno");
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filtrarAlunos(newText, adapterFiltrarAlunoFeedback, alunosFiltro);
                return false;
            }
        });
    }

    void filtrarAlunos(String query, AdapterFiltrarAlunoFeedback adapterFiltrarAlunoFeedback, List<Aluno> alunosFiltro){
        alunosFiltro.clear();
        if(query.equals("")) {
            alunosFiltro.addAll(alunos); //mostra todos os alunos
        }
        else{
            for(Aluno aluno : alunos){
                if(aluno.getNome().toLowerCase().contains(query.toLowerCase()))
                    alunosFiltro.add(aluno); //mostra só os que contém os caracteres digitados
            }
        }
        adapterFiltrarAlunoFeedback.notifyDataSetChanged();
    }

}