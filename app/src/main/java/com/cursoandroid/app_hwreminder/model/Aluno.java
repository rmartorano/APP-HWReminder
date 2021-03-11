package com.cursoandroid.app_hwreminder.model;

import com.cursoandroid.app_hwreminder.config.ConfiguracaoFirebase;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;

public class Aluno {

    private String nome, key;

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
    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public void salvar(){

        DatabaseReference firebase = ConfiguracaoFirebase.getFirebaseDatabase();
        this.key = firebase.push().getKey();
        firebase.child("aluno")
                .child(key)
                .setValue(this);
    }

}
