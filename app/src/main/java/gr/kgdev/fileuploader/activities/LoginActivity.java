package gr.kgdev.fileuploader.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import org.json.JSONObject;

import gr.kgdev.fileuploader.AppCache;
import gr.kgdev.fileuploader.R;
import gr.kgdev.fileuploader.model.User;
import gr.kgdev.fileuploader.utils.HttpClient;
import gr.kgdev.fileuploader.utils.MyHttpClient;
import gr.kgdev.fileuploader.views.Dialogs;

public class LoginActivity extends AppCompatActivity {

    private boolean twice = false;
    private static final String TAG = LoginActivity.class.getName();
    private HttpClient httpClient = new MyHttpClient();
    private TextView usernameTextView;
    private TextView passwordTextView;
    private Button loginButton;

    public LoginActivity() {
        super();
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);
        setupUI(findViewById(R.id.parent));
        usernameTextView = (TextView) this.findViewById(R.id.username);
        passwordTextView = (TextView) this.findViewById(R.id.password);
        loginButton = (Button) this.findViewById(R.id.login_button);
        loginButton.setOnClickListener(v -> login(usernameTextView, passwordTextView));
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void login(TextView usernameTextView, TextView passwordTextView) {
        this.hideSoftKeyboard(this);
        this.loginButton.setActivated(false);
        httpClient.setBasicAuthCredentials(usernameTextView.getText().toString(), passwordTextView.getText().toString());
        httpClient.executeAsync(() -> {
            try {
                JSONObject json = (JSONObject) httpClient.post("/login", null);
                User appUser = new User(json);
                AppCache.setAppUser(appUser);

                Intent i = new Intent(this, MainActivity.class);
                startActivity(i);
            } catch (Throwable e) {
               Log.e(TAG, e.getMessage(), e);
               this.runOnUiThread(() -> Dialogs.showErrorDialog(LoginActivity.this, "Failed to login!"));
            } finally {
                this.runOnUiThread(() -> this.loginButton.setActivated(false));
            }
        });
    }
    @Override
    public void onBackPressed() {
        if (twice == true) {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
            System.exit(0);
        }
        twice = true;
        //if we push the back button twice in 3s we exit the app,otherwise app will continue to run
        Toast.makeText(this, "Please press BACK again to exit", Toast.LENGTH_SHORT).show();
        new Handler().postDelayed(() -> twice = false, 2000);
    }

    public void hideSoftKeyboard(Activity activity) {
        try {
            InputMethodManager inputMethodManager =
                    (InputMethodManager) activity.getSystemService(
                            Activity.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(
                    activity.getCurrentFocus().getWindowToken(), 0);
        } catch (Throwable t) {
            //ignore
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    public void setupUI(View view) {

        // Set up touch listener for non-text box views to hide keyboard.
        if (!(view instanceof TextView)) {
            view.setOnTouchListener((v, event) -> {
                hideSoftKeyboard(this);
                return false;
            });
        }

        //If a layout container, iterate over children and seed recursion.
        if (view instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                View innerView = ((ViewGroup) view).getChildAt(i);
                setupUI(innerView);
            }
        }
    }
}
