package com.niharika.android.groupexpensetracker;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;



public class FAQFragment extends Fragment {

    private void setFragmentTitle() {
        getActivity().setTitle(R.string.faq_fragment_title);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        //((MainActivity)getActivity()).showDrawer(false);
        super.onCreateOptionsMenu(menu, inflater);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_faq, container, false);
        setFragmentTitle();
        ((MainActivity)getActivity()).showDrawer(true);
        return view;
    }

}
