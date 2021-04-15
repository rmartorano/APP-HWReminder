package com.cursoandroid.app_hwreminder.ui.acompanhamento;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.SearchView;

import com.cursoandroid.app_hwreminder.R;
import com.cursoandroid.app_hwreminder.adapter.AdapterFiltrarAlunoFeedback;
import com.cursoandroid.app_hwreminder.model.Aluno;
import com.cursoandroid.app_hwreminder.model.Tarefa;
import com.cursoandroid.app_hwreminder.ui.home.HomeFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AcompanhamentoGeralFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AcompanhamentoGeralFragment extends Fragment {

    private final List<Tarefa> listTarefas = HomeFragment.getListTarefas();
    private final List<Aluno> listAlunos = HomeFragment.getListAlunos();

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public AcompanhamentoGeralFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment AcompanhamentoGeralFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static AcompanhamentoGeralFragment newInstance(String param1, String param2) {
        AcompanhamentoGeralFragment fragment = new AcompanhamentoGeralFragment();
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
        return inflater.inflate(R.layout.fragment_acompanhamento_geral, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ImageView imageViewPesquisarAluno = view.findViewById(R.id.imageViewPesquisarAluno);
        imageViewPesquisarAluno.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(View v) {
                abrirFeedbackAlunos();
            }
        });
        ImageView imageViewAlunosMaisFizeram = view.findViewById(R.id.imageViewAlunosMaisFizeram);
        imageViewAlunosMaisFizeram.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                abrirAlunosMaisFizeram();
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
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
        SearchView searchView = view.findViewById(R.id.searchViewFeedback);
        searchView.setBackgroundColor(Color.WHITE);
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

    private void abrirAlunosMaisFizeram(){
        getActivity().startActivity(new Intent(getContext(), MaisFizeramActivity.class));
    }

    void filtrarAlunos(String query, AdapterFiltrarAlunoFeedback adapterFiltrarAlunoFeedback, List<Aluno> alunosFiltro){
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
        adapterFiltrarAlunoFeedback.notifyDataSetChanged();
    }
}