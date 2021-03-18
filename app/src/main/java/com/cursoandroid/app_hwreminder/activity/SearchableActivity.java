package com.cursoandroid.app_hwreminder.activity;

import android.app.ListActivity;
import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListAdapter;
import android.widget.SimpleAdapter;

import androidx.annotation.Nullable;

import com.cursoandroid.app_hwreminder.R;
import com.cursoandroid.app_hwreminder.ui.home.HomeFragment;

import java.util.ArrayList;
import java.util.List;

public class SearchableActivity extends ListActivity {

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.searchable);
        setDefaultKeyMode(DEFAULT_KEYS_SEARCH_LOCAL);


        //// Get the intent, verify the action and get the query
        Intent intent = getIntent();
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            doMySearch(query);
        }
    }

    public void doMySearch(String query){
        Log.i("Teste","Aqui: "+query);
    }

}
