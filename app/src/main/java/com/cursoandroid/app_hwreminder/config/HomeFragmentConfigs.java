package com.cursoandroid.app_hwreminder.config;

import com.cursoandroid.app_hwreminder.ui.home.HomeFragment;
import com.google.firebase.database.DatabaseReference;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HomeFragmentConfigs {

    private static DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDatabase();

    public HomeFragmentConfigs() {
    }

    public static void salvarConfigs(){

        Map<String, String> map = new HashMap<>();
        map.put("ano", HomeFragment.getYearLastTarefaModified());
        map.put("mes", HomeFragment.getMonthLastTarefaModified());
        map.put("intervalo da semana", HomeFragment.getWeekIntervalLastTarefaModified());
        map.put("turma", HomeFragment.getLastTurmaModified());

        firebaseRef
                .child(ConfiguracaoFirebase.getFirebaseAutenticacao().getCurrentUser().getEmail().replace(".", "-"))
                .child("Configurações HomeFragment")
                .child("geral")
                .setValue(map);
    }

}
