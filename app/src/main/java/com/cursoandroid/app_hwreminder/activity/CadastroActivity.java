package com.cursoandroid.app_hwreminder.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.cursoandroid.app_hwreminder.R;
import com.cursoandroid.app_hwreminder.config.ConfiguracaoFirebase;
import com.cursoandroid.app_hwreminder.model.Usuario;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;

public class CadastroActivity extends AppCompatActivity {

    private EditText campoNome, campoEmail, campoSenha;
    private Button botaoCadastrar;
    private FirebaseAuth autenticacao;
    private Usuario usuario;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro);

        getSupportActionBar().setTitle("Cadastro");

        campoNome = findViewById(R.id.editNomeCadastro);
        campoEmail = findViewById(R.id.editEmailCadastro);
        campoSenha = findViewById(R.id.editSenhaCadastro);
        botaoCadastrar = findViewById(R.id.buttonCadastrar);

        botaoCadastrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String textoNome = campoNome.getText().toString();
                String textoEmail = campoEmail.getText().toString();
                String textoSenha = campoSenha.getText().toString();

                //Validar se os campos foram preenchidos
                if(!textoNome.isEmpty()){
                    if(!textoEmail.isEmpty()){
                        if(!textoSenha.isEmpty()){
                            usuario = new Usuario();
                            usuario.setNome(textoNome);
                            usuario.setEmail(textoEmail);
                            usuario.setSenha(textoSenha);
                            cadastrarUsuario();
                        }
                        else{
                            Toast.makeText(CadastroActivity.this,
                                    "Preencha a senha!",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                    else{
                        Toast.makeText(CadastroActivity.this,
                                "Preencha o email!",
                                Toast.LENGTH_SHORT).show();
                    }
                }
                else{
                    Toast.makeText(CadastroActivity.this,
                            "Preencha o nome!",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    public void cadastrarUsuario(){

        autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
        autenticacao.createUserWithEmailAndPassword(
                usuario.getEmail(), usuario.getSenha()
        ).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    autenticacao.signInWithEmailAndPassword(usuario.getEmail(), usuario.getSenha());
                    Log.i("Teste", "is auth: "+autenticacao.getCurrentUser().getUid());
                    String idUsuario = usuario.getEmail();
                    usuario.setIdUsuario(idUsuario);
                    usuario.salvar();
                    startActivity(new Intent(getApplicationContext(), PrincipalActivity.class));
                }
                else{
                    String execao = "";
                    try{
                        throw task.getException();
                    }catch (FirebaseAuthWeakPasswordException e){
                        execao = "Digite uma senha mais forte";
                    }catch (FirebaseAuthInvalidCredentialsException e){
                        execao = "Digite um email válido";
                    }catch (FirebaseAuthUserCollisionException e){
                        execao = "Esse email já existe";
                    }catch (Exception e){
                        execao = "Erro ao cadastrar usuário: "+e.getMessage();
                        e.printStackTrace();
                    }

                    Toast.makeText(CadastroActivity.this,
                            execao,
                            Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

}
