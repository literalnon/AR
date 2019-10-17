package com.zrenie20don;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by Кирилл on 10.12.2017.
 */

public class OneFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            final View view = getActivity().getLayoutInflater().inflate(
                    R.layout.preview_fragment,
                    container,
                    false
            );
            return view;


    }
}
