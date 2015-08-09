/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.githubgui;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.githubgui.R;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import app.githubgui.app.GithubUser;
import app.githubgui.app.LoginDialog;

/**
 * TODO
 */
public class MainActivity extends AppCompatActivity {
    SharedPreferences pref;
    SharedPreferences.Editor editor;
    private DrawerLayout mDrawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final ActionBar ab = getSupportActionBar();
        ab.setHomeAsUpIndicator(R.drawable.ic_menu);
        ab.setDisplayHomeAsUpEnabled(true);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        if (navigationView != null) {
            setupDrawerContent(navigationView);
        }

        ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        if (viewPager != null) {
            setupViewPager(viewPager);
        }


        pref = getSharedPreferences(LoginDialog.PREF_NAME, Activity.MODE_PRIVATE);
        editor = pref.edit();
        String name = pref.getString(LoginDialog.KEY_NAME, null);
        String password = pref.getString(LoginDialog.KEY_PASSWORD, null);
        Boolean is_logged_in = pref.getBoolean(LoginDialog.IS_LOGIN, false);

        ImageView avatar = (ImageView) findViewById(R.id.avatar);
        TextView userNameTv = (TextView) findViewById(R.id.user_name);
        TextView emailTv = (TextView) findViewById(R.id.email);
        TextView joinedAtTv = (TextView) findViewById(R.id.joined_at);
        TextView followsTv = (TextView) findViewById(R.id.follows);
        TextView followingTv = (TextView) findViewById(R.id.following);
        if(is_logged_in) {
            if(userNameTv.getText() == "") {
                String currentGithubUser = pref.getString(LoginDialog.GITHUB_USER, "");
                if (currentGithubUser != "") {
                    GithubUser githubUser = LoginDialog.parseGithubUser(currentGithubUser);
                    if(githubUser.getName() != userNameTv.getText()) {
                        userNameTv.setText(githubUser.getName());
                    }
                    if(githubUser.getAvatarUrl() != "") {
                        try {
                            Picasso.with(getApplicationContext()).load(githubUser.getAvatarUrl()).resize(250, 250).into(avatar);
                        }catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    if(githubUser.getEmail() != null && githubUser.getEmail() != emailTv.getText()) {
                        emailTv.setText(Html.fromHtml("<a href=\"mailto:" + githubUser.getEmail() + "\">" + githubUser.getEmail() + "</a>"));
                        emailTv.setMovementMethod(LinkMovementMethod.getInstance());
                    }
                    if(githubUser.getCreatedAt() != joinedAtTv.getText()) {
                        joinedAtTv.setText(githubUser.getCreatedAt().substring(0, 10));
                    }
                    if(githubUser.getFollowers() != followsTv.getText()) {
                        followsTv.setText(githubUser.getFollowers());
                    }
                    if(githubUser.getFollowing() != followingTv.getText()) {
                        followingTv.setText(githubUser.getFollowing());
                    }
                }
            }
        }


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Here's a Snackbar", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.sample_actions, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupViewPager(ViewPager viewPager) {
        Adapter adapter = new Adapter(getSupportFragmentManager());
        adapter.addFragment(new CheeseListFragment(), "Category 1");
        adapter.addFragment(new CheeseListFragment(), "Category 2");
        adapter.addFragment(new CheeseListFragment(), "Category 3");
        viewPager.setAdapter(adapter);
    }

    private void setupDrawerContent(NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                menuItem.setChecked(true);
                String selectItem = menuItem.getTitle().toString();
                switch (selectItem) {
                    case "Login":
                        LoginDialog loginDialog = new LoginDialog();
                        loginDialog.show(getFragmentManager(), "Login");
                        break;
                }
                mDrawerLayout.closeDrawers();
                return true;
            }
        });
    }

    static class Adapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragments = new ArrayList<>();
        private final List<String> mFragmentTitles = new ArrayList<>();

        public Adapter(FragmentManager fm) {
            super(fm);
        }

        public void addFragment(Fragment fragment, String title) {
            mFragments.add(fragment);
            mFragmentTitles.add(title);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragments.get(position);
        }

        @Override
        public int getCount() {
            return mFragments.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitles.get(position);
        }
    }
}
