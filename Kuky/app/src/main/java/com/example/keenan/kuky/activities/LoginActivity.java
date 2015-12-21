package com.example.keenan.kuky.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.keenan.kuky.R;
import com.example.keenan.kuky.api.ApiClient;
import com.example.keenan.kuky.models.UserApiKeyResponse;
import com.example.keenan.kuky.models.UserRequest;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Login Activity is the first view when the app is opened. It has two text fields, a username and
 * a password. It handles the authentication of the existing user, or the creation of a new user. Once
 * a valid user exits, then it goes to the Ku View activity to enter the app
 */
public class LoginActivity extends AppCompatActivity {

    private static final String TAG = LoginActivity.class.getSimpleName();
    public static final String PREFS_NAME = "UserData";
    private String uname;
    private String pw;
    private String apiKey;
    private int userId;
    private String errorMessage;

    @Bind(R.id.login_username) EditText username;
    @Bind(R.id.login_password) EditText password;
    @Bind(R.id.login_action) Button loginAction;
    @Bind(R.id.login_error) TextView loginError;

    /**
     * Button handle when the Login button is clicked, makes and API call to check authentication
     * @param view the current screen view
     */
    @OnClick(R.id.login_action)
    public void onLoginClicked(View view) {
        uname = username.getText().toString();
        pw = password.getText().toString();
        // Makes call to attempt to log in with info provided
        ApiClient.getKukyApiClient().login(new UserRequest(uname, pw))
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<UserApiKeyResponse>() {
                    @Override
                    public void onCompleted() {}

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG + ": Retrofit Error - ", e.toString());
                    }

                    @Override
                    public void onNext(UserApiKeyResponse apiKeyResponse) {
                        Log.d(TAG, apiKeyResponse.toString());
                        apiKey = apiKeyResponse.getNewKey();
                        userId = apiKeyResponse.getUserId();
                        errorMessage = apiKeyResponse.getErrorMessage();
                        if (errorMessage != null) {
                            // If error exists, displays it
                            loginError.setText(errorMessage);
                        } else {
                            SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
                            SharedPreferences.Editor editor = settings.edit();
                            editor.putString("username", username.getText().toString());
                            editor.putInt("userId", userId);
                            editor.putString("apiKey", apiKey);
                            editor.apply();
                            changeActivity();
                        }
                    }
                });
    }

    /**
     * Button handle when the Sign Up button is clicked, makes and API call to check authentication
     * and create a new user
     * @param view the current screen view
     */
    @OnClick(R.id.register_action)
    public void onRegisterClicked(View view) {
        uname = username.getText().toString();
        pw = password.getText().toString();
        // Do you want to show this even registration isn't successful? If not, move this
        Snackbar.make(view, "uname: " + uname + " | pw: " + pw, Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();
        ApiClient.getKukyApiClient().register(new UserRequest(uname, pw))
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<UserApiKeyResponse>() {
                    @Override
                    public void onCompleted() {}

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG + ": Retrofit Error - ", e.toString());
                    }

                    @Override
                    public void onNext(UserApiKeyResponse apiKeyResponse) {
                        Log.d(TAG, apiKeyResponse.toString());
                        apiKey = apiKeyResponse.getNewKey();
                        userId = apiKeyResponse.getUserId();
                        errorMessage = apiKeyResponse.getErrorMessage();
                        if (errorMessage != null) {
                            if (errorMessage.equals("SequelizeUniqueConstraintError")) {
                                loginError.setText("This username is already taken.");
                            } else {
                                loginError.setText(errorMessage);
                            }
                        } else {
                            SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
                            SharedPreferences.Editor editor = settings.edit();
                            editor.putString("username", username.getText().toString());
                            editor.putInt("userId", userId);
                            editor.putString("apiKey", apiKey);
                            editor.apply();
                            changeActivity();
                        }
                    }
                });
    }

    /**
     * Starts the Ku View activity when the user is verified into the app
     */
    public void changeActivity() {
        Log.d(TAG, "CHANGING ACTIVITIES");
        Intent intent = new Intent(LoginActivity.this, KuViewActivity.class);
        startActivity(intent);
    }

    /**
     * Sets up the view of the activity
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state as given here.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
    }

    /**
     * Inflate the menu; this adds items to the action bar if it is present.
     * @param menu Menu object
     * @return boolean true
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_login, menu);
        return true;
    }

    /**
     * Handle action bar item clicks here. The action bar will
     * automatically handle clicks on the Home/Up button, so long
     * as you specify a parent activity in AndroidManifest.xml.
     * @param item MenuItem object
     * @return boolean true
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        // remove the settings button
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
