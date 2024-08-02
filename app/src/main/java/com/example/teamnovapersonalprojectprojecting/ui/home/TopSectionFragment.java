package com.example.teamnovapersonalprojectprojecting.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.teamnovapersonalprojectprojecting.JoinActivity;
import com.example.teamnovapersonalprojectprojecting.LoginActivity;
import com.example.teamnovapersonalprojectprojecting.MainActivity;
import com.example.teamnovapersonalprojectprojecting.R;
import com.example.teamnovapersonalprojectprojecting.SearchActivity;

public class TopSectionFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        int topSection = R.layout.item_topsection_dm;
        View view = inflater.inflate(topSection , container, false);

        if(topSection == R.layout.item_topsection_dm){
            Button showFriendAddDialogButton = view.findViewById(R.id.add_friend_dialog_button);
            showFriendAddDialogButton.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    FriendAddDialogFragment dialogFragment = new FriendAddDialogFragment();
                    dialogFragment.show(getFragmentManager(), "FriendAddDialogFragment");
                }
            });
        } else if (topSection == R.layout.item_topsection_project){
            Button searchButton = view.findViewById(R.id.search_button);
            searchButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getActivity(), SearchActivity.class);
                    startActivity(intent);
                }
            });

            Button addMemberButton = view.findViewById(R.id.add_project_member_button);
            addMemberButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MemberAddDialogFragment dialogFragment = new MemberAddDialogFragment();
                    dialogFragment.show(getFragmentManager(), "MemberAddDialogFragment");
                }
            });

            TextView projectNameTextView = view.findViewById(R.id.project_name);
            projectNameTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ProjectSettingDialogFragment dialogFragment = new ProjectSettingDialogFragment();
                    dialogFragment.show(getFragmentManager(), "ProjectSettingDialogFragment");
                }
            });


        }
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }
}
