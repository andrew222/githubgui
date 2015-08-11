package app.githubgui.app;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.githubgui.R;
import com.squareup.picasso.Picasso;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * Created by andrewyang on 2015/8/9.
 */
public class AsyncAPIRequest extends AsyncTask<String, Void, String> {

    SharedPreferences pref;
    SharedPreferences.Editor editor;
    private Context mContext;
    private Activity mActivity;
    private String mRequestType;

    public AsyncAPIRequest(Activity activity, String requestType) {
        this.mActivity = activity;
        this.mContext = activity.getApplicationContext();
        this.mRequestType = requestType;
    }

    @Override
    protected void onPreExecute() {
        pref = mContext.getSharedPreferences(LoginDialog.PREF_NAME, Activity.MODE_PRIVATE);
        editor = pref.edit();
    }

    @Override
    protected String doInBackground(String... urls) {
        return Get(urls[0]);
    }

    @Override
    protected void onPostExecute(String response) {
        switch (mRequestType) {
            case "repos":
                editor.putString(LoginDialog.GITHUB_REPOS, response);
                Log.d("TAG", "onPostExecute " + response);
                fillRepos(response, pref, mActivity, mContext);
                break;
            case "stars":
                editor.putString(LoginDialog.GITHUB_STARS, response);
                Log.d("TAG", "onPostExecute " + response);
                fillStars(response, pref, mActivity, mContext);
            default:
                editor.putString(LoginDialog.GITHUB_USER, response);
                break;
        }
        editor.commit();
        fillUserInfo(response, pref, mActivity, mContext);
    }

    public String Get(String url) {

        InputStream inputStream = null;
        String result = "";
        try {
            HttpClient httpClient = new DefaultHttpClient();
            HttpGet httpGet = new HttpGet(url);
            if(mRequestType == "stars") {
                httpGet.addHeader("Accept", "application/vnd.github.v3.star+json");
            }
            HttpResponse httpResponse = httpClient.execute(httpGet);
            inputStream = httpResponse.getEntity().getContent();
            if (inputStream != null) {
                result = convertInputStreamToString(inputStream);
            } else {
                result = "Don't work";
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }


    private static String convertInputStreamToString(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(inputStream));
        String line = "";
        String result = "";
        while((line = bufferedReader.readLine()) != null)
            result += line;

        inputStream.close();
        return result;
    }

    public static void fillUserInfo(String userInof, SharedPreferences pref, Activity mActivity, Context mContext) {
        SharedPreferences.Editor editor = pref.edit();
        String name = pref.getString(LoginDialog.KEY_NAME, null);
        String password = pref.getString(LoginDialog.KEY_PASSWORD, null);
        Boolean is_logged_in = pref.getBoolean(LoginDialog.IS_LOGIN, false);

        ImageView avatar = (ImageView) mActivity.findViewById(R.id.github_avatar);
        TextView userNameTv = (TextView) mActivity.findViewById(R.id.user_name);
        TextView emailTv = (TextView) mActivity.findViewById(R.id.email);
        TextView joinedAtTv = (TextView) mActivity.findViewById(R.id.joined_at);
        TextView followsTv = (TextView) mActivity.findViewById(R.id.follows);
        TextView followingTv = (TextView) mActivity.findViewById(R.id.following);
        if(is_logged_in) {
            String currentGithubUser = pref.getString(LoginDialog.GITHUB_USER, "");
            if (currentGithubUser != "") {
                GithubUser githubUser = LoginDialog.parseGithubUser(currentGithubUser);
                if(githubUser.getName() != userNameTv.getText()) {
                    userNameTv.setText(githubUser.getName());
                }
                if(githubUser.getAvatarUrl() != "") {
                    try {
                        Glide.with(mContext).load(githubUser.getAvatarUrl()).into(avatar);
                    }catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                if(githubUser.getEmail() != null && githubUser.getEmail() != emailTv.getText()) {
                    emailTv.setText(Html.fromHtml("<a href=\"mailto:" + githubUser.getEmail() + "\">" + githubUser.getEmail() + "</a>"));
                    emailTv.setMovementMethod(LinkMovementMethod.getInstance());
                }
                if(githubUser.getCreatedAt() != joinedAtTv.getText() && githubUser.getCreatedAt() != null) {
                    joinedAtTv.setText(githubUser.getCreatedAt().substring(0, 10));
                }
                if(githubUser.getFollowers() != followsTv.getText()) {
                    followsTv.setText(githubUser.getFollowers());
                }
                if(githubUser.getFollowing() != followingTv.getText()) {
                    followingTv.setText(githubUser.getFollowing());
                }
                ViewPager viewPager = (ViewPager) mActivity.findViewById(R.id.viewpager);
                PagerAdapter adapter = viewPager.getAdapter();
            }
        }
    }

    public void fillRepos(String repos, SharedPreferences pref, Activity mActivity, Context mContext) {
        ArrayList<GithubRepositories> reposObjs = LoginDialog.parseGithubRepos(repos);
        for(GithubRepositories rep : reposObjs) {
            Log.d("TAG", "fillRepos :" + rep.getDescription());
        }
    }

    public void fillStars(String repos, SharedPreferences pref, Activity mActivity, Context mContext) {
        ArrayList<GithubStarred> starsObjs = LoginDialog.parseGithubStars(repos);
        for(GithubStarred rep : starsObjs) {
            Log.d("TAG", "fillRepos :" + rep.getStarredAt());
        }
    }
}
