package com.cursoandroid.app_hwreminder.model;

import android.os.Build;

import androidx.annotation.RequiresApi;

import com.cursoandroid.app_hwreminder.config.Date;
import com.cursoandroid.app_hwreminder.config.ConfiguracaoFirebase;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class Tarefa {

    private String titulo, disciplina, dataEntrega, descricao, key;
    private List<String> listAlunosFizeram = new ArrayList<>();
    private List<String> listAlunosNaoFizeram = new ArrayList<>();

    public Tarefa() {
    }

    public Tarefa(String titulo, String disciplina, String dataEntrega, String descricao) {
        this.titulo = titulo;
        this.disciplina = disciplina;
        this.dataEntrega = dataEntrega;
        this.descricao = descricao;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDisciplina() {
        return disciplina;
    }

    public void setDisciplina(String disciplina) {
        this.disciplina = disciplina;
    }

    public String getDataEntrega() {
        return dataEntrega;
    }

    public void setDataEntrega(String dataEntrega) {
        this.dataEntrega = dataEntrega;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }


    @Exclude
    public String getKey() {
        return key;
    }


    public void setKey(String key) {
        this.key = key;
    }

    public void salvar() throws ParseException {

        DatabaseReference firebase = ConfiguracaoFirebase.getFirebaseDatabase();
        this.key = firebase.push().getKey();
        firebase
                .child(ConfiguracaoFirebase.getFirebaseAutenticacao().getCurrentUser().getEmail().replace(".", "-"))
                .child("tarefa")
                .child(this.getYearString())
                .child(this.getMonthString())
                .child(this.getWeekIntervalAsChildString())
                .child(this.key)
                .setValue(this);

    }

    public void deletarTarefa() throws ParseException {

        DatabaseReference firebase = ConfiguracaoFirebase.getFirebaseDatabase();
        firebase
                .child(ConfiguracaoFirebase.getFirebaseAutenticacao().getCurrentUser().getEmail().replace(".", "-"))
                .child("tarefa")
                .child(this.getYearString())
                .child(this.getMonthString())
                .child(this.getWeekIntervalAsChildString())
                .child(this.key)
                .removeValue();

    }

    @Exclude
    public void setListAlunosFizeram(List<String> listAlunosFizeram){
        this.listAlunosFizeram.clear();
        this.listAlunosFizeram.addAll(listAlunosFizeram);
    }

    @Exclude
    public List<String> getListAlunosFizeram() {
        return listAlunosFizeram;
    }

    @Exclude
    public void setListAlunosNaoFizeram(List<String> listAlunosNaoFizeram) {
        this.listAlunosNaoFizeram.clear();
        this.listAlunosNaoFizeram.addAll(listAlunosNaoFizeram);
    }

    @Exclude
    public List<String> getListAlunosNaoFizeram() {
        return listAlunosNaoFizeram;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Exclude
    public void addToListAlunosFizeram(String element) {
        this.listAlunosFizeram.add(element);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Exclude
    public void removeFromListAlunosFizeram(String element){
        this.listAlunosFizeram.remove(element);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Exclude
    public void addToListAlunosNaoFizeram(String element){
        this.listAlunosNaoFizeram.add(element);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Exclude
    public void removeFromListAlunosNaoFizeram(String element){
        this.listAlunosNaoFizeram.remove(element);
    }

    @Exclude
    public String getMonthString() throws ParseException {
        java.util.Date date = new SimpleDateFormat("dd/MM/yyyy", new java.util.Locale("pt","BR")).parse(this.getDataEntrega());
        Calendar calendar = Calendar.getInstance();
        Calendar sexta = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.DAY_OF_WEEK, calendar.getFirstDayOfWeek());
        calendar.setTimeInMillis(calendar.getTimeInMillis() + Long.parseLong("86400000")); // primeiro dia da semana como segunda feira
        long sextaMili = calendar.getTimeInMillis() + Long.parseLong("345600000"); // monday in millisecs + 4 days in millisecs
        sexta.setTimeInMillis(sextaMili);

        if(sexta.get(Calendar.MONTH) != calendar.get(Calendar.MONTH)){ //se o mês de segunda e sexta forem diferentes, retorna o mês de segunda
            return new SimpleDateFormat("MMMM", new java.util.Locale("pt","BR")).format(calendar.getTime());
        }
        else
            return new SimpleDateFormat("MMMM", new java.util.Locale("pt","BR")).format(sexta.getTime());
    }

    @Exclude
    public String getYearString() throws ParseException {
        java.util.Date date = new SimpleDateFormat("dd/MM/yyyy", new java.util.Locale("pt","BR")).parse(this.getDataEntrega());
        return new SimpleDateFormat("yyyy", new java.util.Locale("pt","BR")).format(date);
    }

    @Exclude
    public String getWeekIntervalAsChildString(){
        DecimalFormat mFormat = new DecimalFormat("00");
        Calendar calendar = Calendar.getInstance();
        Calendar sexta = Calendar.getInstance();
        try {
            calendar.setTime(new SimpleDateFormat("dd/MM/yyyy", new java.util.Locale("pt","BR")).parse(this.getDataEntrega()));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        calendar.set(Calendar.DAY_OF_WEEK, calendar.getFirstDayOfWeek());
        calendar.setTimeInMillis(calendar.getTimeInMillis() + Long.parseLong("86400000"));
        long sextaMili = calendar.getTimeInMillis() + Long.parseLong("345600000"); // monday in millisecs + 4 days in millisecs
        sexta.setTimeInMillis(sextaMili);
        return "Semana "+mFormat.format(Double.valueOf(calendar.get(Calendar.DAY_OF_MONTH)))+" | "+mFormat.format(Double.valueOf(calendar.get(Calendar.MONTH)+1))+" a "+mFormat.format(Double.valueOf(sexta.get(Calendar.DAY_OF_MONTH)))+" | "+mFormat.format(Double.valueOf(sexta.get(Calendar.MONTH)+1));

    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Exclude
    public void salvarListas() throws ParseException {
        DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDatabase();
        firebaseRef
                .child(ConfiguracaoFirebase.getFirebaseAutenticacao().getCurrentUser().getEmail().replace(".", "-"))
                .child("tarefa")
                .child(this.getYearString())
                .child(this.getMonthString())
                .child(this.getWeekIntervalAsChildString())
                .child(this.key)
                .child("Alunos que fizeram")
                .setValue(this.listAlunosFizeram);

        firebaseRef
                .child(ConfiguracaoFirebase.getFirebaseAutenticacao().getCurrentUser().getEmail().replace(".", "-"))
                .child("tarefa")
                .child(this.getYearString())
                .child(this.getMonthString())
                .child(this.getWeekIntervalAsChildString())
                .child(this.key)
                .child("Alunos que não fizeram")
                .setValue(this.listAlunosNaoFizeram);
    }
}
