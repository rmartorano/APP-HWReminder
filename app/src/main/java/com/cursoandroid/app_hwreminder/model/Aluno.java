package com.cursoandroid.app_hwreminder.model;

import android.widget.CheckBox;

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
    }

    @Exclude
    public boolean isCheckedBoxTerca() {
        return checkBoxTerca;
    }

    public void setCheckBoxTerca(boolean checkBoxTerca) {
        this.checkBoxTerca = checkBoxTerca;
    }

    @Exclude
    public boolean isCheckedBoxQuarta() {
        return checkBoxQuarta;
    }

    public void setCheckBoxQuarta(boolean checkBoxQuarta) {
        this.checkBoxQuarta = checkBoxQuarta;
    }

    @Exclude
    public boolean isCheckedBoxQuinta() {
        return checkBoxQuinta;
    }

    public void setCheckBoxQuinta(boolean checkBoxQuinta) {
        this.checkBoxQuinta = checkBoxQuinta;
    }

    @Exclude
    public boolean isCheckedBoxSexta() {
        return checkBoxSexta;
    }

    public void setCheckBoxSexta(boolean checkBoxSexta) {
        this.checkBoxSexta = checkBoxSexta;
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

        DatabaseReference firebase = ConfiguracaoFirebase.getFirebaseDatabase();
        firebase.child("aluno")
                .child(this.nome)
                .child("frequencia")
                .child(new HomeFragment().getMonthString())
                .child(new HomeFragment().getWeekIntervalAsChildString())
                .setValue(checkBoxes);
    }

}
