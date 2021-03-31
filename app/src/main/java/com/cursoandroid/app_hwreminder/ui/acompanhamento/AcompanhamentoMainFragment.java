package com.cursoandroid.app_hwreminder.ui.acompanhamento;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager2.widget.ViewPager2;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;

import com.cursoandroid.app_hwreminder.activity.MainActivity;
import com.cursoandroid.app_hwreminder.adapter.AdapterFiltrarAlunoFeedback;
import com.cursoandroid.app_hwreminder.config.Date;
import com.cursoandroid.app_hwreminder.R;
import com.cursoandroid.app_hwreminder.model.Aluno;
import com.cursoandroid.app_hwreminder.model.Tarefa;
import com.cursoandroid.app_hwreminder.ui.aluno.AdicionarAlunoFragment;
import com.cursoandroid.app_hwreminder.ui.home.HomeFragment;
import com.cursoandroid.app_hwreminder.ui.home.HomeFragmentMonthly;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.ogaclejapan.smarttablayout.SmartTabLayout;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItemAdapter;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItems;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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

    private final List<Tarefa> listTarefas = HomeFragment.getListTarefas();
    private final List<Aluno> listAlunos = HomeFragment.getListAlunos();
    private final Map<String, Integer> mapFizeram = new HashMap<>();
    private final Map<String, Integer> mapNaoFizeram = new HashMap<>();

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

    @SuppressLint("SetTextI18n")
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        TextView tituloAcompanhamento = view.findViewById(R.id.textViewTituloAcompanhamento);

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
        calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH)+1);
        calendar.setTimeInMillis(calendar.getTimeInMillis() + Long.parseLong("86400000")); // segunda
        Calendar terca = Calendar.getInstance();
        terca.setTimeInMillis(calendar.getTimeInMillis() + Long.parseLong("86400000")); // terça
        Calendar quarta = Calendar.getInstance();
        quarta.setTimeInMillis(terca.getTimeInMillis() + Long.parseLong("86400000")); // quarta
        Calendar quinta = Calendar.getInstance();
        quinta.setTimeInMillis(quarta.getTimeInMillis() + Long.parseLong("86400000")); // quinta
        Calendar sexta = Calendar.getInstance();
        sexta.setTimeInMillis(quinta.getTimeInMillis() + Long.parseLong("86400000")); // sexta

        //Configuração do gráfico
        DecimalFormat mFormat = new DecimalFormat("00");
        final String[] diasSemana = new String[] {
                mFormat.format((double) calendar.get(Calendar.DAY_OF_MONTH)) +"/"+mFormat.format((double)calendar.get(Calendar.MONTH)),
                mFormat.format((double) terca.get(Calendar.DAY_OF_MONTH))+"/"+mFormat.format((double)terca.get(Calendar.MONTH)),
                mFormat.format((double)quarta.get(Calendar.DAY_OF_MONTH))+"/"+mFormat.format((double)quarta.get(Calendar.MONTH)),
                mFormat.format((double)quinta.get(Calendar.DAY_OF_MONTH))+"/"+mFormat.format((double)quinta.get(Calendar.MONTH)),
                mFormat.format((double)sexta.get(Calendar.DAY_OF_MONTH))+"/"+mFormat.format((double)sexta.get(Calendar.MONTH))
            };

        LineChart chart = view.findViewById(R.id.line_chart);

        ValueFormatter formatter = new ValueFormatter() {
            @Override
            public String getAxisLabel(float value, AxisBase axis) {
                return diasSemana[(int) value];
            }
        };
        XAxis xAxis = chart.getXAxis();
        xAxis.setGranularity(1f); // minimum axis-step (interval) is 1
        xAxis.setValueFormatter(formatter);
        xAxis.setTextSize(11);
        xAxis.setTextColor(R.color.teal_200);
        xAxis.setAvoidFirstLastClipping(true);

        ValueFormatter pointFormatter = new ValueFormatter() {
            private DecimalFormat format = new DecimalFormat("0");
            @Override
            public String getPointLabel(Entry entry) {
                return format.format(entry.getY());
            }
        };

        List<Entry> entriesFizeram = new ArrayList<>();
        entriesFizeram.add(new Entry(0, recuperarQtdTarefasSemanal("seg", true)));
        entriesFizeram.add(new Entry(1, recuperarQtdTarefasSemanal("ter", true)));
        entriesFizeram.add(new Entry(2, recuperarQtdTarefasSemanal("qua", true)));
        entriesFizeram.add(new Entry(3, recuperarQtdTarefasSemanal("qui", true)));
        entriesFizeram.add(new Entry(4, recuperarQtdTarefasSemanal("sex", true)));

        List<Entry> entriesNaoFizeram = new ArrayList<>();
        entriesNaoFizeram.add(new Entry(0, recuperarQtdTarefasSemanal("seg", false)));
        entriesNaoFizeram.add(new Entry(1, recuperarQtdTarefasSemanal("ter", false)));
        entriesNaoFizeram.add(new Entry(2, recuperarQtdTarefasSemanal("qua", false)));
        entriesNaoFizeram.add(new Entry(3, recuperarQtdTarefasSemanal("qui", false)));
        entriesNaoFizeram.add(new Entry(4, recuperarQtdTarefasSemanal("sex", false)));

        LineDataSet dataSetFizeram = new LineDataSet(entriesFizeram, "Quantidade fizeram");
        dataSetFizeram.setAxisDependency(YAxis.AxisDependency.LEFT);
        dataSetFizeram.setColor(Color.GREEN);
        dataSetFizeram.setValueTextColor(Color.BLACK);
        dataSetFizeram.setValueTextSize(10);
        dataSetFizeram.setValueFormatter(pointFormatter);

        LineDataSet dataSetNaoFizeram = new LineDataSet(entriesNaoFizeram, "Quantidade não fizeram");
        dataSetNaoFizeram.setAxisDependency(YAxis.AxisDependency.LEFT);
        dataSetNaoFizeram.setColor(Color.RED);
        dataSetNaoFizeram.setValueTextColor(Color.BLACK);
        dataSetNaoFizeram.setValueTextSize(10);
        dataSetNaoFizeram.setValueFormatter(pointFormatter);

        List<ILineDataSet> dataSets = new ArrayList<>();
        dataSets.add(dataSetFizeram);
        dataSets.add(dataSetNaoFizeram);

        Description description = new Description();
        description.setText("Quantidade total de alunos: "+listAlunos.size());
        description.setTextSize(10);

        LineData lineData = new LineData(dataSets);
        chart.setDescription(description);
        chart.setData(lineData);
        chart.setAutoScaleMinMaxEnabled(true);
        chart.invalidate();

        Date date = new Date();
        tituloAcompanhamento.setText("Acompanhamento díario\n"+date.getWeekInterval());

        FragmentPagerItemAdapter adapter = new FragmentPagerItemAdapter(
                getActivity().getSupportFragmentManager(), FragmentPagerItems.with(getContext())
                .add("Geral", AcompanhamentoGeralFragment.class)
                .add("Filtrar", HomeFragmentMonthly.class)
                .create());

        ViewPager viewPager = view.findViewById(R.id.viewpager);
        viewPager.setAdapter(adapter);

        SmartTabLayout viewPagerTab = view.findViewById(R.id.viewpagertab);
        viewPagerTab.setViewPager(viewPager);

    }

    // "seg", "ter", "qua", "qui", "sex" / listaFizeram true para listFizeram e false para listNaoFizeram
    public int recuperarQtdTarefasSemanal(String diaSemana, boolean listaFizeram) {
        int qtd = 0;
        Date data = new Date();
        java.util.Date dateJava = new java.util.Date();
        String diaSemanaTarefa = "";
        for (Tarefa tarefa : listTarefas) {
            if (tarefa.getWeekIntervalAsChildString().equals(data.getWeekIntervalAsChildString())) {
                //get weekDay string from tarefa
                try {
                    dateJava = new SimpleDateFormat("dd/MM/yyyy").parse(tarefa.getDataEntrega());
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