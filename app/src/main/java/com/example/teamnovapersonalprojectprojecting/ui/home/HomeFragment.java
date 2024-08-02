package com.example.teamnovapersonalprojectprojecting.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.teamnovapersonalprojectprojecting.R;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private RecyclerView serverItemRecyclerView;
    private ChannelAdapter channelMyAdapter;
    private List<ChannelAdapter.MyItem> channelItemList;

    private DMAdapter dmAdapterMyAdapter;
    private List<DMAdapter.MyItem> dmAdapterItemList;

    private RecyclerView serverListRecyclerView;
    private ServerListAdapter serverListAdapter;
    private List<ServerListAdapter.MyItem> serverListItemList;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        setHomeContent(view);
        setHomeServerList(view);

        FragmentManager fragmentManager = getParentFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.top_section, new TopSectionFragment());
        transaction.commit();

        return view;
    }

    private void setHomeServerList(View view) {
        serverListRecyclerView = view.findViewById(R.id.server_list_recyclerview);
        serverListRecyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));

        serverListItemList = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            serverListItemList.add(new ServerListAdapter.MyItem(R.drawable.day_background));
        }
        serverListItemList.add(new ServerListAdapter.MyItem(R.drawable.ic_add));

        serverListAdapter = new ServerListAdapter(serverListItemList);
        serverListRecyclerView.setAdapter(serverListAdapter);
    }

    private void setHomeContent(View view){
        serverItemRecyclerView = view.findViewById(R.id.server_item_recyclerview);
        serverItemRecyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));

        channelItemList = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            List<String> contentList = new ArrayList<>();
            for (int j = 1; j <= i; j++){
                contentList.add("체널 " + j);
            }
            channelItemList.add(new ChannelAdapter.MyItem("카테고리 " + i, contentList ));
        }

        channelMyAdapter = new ChannelAdapter(channelItemList);


        dmAdapterItemList = new ArrayList<>();
        for (int i = 1; i <= 20; i++) {
            dmAdapterItemList.add(new DMAdapter.MyItem(R.drawable.ic_account_black_24dp, "사용자 " + i));
        }

        dmAdapterMyAdapter = new DMAdapter(dmAdapterItemList);

        //현제는 dmadapter를 보여줌
        serverItemRecyclerView.setAdapter(dmAdapterMyAdapter);
        //serverItemRecyclerView.setAdapter(channelMyAdapter);
    }
}