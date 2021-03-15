package com.cursoandroid.app_hwreminder.model;

import android.widget.CheckBox;

import com.cursoandroid.app_hwreminder.config.ConfiguracaoFirebase;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;

public class Aluno {

    private String nome, key;
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

    public boolean isCheckedBoxSegunda() {
        return checkBoxSegunda;
    }

    public void setCheckBoxSegunda(boolean checkBoxSegunda) {
        this.checkBoxSegunda = checkBoxSegunda;
    }

    public boolean isCheckedBoxTerca() {
        return checkBoxTerca;
    }

    public void setCheckBoxTerca(boolean checkBoxTerca) {
        this.checkBoxTerca = checkBoxTerca;
    }

    public boolean isCheckedBoxQuarta() {
        return checkBoxQuarta;
    }

    public void setCheckBoxQuarta(boolean checkBoxQuarta) {
        this.checkBoxQuarta = checkBoxQuarta;
    }

    public boolean isCheckedBoxQuinta() {
        return checkBoxQuinta;
    }

    public void setCheckBoxQuinta(boolean checkBoxQuinta) {
        this.checkBoxQuinta = checkBoxQuinta;
    }

    public boolean isCheckedBoxSexta() {
        return checkBoxSexta;
    }

    public void setCheckBoxSexta(boolean checkBoxSexta) {
        this.checkBoxSexta = checkBoxSexta;
    }

    public void salvar(){

        DatabaseReference firebase = ConfiguracaoFirebase.getFirebaseDatabase();
        this.key = firebase.push().getKey();
        firebase.child("aluno")
                .child(this.nome)
                .setValue(this);
    }

    public void update(){
        DatabaseReference firebase = ConfiguracaoFirebase.getFirebaseDatabase();
        firebase.child("aluno")
                .child(this.nome)
                .setValue(this);
    }

}
