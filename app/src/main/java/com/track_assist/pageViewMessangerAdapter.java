package com.track_assist;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.track_assist.Home;
import com.track_assist.lastVisited;
import com.track_assist.profile;

public class pageViewMessangerAdapter extends FragmentPagerAdapter {
    private final String semesterName;
    private final String sectionName;
    private final String srn;

    public pageViewMessangerAdapter(@NonNull FragmentManager fm, int behavior, String semesterName, String sectionName, String srn) {
        super(fm, behavior);
        this.semesterName = semesterName;
        this.sectionName = sectionName;
        this.srn = srn;
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        Fragment fragment = null;
        Bundle bundle = new Bundle();
        bundle.putString("semesterName", semesterName);
        bundle.putString("sectionName", sectionName);
        bundle.putString("srn", srn);

        switch (position) {
            case 0:
                fragment = new Home();
                break;
            case 1:
                fragment = new lastVisited();
                break;
            case 2:
                fragment = new profile();
                break;
        }

        if (fragment != null) {
            fragment.setArguments(bundle);
        }

        return fragment;
    }

    @Override
    public int getCount() {
        return 3; // Total number of tabs
    }


    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return "home";
            case 1:
                return "lastvisited";
            case 2:
                return "profile";
            default:
                return null;
        }
    }
}
