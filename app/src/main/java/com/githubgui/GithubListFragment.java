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
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import app.githubgui.app.GithubRepositories;
import app.githubgui.app.GithubStarred;
import app.githubgui.app.LoginDialog;

public class GithubListFragment extends Fragment {
    private Activity mActivity;
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private ArrayList<GithubRepositories> reposObjs;
    private ArrayList<GithubStarred> starsObjs;
    private String fragmentTyep;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mActivity = getActivity();
        this.pref = mActivity.getSharedPreferences(LoginDialog.PREF_NAME, Activity.MODE_PRIVATE);
        this.editor = pref.edit();
        reposObjs = LoginDialog.parseGithubRepos(pref.getString(LoginDialog.GITHUB_REPOS, ""));
        starsObjs = LoginDialog.parseGithubStars(pref.getString(LoginDialog.GITHUB_STARS, ""));
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if(isVisibleToUser) {
            Toast.makeText(mActivity.getApplicationContext(), "is Visible to user", Toast.LENGTH_LONG).show();
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        RecyclerView rv = (RecyclerView) inflater.inflate(
                R.layout.fragment_github_list, container, false);
        setupRecyclerView(rv);
        return rv;
    }

    private void setupRecyclerView(RecyclerView recyclerView) {
        recyclerView.setLayoutManager(new LinearLayoutManager(recyclerView.getContext()));
        recyclerView.setAdapter(new SimpleStringRecyclerViewAdapter(getActivity(),
                getRandomSublist()));
    }

    private List<String> getRandomSublist() {
        ArrayList<String> list = new ArrayList<>();
        Bundle bundle = this.getArguments();
        fragmentTyep = bundle.getString("type", "");
        switch (fragmentTyep) {
            case "Repositories":
                if(reposObjs != null) {
                    for (GithubRepositories repo : reposObjs) {
                        list.add(repo.getName());
                    }
                }
                break;
            case "Stars":
                if(starsObjs != null) {
                    for (GithubStarred star : starsObjs) {
                        list.add(star.getStarredAt());
                    }
                }
                break;
            case "Activities":
                if(reposObjs != null) {
                    for (GithubRepositories repo : reposObjs) {
                        list.add(String.valueOf(repo.getReposId()));
                    }
                }
                break;
        }
        return list;
    }

    public static class SimpleStringRecyclerViewAdapter
            extends RecyclerView.Adapter<SimpleStringRecyclerViewAdapter.ViewHolder> {

        private final TypedValue mTypedValue = new TypedValue();
        private int mBackground;
        private List<String> mValues;

        public static class ViewHolder extends RecyclerView.ViewHolder {
            public String mBoundString;

            public final View mView;
            public final ImageView mImageView;
            public final TextView mTextView;

            public ViewHolder(View view) {
                super(view);
                mView = view;
                mImageView = (ImageView) view.findViewById(R.id.avatar);
                mTextView = (TextView) view.findViewById(android.R.id.text1);
            }

            @Override
            public String toString() {
                return super.toString() + " '" + mTextView.getText();
            }
        }

        public String getValueAt(int position) {
            return mValues.get(position);
        }

        public SimpleStringRecyclerViewAdapter(Context context, List<String> items) {
            context.getTheme().resolveAttribute(R.attr.selectableItemBackground, mTypedValue, true);
            mBackground = mTypedValue.resourceId;
            mValues = items;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.list_item, parent, false);
            view.setBackgroundResource(mBackground);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            holder.mBoundString = mValues.get(position);
            holder.mTextView.setText(mValues.get(position));

            holder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Context context = v.getContext();
                    Intent intent = new Intent(context, CheeseDetailActivity.class);
                    intent.putExtra(CheeseDetailActivity.EXTRA_NAME, holder.mBoundString);

                    context.startActivity(intent);
                }
            });

            Glide.with(holder.mImageView.getContext())
                    .load(GithubLists.getRandomCheeseDrawable())
                    .fitCenter()
                    .into(holder.mImageView);
        }

        @Override
        public int getItemCount() {
            return mValues.size();
        }
    }
}
