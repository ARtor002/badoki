package ir.artor.badoki;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import ir.artor.badoki.ui.LoginFragment;
import ir.artor.badoki.ui.RegisterFragment;
import ir.artor.badoki.util.SessionManager;

public class AuthActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (SessionManager.isLoggedIn()) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }
        setContentView(R.layout.activity_auth);
        if (savedInstanceState == null) {
            showLogin();
        }
    }

    public void showLogin() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.auth_container, new LoginFragment())
                .commit();
    }

    public void showRegister() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.auth_container, new RegisterFragment())
                .commit();
    }

    public void openMain() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}
