package app.githubgui.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.githubgui.R;

import java.util.HashMap;

/**
 * Created by andrewyang on 2015/8/2.
 */
public class LoginDialog extends DialogFragment {
    public static final String PREF_NAME = "Pref";
    public static final String IS_LOGIN = "isLoggedin";
    public static final String KEY_NAME = "name_or_email";
    public static final String KEY_PASSWORD = "password";

    SharedPreferences pref;
    SharedPreferences.Editor editor;
    Context context;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.context = getActivity().getApplicationContext();
        pref = context.getSharedPreferences(PREF_NAME, Activity.MODE_PRIVATE);
        editor = pref.edit();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final LayoutInflater inflater = getActivity().getLayoutInflater();
        String title = getResources().getString(R.string.login_title);
        builder.setView(inflater.inflate(R.layout.login_dialog_layout, null))
            .setPositiveButton("Sign In", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Dialog dialogView = (Dialog) LoginDialog.this.getDialog();
                    EditText nameET = (EditText) dialogView.findViewById(R.id.name_or_email);
                    EditText passwordET = (EditText) dialogView.findViewById(R.id.password);
                    createLoginSession(nameET.getText().toString(), passwordET.getText().toString());
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

    public HashMap<String, String> getUserDetails() {
        HashMap<String, String> user = new HashMap<String, String>();
        user.put(KEY_NAME, pref.getString(KEY_NAME, null));
        user.put(KEY_PASSWORD, pref.getString(KEY_PASSWORD, null));
        return user;
    }
}
