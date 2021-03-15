package com.cursoandroid.app_hwreminder.adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.cursoandroid.app_hwreminder.R;
import com.cursoandroid.app_hwreminder.model.Aluno;
import com.cursoandroid.app_hwreminder.model.AlunoAddPendente;
import com.cursoandroid.app_hwreminder.model.Tarefa;

import java.util.List;

public class AdapterAddAlunoPendente extends RecyclerView.Adapter<AdapterAddAlunoPendente.MyViewHolder> {

    List<AlunoAddPendente> alunos;
    Context context;

    public AdapterAddAlunoPendente(List<AlunoAddPendente> alunos, Context context) {
        this.alunos = alunos;
        this.context = context;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemLista = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_aluno_pendente, parent, false);
        return new MyViewHolder(itemLista);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        AlunoAddPendente aluno = alunos.get(position);

        holder.nome.setOnTouchListener(new View.OnTouchListener() {
            final int DRAWABLE_LEFT = 0;
            final int DRAWABLE_TOP = 1;
            final int DRAWABLE_RIGHT = 2;
            final int DRAWABLE_BOTTOM = 3;
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_UP){
                    if(event.getRawX() >= (holder.nome.getRight() - holder.nome.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())){ // opens dialog when clicking the drawable on the right
                        abrirDialog(v, aluno);
                        return true;
                    }
                }
                return false;
            }
        });

        holder.nome.setText(aluno.getNome());

    }


    @Override
    public int getItemCount() {
        return alunos.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        TextView nome;

        public MyViewHolder(View itemView) {
            super(itemView);

            nome = itemView.findViewById(R.id.textAdapterAlunoPendenteNome);

        }

    }

    public void abrirDialog(View view, AlunoAddPendente aluno){

        //Instance alertDialog
        AlertDialog.Builder dialog = new AlertDialog.Builder(view.getContext());

        //Config title and message
        dialog.setTitle("Remover aluno(a)");
        dialog.setMessage("Deseja remover "+aluno.getNome()+"?");

        //Config cancel (can't be closed unless choosing an option)
        dialog.setCancelable(false);

        //Config icon
        dialog.setIcon(R.drawable.ic_baseline_person_add_24);

        //Config actions for yes or no
        dialog.setPositiveButton("Sim", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                alunos.remove(aluno); // remove from list
                Toast.makeText(view.getContext(), aluno.getNome()+" removido(a)", Toast.LENGTH_SHORT).show();
                notifyDataSetChanged();
            }
        });

        dialog.setNegativeButton("Não", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        //Criar e exibir AlertDialog
        dialog.create();
        dialog.show();

    }

}
