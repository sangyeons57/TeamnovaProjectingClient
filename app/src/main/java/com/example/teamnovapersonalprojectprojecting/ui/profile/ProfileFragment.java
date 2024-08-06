package com.example.teamnovapersonalprojectprojecting.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.teamnovapersonalprojectprojecting.PersonalSettingActivity;
import com.example.teamnovapersonalprojectprojecting.R;
import com.example.teamnovapersonalprojectprojecting.util.DataManager;
import com.example.teamnovapersonalprojectprojecting.FriendsActivity;

public class ProfileFragment extends Fragment {
    private Button statusButton;
    private Button friendsButton;
    private ImageButton personalSettingButton;

    private TextView profileName;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        statusButton = view.findViewById(R.id.set_status_button);
        personalSettingButton = view.findViewById(R.id.personal_setting_button);
        friendsButton = view.findViewById(R.id.friendsButton);
        profileName = view.findViewById(R.id.profile_name);



        statusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ChangeStatusDialogFragment dialogFragment = new ChangeStatusDialogFragment();
                dialogFragment.show(getFragmentManager(), "ChangeStatusDialogFragment");
            }
        });

        personalSettingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), PersonalSettingActivity.class));
            }
        });

        friendsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), FriendsActivity.class));
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        profileName.setText(DataManager.Instance().username);
    }
}