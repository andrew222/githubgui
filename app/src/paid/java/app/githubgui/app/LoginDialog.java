package app.githubgui.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.Preference;
import android.support.annotation.NonNull;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.githubgui.MainActivity;
import com.githubgui.R;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by andrewyang on 2015/8/2.
 */
public class LoginDialog extends DialogFragment {
    public static final String PREF_NAME = "Pref";
    public static final String IS_LOGIN = "isLoggedin";
    public static final String KEY_NAME = "name_or_email";
    public static final String KEY_PASSWORD = "password";
    public static final String GITHUB_USER = "current_github_user";
    public static final String GITHUB_REPOS = "repos";
    public static final String GITHUB_STARS = "stars";
    public View rootView;
    public ProgressDialog proDialog;

    SharedPreferences pref;
    SharedPreferences.Editor editor;
    Context context;
    Activity activity;
    public User currentUser;
    public AlertDialog.Builder builder;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.context = getActivity().getApplicationContext();
        this.activity = getActivity();
        pref = context.getSharedPreferences(PREF_NAME, Activity.MODE_PRIVATE);
        editor = pref.edit();
        currentUser = getUserDetails();
        builder = new AlertDialog.Builder(getActivity());
    }

    @Override
    public void onResume() {
        super.onResume();
        Dialog dialog = this.getDialog();
        EditText name = (EditText) dialog.findViewById(R.id.name_or_email);
        EditText password = (EditText) dialog.findViewById(R.id.password);
        if(currentUser != null) {
            name.setText(currentUser.getName());
            password.setText(currentUser.getPassword());
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final LayoutInflater inflater = getActivity().getLayoutInflater();
        String title = getResources().getString(R.string.login_title);
        builder.setView(inflater.inflate(R.layout.login_dialog_layout, null))
            .setPositiveButton("Sign In", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Dialog dialogView = (Dialog) LoginDialog.this.getDialog();
                    EditText nameET = (EditText) dialogView.findViewById(R.id.name_or_email);
                    String user_name = nameET.getText().toString();
                    String old_user_name = pref.getString(KEY_NAME, "");
                    EditText passwordET = (EditText) dialogView.findViewById(R.id.password);
                    if (user_name != "" && old_user_name != user_name) {
                        createLoginSession(user_name, passwordET.getText().toString());
                        String url = "https://api.github.com/users/" + user_name;
                        new AsyncAPIRequest(activity, "").execute(url);

                        proDialog = new ProgressDialog(activity);
                        proDialog.show();
                    }
                }
            })
            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    LoginDialog.this.getDialog().cancel();
                }
            });
        return builder.create();
    }

    public void createLoginSession(String name, String password) {
        editor.putBoolean(IS_LOGIN, true);
        editor.putString(KEY_NAME, name);
        editor.putString(KEY_PASSWORD, password);
        editor.commit();
    }

    public User getUserDetails() {
        User user = new User(pref.getString(KEY_NAME, null), pref.getString(KEY_PASSWORD, null));
        return user;
    }

    public static GithubUser parseGithubUser(String json) {
        Gson gson = new Gson();
        return gson.fromJson(json, GithubUser.class);
    }
    public static ArrayList<GithubRepositories> parseGithubRepos(String json) {
        Type listOfGithubRepositories = new TypeToken<List<GithubRepositories>>(){}.getType();
        Gson gson = new Gson();
        return gson.fromJson(json,listOfGithubRepositories);
    }

    public static ArrayList<GithubStarred> parseGithubStars(String json) {
        Type listOfGithubStars = new TypeToken<List<GithubStarred>>(){}.getType();
        Gson gson = new Gson();
        return gson.fromJson(json,listOfGithubStars);
    }
}
