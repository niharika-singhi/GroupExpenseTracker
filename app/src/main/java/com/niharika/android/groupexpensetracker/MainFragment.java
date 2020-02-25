package com.niharika.android.groupexpensetracker;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


/**
 * A simple {@link Fragment} subclass.
 */
public class MainFragment extends Fragment {

    public static final String TAG = "DebugExpenseTracker";
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    @Override
    public void onStart() {
        super.onStart();
    }

    public void updateUI(FirebaseUser currentUser) {
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            Navigation.findNavController(getView()).navigate(R.id.accountTabFragment);
        } else {
            Navigation.findNavController(getView()).navigate(R.id.registerFragment);
        }
    }

    public void onResume() {
        super.onResume();
        updateUI(currentUser);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        if (!AccountLab.get(getContext()).isNetworkAvailableAndConnected()) {
            NavController navController = Navigation.findNavController(getActivity(), R.id.nav_host_fragment);
            navController.navigate(R.id.errFragment);
            return view;
        }
        return view;
    }
}
