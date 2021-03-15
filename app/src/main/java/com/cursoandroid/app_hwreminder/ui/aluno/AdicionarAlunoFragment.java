package com.cursoandroid.app_hwreminder.ui.aluno;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.cursoandroid.app_hwreminder.R;
import com.cursoandroid.app_hwreminder.activity.MainActivity;
import com.cursoandroid.app_hwreminder.adapter.AdapterAddAlunoPendente;
import com.cursoandroid.app_hwreminder.adapter.AdapterAluno;
import com.cursoandroid.app_hwreminder.model.Aluno;
import com.cursoandroid.app_hwreminder.model.AlunoAddPendente;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AdicionarAlunoFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AdicionarAlunoFragment extends Fragment {

    List<AlunoAddPendente> listAlunos = new ArrayList<>();
    private AdapterAddAlunoPendente adapterAluno = null;

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
                abrirDialog(v);
            }
        });

        return view;
    }

    public void abrirDialog(View view){

        //Instance alertDialog
        AlertDialog.Builder dialog = new AlertDialog.Builder(getContext());

        //Config title and message
        dialog.setTitle("Adicionar aluno");
        dialog.setMessage("Insira o nome do aluno");

        //Config input view
        final EditText input = new EditText(getContext());
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        dialog.setView(input);

        //Config cancel (can't be closed unless choosing an option)
        dialog.setCancelable(false);

        //Config icon
        dialog.setIcon(R.drawable.ic_baseline_person_add_24);

        //Config actions for yes or no
        dialog.setPositiveButton("Adicionar", new DialogInterface.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(input.getText().toString().equals("")){
                    Toast.makeText(
                            getContext().getApplicationContext(),
                            "Cancelado",
                            Toast.LENGTH_SHORT
                    ).show();
                }
                else {
                    Toast.makeText(
                            getContext().getApplicationContext(),
                            input.getText().toString()+" adicionado com sucesso!",
                            Toast.LENGTH_SHORT
                    ).show();
                    AlunoAddPendente aluno = new AlunoAddPendente();
                    aluno.setNome(input.getText().toString());
                    listAlunos.add(aluno);
                    sortList(); // sort list alphabetically and notifify the adapter
                    adapterAluno.notifyDataSetChanged();
                }
            }
        });

        dialog.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        //Criar e exibir AlertDialog
        dialog.create();
        dialog.show();

    }

    public void salvarAlunos(){
        for(AlunoAddPendente alunoAdd : listAlunos){
            Aluno aluno = new Aluno(alunoAdd.getNome());
            aluno.salvar();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void sortList(){ //sort list alphabetically
        listAlunos.sort(Comparator.comparing(AlunoAddPendente::getNome));
    }


}