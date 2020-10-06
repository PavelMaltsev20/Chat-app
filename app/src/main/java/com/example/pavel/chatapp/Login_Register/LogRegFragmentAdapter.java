package com.example.pavel.chatapp.Login_Register;

import android.content.Context;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import java.util.ArrayList;

public class LogRegFragmentAdapter extends FragmentPagerAdapter {

    Context context;
    ArrayList<Fragment> fragments;
    ArrayList<String> titles;

    public LogRegFragmentAdapter(FragmentManager fm, Context context) {
        super(fm);
        this.context=context;
        this.fragments=new ArrayList<>();
        this.titles=new ArrayList<>();
    }

    @Override
    public Fragment getItem(int i) {
        return fragments.get(i);
    }

    @Override
    public int getCount() {
        return fragments.size();
    }

    public void addFragment(Fragment fragment,String title){
        fragments.add(fragment);
        titles.add(title);
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return titles.get(position);
    }

}
