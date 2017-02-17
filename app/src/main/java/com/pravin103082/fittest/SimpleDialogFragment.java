package com.pravin103082.fittest;

import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by Pravin103082 on 27-01-2017.
 */

public class SimpleDialogFragment extends DialogFragment
{
    TextView dialogFragment;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_sample_dialog, container, false);
        dialogFragment= (TextView) rootView.findViewById(R.id.dialogFragment);
        getDialog().setTitle("Simple Dialog");
        return rootView;
    }
}
