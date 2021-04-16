package com.cursoandroid.app_hwreminder.ui.tarefa;

import android.app.DatePickerDialog;
import android.content.Intent;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.cursoandroid.app_hwreminder.activity.MainActivity;
import com.cursoandroid.app_hwreminder.R;
import com.cursoandroid.app_hwreminder.config.ConfiguracaoFirebase;
import com.cursoandroid.app_hwreminder.config.Date;
import com.cursoandroid.app_hwreminder.config.HomeFragmentConfigs;
import com.cursoandroid.app_hwreminder.model.Tarefa;
import com.cursoandroid.app_hwreminder.ui.home.HomeFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ReminderFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ReminderFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER

    private EditText editDate, editTitulo, editDescricao;
    private Spinner spinner, spinnerTurma;
    private Button buttonSalvar;
    private final Calendar myCalendar = Calendar.getInstance();
    private Tarefa tarefa;

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public ReminderFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ReminderFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ReminderFragment newInstance(String param1, String param2) {
        ReminderFragment fragment = new ReminderFragment();
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
        return inflater.inflate(R.layout.fragment_reminder, container, false);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        editTitulo = view.findViewById(R.id.editTitulo);
        editDescricao = view.findViewById(R.id.editDescricao);
        buttonSalvar = view.findViewById(R.id.buttonSalvar);
        spinner = view.findViewById(R.id.disciplines_spinner);
        editDate = view.findViewById(R.id.deadlineDate);
        recuperarTurmas();

        //onClick para salvar tarefa
        buttonSalvar.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View v) {
                try {
                    salvarTarefa(v);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        });

        //date picker

        editDate = view.findViewById(R.id.deadlineDate);
        DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                // TODO Auto-generated method stub
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, month);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateLabel();
            }
        };

        editDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(getContext(), date, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        //Spinner
        Spinner spinner = view.findViewById(R.id.disciplines_spinner);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
                R.array.array_disciplines, R.layout.spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);
    }

    private void updateLabel() {
        String myFormat = "dd/MM/yyyy"; //Date format
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, new java.util.Locale("pt","BR"));

        editDate.setText(sdf.format(myCalendar.getTime()));
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void salvarTarefa(View view) throws ParseException {
        if(validarCampos()){
            tarefa = new Tarefa(editTitulo.getText().toString(), spinner.getSelectedItem().toString(), editDate.getText().toString(), editDescricao.getText().toString(), spinnerTurma.getSelectedItem().toString());
            HomeFragment.setMonthLastTarefaModified(tarefa.getMonthString());
            HomeFragment.setYearLastTarefaModified(tarefa.getYearString());
            HomeFragment.setWeekIntervalLastTarefaModified(tarefa.getWeekIntervalAsChildString());
            HomeFragmentConfigs.salvarConfigs();
            tarefa.salvar();
            Toast.makeText(getContext(), "Salvo com sucesso!", Toast.LENGTH_SHORT).show();
            getActivity().finish();
            startActivity(new Intent(getContext(), MainActivity.class));
        }
    }

    public boolean validarCampos(){
        if(!editTitulo.getText().toString().equals("") && editTitulo.getText() != null){
            if(!editDescricao.getText().toString().equals("") && editDescricao.getText() != null){
                if(!spinner.getSelectedItem().toString().equals("Selecione uma disciplina") && spinner.getSelectedItem() != null) {
                    if(!editDate.getText().toString().equals("") && editDate.getText() != null){
                        if(!(spinnerTurma.getSelectedItem().toString() == null) && !spinnerTurma.getSelectedItem().toString().equalsIgnoreCase("Selecionar turma")){
                            return true;
                        }
                        else{
                            Toast.makeText(getContext(), "Selecione ou crie uma turma primeiro", Toast.LENGTH_SHORT).show();
                            return false;
                        }
                    }
                    else{
                        Toast.makeText(getContext(), "Escolha uma data", Toast.LENGTH_SHORT).show();
                        return false;
                    }
                }
                else{
                    Toast.makeText(getContext(), "Escolha uma disciplina", Toast.LENGTH_SHORT).show();
                    return false;
                }
            }
            else{
                Toast.makeText(getContext(), "Preencha o campo Descrição", Toast.LENGTH_SHORT).show();
                return false;
            }
        }
        else{
            Toast.makeText(getContext(), "Preencha o campo Título", Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    public void recuperarTurmas(){
        DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDatabase();
        String user = ConfiguracaoFirebase.getFirebaseAutenticacao().getCurrentUser().getUid();
        spinnerTurma = getView().findViewById(R.id.spinnerTurmaCriarTarefa);
        Date dateFromProject = new Date();
        final String[] lastTurmaModified = {HomeFragment.getLastTurmaModified()};
        List<String> spinnerArray =  new ArrayList<String>();
        firebaseRef.child(user).child("Configurações HomeFragment").child("turmas").child(dateFromProject.getYearString()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot turma : snapshot.getChildren()){
                    Log.i("Teste", "turma: "+turma.getValue());
                    if(!turma.getValue().equals("Selecionar turma")){
                        spinnerArray.add(turma.getValue().toString());
                    }
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                        getContext(), R.layout.spinner_item, spinnerArray);
                spinnerTurma.setAdapter(adapter);
                if(!lastTurmaModified[0].equals("")){
                    for(int i=0; i<spinnerTurma.getCount(); i++){
                        if(spinnerTurma.getItemAtPosition(i).toString().equals(lastTurmaModified[0])){
                            spinnerTurma.setSelection(i);
                            break;
                        }
                    }
                }
                spinnerTurma.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @RequiresApi(api = Build.VERSION_CODES.N)
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        if(spinnerTurma.getSelectedItem().toString().equalsIgnoreCase(lastTurmaModified[0]))
                            return;
                        lastTurmaModified[0] = spinnerTurma.getSelectedItem().toString();
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

}