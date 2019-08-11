package com.deepspring.lib.ui.adapter;


import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import java.util.List;

/**
 * Created by Anonym on 2017/9/13.
 */

//根据position返回对应位置的fragment
// FragmentPagerAdapter使用场景：显示一些不会变化的View，静态View，不请求网络，Page数量不多
public class ViewFragmentAdapter extends FragmentPagerAdapter {

    private List<Fragment> mFragments;


    public ViewFragmentAdapter(FragmentManager fm, List<Fragment> fragments) {
        super(fm);
        mFragments = fragments;
    }


    @Override
    public Fragment getItem(int position) {
        return mFragments.get(position);
    }

    @Override
    public int getCount() {
        return mFragments.size();
    }
}
