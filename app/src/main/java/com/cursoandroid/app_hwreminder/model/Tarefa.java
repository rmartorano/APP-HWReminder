package com.cursoandroid.app_hwreminder.model;

import com.cursoandroid.app_hwreminder.config.ConfiguracaoFirebase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;

public class Tarefa {

    private String titulo, disciplina, dataEntrega, descricao, key;

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

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public void salvar(){

        DatabaseReference firebase = ConfiguracaoFirebase.getFirebaseDatabase();
        this.key = firebase.push().getKey();
        firebase.child("tarefa")
                .child(key)
                .setValue(this);

    }

}
