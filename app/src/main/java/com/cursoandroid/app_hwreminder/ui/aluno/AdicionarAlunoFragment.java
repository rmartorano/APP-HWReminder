package com.cursoandroid.app_hwreminder.ui.aluno;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.cursoandroid.app_hwreminder.R;
import com.cursoandroid.app_hwreminder.activity.MainActivity;
import com.cursoandroid.app_hwreminder.adapter.AdapterAddAlunoPendente;
import com.cursoandroid.app_hwreminder.adapter.AdapterAluno;
import com.cursoandroid.app_hwreminder.config.ConfiguracaoFirebase;
import com.cursoandroid.app_hwreminder.config.Date;
import com.cursoandroid.app_hwreminder.config.HomeFragmentConfigs;
import com.cursoandroid.app_hwreminder.model.Aluno;
import com.cursoandroid.app_hwreminder.model.AlunoAddPendente;
import com.cursoandroid.app_hwreminder.ui.home.HomeFragment;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AdicionarAlunoFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AdicionarAlunoFragment extends Fragment {

    private List<AlunoAddPendente> listAlunos = new ArrayList<>();
    private AdapterAddAlunoPendente adapterAluno = null;
    private List<String> listTurmas = new LinkedList<>();
    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDatabase();
    private DatabaseReference turmaRef;
    private int lastTurma = 0;
    private Spinner spinnerTurma;


    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public AdicionarAlunoFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment AdicionarAlunoFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static AdicionarAlunoFragment newInstance(String param1, String param2) {
        AdicionarAlunoFragment fragment = new AdicionarAlunoFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_adicionar_aluno, container, false);

        Button salvarBtn = view.findViewById(R.id.buttonSalvarAddAlunos);

        salvarBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                salvarAlunos();
                getActivity().finish();
                startActivity(new Intent(getContext(), MainActivity.class));
            }
        });

        Date date = new Date();

        turmaRef = firebaseRef
                .child(ConfiguracaoFirebase.getFirebaseAutenticacao().getCurrentUser().getEmail().replace(".", "-"))
                .child("Configurações HomeFragment")
                .child("turmas")
                .child(date.getYearString());

        //Config recyclerView
        adapterAluno = new AdapterAddAlunoPendente(listAlunos, getContext());
        RecyclerView recyclerView = view.findViewById(R.id.recyclerViewAlunoPendente);
        RecyclerView.LayoutManager layoutManagerAluno = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManagerAluno);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(adapterAluno);
        recyclerView.addItemDecoration(new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.VERTICAL));

        TextView textViewAddAluno = view.findViewById(R.id.textViewAddAluno);
        textViewAddAluno.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(listTurmas.isEmpty()){
                    recuperarTurmas();
                }
                else
                    abrirDialog(v);
            }
        });

        return view;
    }

    public void abrirDialog(View v){

        //Instance alertDialog
        AlertDialog.Builder dialog = new AlertDialog.Builder(getContext());

        dialog.setTitle("Adicionar aluno");
        dialog.setMessage("Insira o nome e turma do aluno");

        //Config input view
        LinearLayout layout = new LinearLayout(getContext());
        layout.setOrientation(LinearLayout.HORIZONTAL);
        layout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        LinearLayout layoutTurma = new LinearLayout(getContext());
        layoutTurma.setLayoutParams(new LinearLayout.LayoutParams(2000, LinearLayout.LayoutParams.WRAP_CONTENT));
        layoutTurma.setOrientation(LinearLayout.VERTICAL);
        layout.addView(layoutTurma);

        EditText input = new EditText(getContext());
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setHint("Nome do aluno");
        layoutTurma.addView(input);

        LinearLayout layout2 = new LinearLayout(getContext());
        layout2.setOrientation(LinearLayout.HORIZONTAL);
        layout2.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        layout2.setGravity(Gravity.CENTER | Gravity.LEFT);
        layoutTurma.addView(layout2);

        spinnerTurma = new Spinner(getContext());
        if(listTurmas.isEmpty() || !listTurmas.get(0).equalsIgnoreCase("Selecionar turma"))
            listTurmas.add(0, "Selecionar turma");
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, listTurmas);
        spinnerTurma.setAdapter(spinnerArrayAdapter);
        spinnerTurma.setSelection(lastTurma);
        layout2.addView(spinnerTurma);

        TextView textViewAddTurma = new TextView(getContext());
        textViewAddTurma.setText("Adicionar turma");
        textViewAddTurma.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_add_green_24, 0, 0, 0);
        layout2.addView(textViewAddTurma);
        textViewAddTurma.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                criarTurma(getView());
            }
        });

        dialog.setView(layout);

        //Config cancel (can't be closed unless choosing an option)
        dialog.setCancelable(false);

        //Config icon
        dialog.setIcon(R.drawable.ic_baseline_person_add_24);


        dialog.setNegativeButton("Cancelar", null);
        dialog.setPositiveButton("Adicionar", null);

        AlertDialog alertDialog = dialog.create();

        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                Button positiveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                positiveButton.setOnClickListener(new View.OnClickListener() {
                    @RequiresApi(api = Build.VERSION_CODES.N)
                    @Override
                    public void onClick(View v) {
                        if(input.getText().toString().equals("")){
                            Toast.makeText(
                                    getContext().getApplicationContext(),
                                    "Cancelado",
                                    Toast.LENGTH_SHORT
                            ).show();
                            dialog.dismiss();
                        }
                        else {
                            if (validarAluno(input.getText().toString(), spinnerTurma.getSelectedItem().toString(), input)) {
                                Toast.makeText(
                                        getContext().getApplicationContext(),
                                        input.getText().toString() + " adicionad(o) com sucesso!",
                                        Toast.LENGTH_SHORT
                                ).show();
                                AlunoAddPendente aluno = new AlunoAddPendente();
                                aluno.setNome(input.getText().toString());
                                aluno.setTurma(spinnerTurma.getSelectedItem().toString());
                                listAlunos.add(aluno);
                                sortList(); // sort list alphabetically and notifify the adapter
                                adapterAluno.notifyDataSetChanged();
                                lastTurma = spinnerTurma.getSelectedItemPosition();
                                dialog.dismiss();
                            }
                        }
                    }
                });
            }
        });

        alertDialog.show();

    }

    public void criarTurma(View v){

        AlertDialog.Builder dialog = new AlertDialog.Builder(getContext());

        dialog.setTitle("Criar turma");
        dialog.setMessage("Insira o nome ou sigla da turma");

        EditText input = new EditText(getContext());
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setHint("Turma");
        dialog.setView(input);

        dialog.setPositiveButton("Criar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String inputText = input.getText().toString();
                listTurmas.add(inputText);
                turmaRef.setValue(listTurmas);
                Toast.makeText(getContext(), "Turma "+inputText+" adicionada com sucesso", Toast.LENGTH_SHORT).show();
                spinnerTurma.setSelection(listTurmas.indexOf(inputText));
            }
        });

        dialog.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        dialog.create();
        dialog.show();

    }

    public boolean validarAluno(String nome, String turma, EditText input){

        List<Aluno> alunos = HomeFragment.getListAlunos();
        List<String> alunosNome = new LinkedList<>();
        Map<String, String> mapTurmas = new HashMap<>();
        for(Aluno aluno : alunos){
            alunosNome.add(aluno.getNome());
            mapTurmas.put(aluno.getNome(), aluno.getTurma());
        }
        for(AlunoAddPendente aluno : listAlunos){
            alunosNome.add(aluno.getNome());
            mapTurmas.put(aluno.getNome(), aluno.getTurma());
        }

        if(alunosNome.contains(nome) && mapTurmas.get(nome).equalsIgnoreCase(turma)){
            Toast.makeText(
                    getContext().getApplicationContext(),
                    input.getText().toString() + " já existe na turma "+spinnerTurma.getSelectedItem().toString(),
                    Toast.LENGTH_SHORT
            ).show();
            return false;
        }

        return true;

    }

    public void salvarAlunos(){
        for(AlunoAddPendente alunoAdd : listAlunos) {
            Aluno aluno = new Aluno();
            aluno.setNome(alunoAdd.getNome());
            aluno.setTurma(alunoAdd.getTurma());
            aluno.salvar();
            HomeFragment.setLastTurmaModified(alunoAdd.getTurma());
        }
        Log.i("Teste", "last turma: "+HomeFragment.getLastTurmaModified());
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void sortList(){ //sort list alphabetically
        listAlunos.sort(Comparator.comparing(AlunoAddPendente::getNome));
    }

    public void recuperarTurmas(){

        turmaRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot turmaSnapshot : snapshot.getChildren()){
                    String turma = turmaSnapshot.getValue().toString();
                    listTurmas.add(turma);
                }
                abrirDialog(getView());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    @Override
    public void onPause() {
        super.onPause();
        HomeFragmentConfigs.salvarConfigs();
    }
}