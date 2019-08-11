package com.deepspring.lib.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.deepspring.lib.R;

public class WalkFragment extends Fragment {

    @Nullable
    @Override
    //重写onCreateView方法
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_walk, container, false);
        return  view;
    }
}
