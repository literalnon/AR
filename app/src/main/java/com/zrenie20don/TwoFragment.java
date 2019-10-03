package com.zrenie20don;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by Кирилл on 10.12.2017.
 */

public class TwoFragment extends Fragment {


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = getActivity().getLayoutInflater().inflate(
                R.layout.two_new,
                container,
                false
        );
        return view;
    }
}
