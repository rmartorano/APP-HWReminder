package com.cursoandroid.app_hwreminder.model;

import android.widget.CheckBox;

import com.cursoandroid.app_hwreminder.Date;
import com.cursoandroid.app_hwreminder.config.ConfiguracaoFirebase;
import com.cursoandroid.app_hwreminder.ui.home.HomeFragment;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;

public class Aluno {

    private String nome;
    private boolean checkBoxSegunda, checkBoxTerca, checkBoxQuarta, checkBoxQuinta, checkBoxSexta;

    public Aluno() {
        checkBoxSegunda = checkBoxTerca = checkBoxQuarta = checkBoxQuinta = checkBoxSexta = true;
    }

    public Aluno(String nome) {
        this.nome = nome;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    @Exclude
    public boolean isCheckedBoxSegunda() {
        return checkBoxSegunda;
    }

    public void setCheckBoxSegunda(boolean checkBoxSegunda) {
        this.checkBoxSegunda = checkBoxSegunda;
        HomeFragment.diaSemanaAluno = "seg";
    }

    @Exclude
    public boolean isCheckedBoxTerca() {
        return checkBoxTerca;
    }

    public void setCheckBoxTerca(boolean checkBoxTerca) {
        this.checkBoxTerca = checkBoxTerca;
        HomeFragment.diaSemanaAluno = "ter";
    }

    @Exclude
    public boolean isCheckedBoxQuarta() {
        return checkBoxQuarta;
    }

    public void setCheckBoxQuarta(boolean checkBoxQuarta) {
        this.checkBoxQuarta = checkBoxQuarta;
        HomeFragment.diaSemanaAluno = "qua";
    }

    @Exclude
    public boolean isCheckedBoxQuinta() {
        return checkBoxQuinta;
    }

    public void setCheckBoxQuinta(boolean checkBoxQuinta) {
        this.checkBoxQuinta = checkBoxQuinta;
        HomeFragment.diaSemanaAluno = "qui";
    }

    @Exclude
    public boolean isCheckedBoxSexta() {
        return checkBoxSexta;
    }

    public void setCheckBoxSexta(boolean checkBoxSexta) {
        this.checkBoxSexta = checkBoxSexta;
        HomeFragment.diaSemanaAluno = "sex";
    }

    @Exclude
    public String getDiaSemana() {
        return HomeFragment.diaSemanaAluno;
    }

    public class CheckBoxes{

    }

    public void salvar(){

        DatabaseReference firebase = ConfiguracaoFirebase.getFirebaseDatabase();
        firebase.child("aluno")
                .child(this.nome)
                .setValue(this);
        this.salvarCheckBox();
    }

    public void salvarCheckBox(){
        Map<String, Object> checkBoxes = new HashMap<>();
        checkBoxes.put("checkedBoxSegunda", this.checkBoxSegunda);
        checkBoxes.put("checkedBoxTerca", this.checkBoxTerca);
        checkBoxes.put("checkedBoxQuarta", this.checkBoxQuarta);
        checkBoxes.put("checkedBoxQuinta", this.checkBoxQuinta);
        checkBoxes.put("checkedBoxSexta", this.checkBoxSexta);
        checkBoxes.put("diaSemana", this.getDiaSemana());

        Date date = new Date();
        DatabaseReference firebase = ConfiguracaoFirebase.getFirebaseDatabase();
        firebase.child("aluno")
                .child(this.nome)
                .child("frequencia")
                .child(date.getYearString())
                .child(date.getMonthString())
                .child(date.getWeekIntervalAsChildString())
                .setValue(checkBoxes);
    }

}
