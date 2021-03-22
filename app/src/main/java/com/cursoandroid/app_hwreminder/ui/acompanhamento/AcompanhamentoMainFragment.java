package com.cursoandroid.app_hwreminder.ui.acompanhamento;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cursoandroid.app_hwreminder.config.Date;
import com.cursoandroid.app_hwreminder.R;
import com.cursoandroid.app_hwreminder.config.ConfiguracaoFirebase;
import com.cursoandroid.app_hwreminder.model.Tarefa;
import com.cursoandroid.app_hwreminder.ui.home.HomeFragment;
import com.google.firebase.database.DatabaseReference;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AcompanhamentoMainFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AcompanhamentoMainFragment extends Fragment {

    private final DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDatabase();
    private final List<Tarefa> listTarefas = HomeFragment.getListTarefas();
    private Map<String, Integer> mapFizeram = new HashMap<>();
    private Map<String, Integer> mapNaoFizeram = new HashMap<>();

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public AcompanhamentoMainFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment AcompanhamentoMainFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static AcompanhamentoMainFragment newInstance(String param1, String param2) {
        AcompanhamentoMainFragment fragment = new AcompanhamentoMainFragment();
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
        return inflater.inflate(R.layout.fragment_acompanhamento_main, container, false);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //preencher mapas
        mapFizeram.put("seg", 0);
        mapFizeram.put("ter", 0);
        mapFizeram.put("qua", 0);
        mapFizeram.put("qui", 0);
        mapFizeram.put("sex", 0);

        mapNaoFizeram.put("seg", 0);
        mapNaoFizeram.put("ter", 0);
        mapNaoFizeram.put("qua", 0);
        mapNaoFizeram.put("qui", 0);
        mapNaoFizeram.put("sex", 0);

        //configura dias para mostrar no gráfico
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_WEEK, calendar.getFirstDayOfWeek());
        calendar.setTimeInMillis(calendar.getTimeInMillis() + Long.parseLong("86400000")); // segunda
        Calendar terca = Calendar.getInstance();
        terca.setTimeInMillis(calendar.getTimeInMillis() + Long.parseLong("86400000")); // terça
        Calendar quarta = Calendar.getInstance();
        quarta.setTimeInMillis(terca.getTimeInMillis() + Long.parseLong("86400000")); // quarta
        Calendar quinta = Calendar.getInstance();
        quinta.setTimeInMillis(quarta.getTimeInMillis() + Long.parseLong("86400000")); // quinta
        Calendar sexta = Calendar.getInstance();
        sexta.setTimeInMillis(quinta.getTimeInMillis() + Long.parseLong("86400000")); // sexta

        GraphView linegraph = view.findViewById(R.id.line_graph);
        linegraph.setTitle("Acompanhamento semanal");
        linegraph.setTitleColor(R.color.black);
        linegraph.setTitleTextSize(60);
        LineGraphSeries<DataPoint> lineSeriesFizeram = new LineGraphSeries<>(new DataPoint[] {
                new DataPoint(calendar.get(Calendar.DAY_OF_MONTH), recuperarQtdTarefasSemanal("seg", true)),
                new DataPoint(terca.get(Calendar.DAY_OF_MONTH), recuperarQtdTarefasSemanal("ter", true)),
                new DataPoint(quarta.get(Calendar.DAY_OF_MONTH), recuperarQtdTarefasSemanal("qua", true)),
                new DataPoint(quinta.get(Calendar.DAY_OF_MONTH), recuperarQtdTarefasSemanal("qui", true)),
                new DataPoint(sexta.get(Calendar.DAY_OF_MONTH), recuperarQtdTarefasSemanal("sex", true))
        });
        linegraph.addSeries(lineSeriesFizeram);
        lineSeriesFizeram.setColor(Color.GREEN);
        lineSeriesFizeram.setTitle("Fizeram");
        lineSeriesFizeram.setThickness(8);
        lineSeriesFizeram.setDataPointsRadius(10);

        LineGraphSeries<DataPoint> lineSeriesNaoFizeram = new LineGraphSeries<>(new DataPoint[] {
                new DataPoint(calendar.get(Calendar.DAY_OF_MONTH), recuperarQtdTarefasSemanal("seg", false)),
                new DataPoint(terca.get(Calendar.DAY_OF_MONTH), recuperarQtdTarefasSemanal("ter", false)),
                new DataPoint(quarta.get(Calendar.DAY_OF_MONTH), recuperarQtdTarefasSemanal("qua", false)),
                new DataPoint(quinta.get(Calendar.DAY_OF_MONTH), recuperarQtdTarefasSemanal("qui", false)),
                new DataPoint(sexta.get(Calendar.DAY_OF_MONTH), recuperarQtdTarefasSemanal("sex", false))
        });
        linegraph.addSeries(lineSeriesNaoFizeram);
        lineSeriesNaoFizeram.setColor(Color.RED);
        lineSeriesNaoFizeram.setTitle("Não fizeram");
        lineSeriesNaoFizeram.setThickness(8);
        lineSeriesNaoFizeram.setDataPointsRadius(10);
    }

    // "seg", "ter", "qua", "qui", "sex" / listaFizeram true para listFizeram e false para listNaoFizeram
    public int recuperarQtdTarefasSemanal(String diaSemana, boolean listaFizeram) {
        int qtd = 0;
        Date data = new Date();
        java.util.Date dateJava = new java.util.Date();
        String diaSemanaTarefa = "";
        for (Tarefa tarefa : listTarefas) {
            Log.i("Teste", "Data: " + tarefa.getDataEntregaAsChildString() + " outra data: " + data.getWeekIntervalAsChildString());
            if (tarefa.getDataEntregaAsChildString().equals(data.getWeekIntervalAsChildString())) {
                //get weekDay string from tarefa
                try {
                    dateJava = new SimpleDateFormat("dd/MM/yy").parse(tarefa.getDataEntrega());
                    diaSemanaTarefa = new SimpleDateFormat("EE").format(dateJava);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                if(diaSemanaTarefa.equals(diaSemana)) {
                    if(listaFizeram) {
                        mapFizeram.put(diaSemana, tarefa.getListAlunosFizeram().size());
                        return mapFizeram.get(diaSemana);
                    }
                    else{
                        mapNaoFizeram.put(diaSemana, tarefa.getListAlunosNaoFizeram().size());
                        return mapNaoFizeram.get(diaSemana);
                    }
                }
            }
        }
        return qtd;
    }

    @Override
    public void onStart() {
        super.onStart();
    }
}