package com.example.teamnovapersonalprojectprojecting.ui.home;

import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.teamnovapersonalprojectprojecting.R;
import com.example.teamnovapersonalprojectprojecting.local.database.main.DB_DMList;
import com.example.teamnovapersonalprojectprojecting.local.database.main.LocalDBMain;
import com.example.teamnovapersonalprojectprojecting.socket.SocketEventListener;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private RecyclerView serverItemRecyclerView;
    private ProjectAdapter projectMyAdapter;
    private List<ProjectAdapter.MyItem> projectItemList;

    private DMAdapter dmAdapterMyAdapter;

    private RecyclerView serverListRecyclerView;
    private ServerListAdapter serverListAdapter;
    private List<ServerListAdapter.MyItem> serverListItemList;

    private SocketEventListener.EventListener eventListener;

    List<DMAdapter.DataModel> dmList;

    public static boolean isSelectedDMPage;

    private View view;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        isSelectedDMPage = true;
        dmList = new ArrayList<>();

        view = inflater.inflate(R.layout.fragment_home, container, false);

        FragmentManager fragmentManager = getParentFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.top_section, new TopSectionFragment());
        transaction.commit();

        eventListener = (jsonUtil)-> {
            setDMAdapterItemList();
            return false;
        };
        SocketEventListener.addEvent(SocketEventListener.eType.RELOAD_DM_LIST, eventListener);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        setHomeContent(view);
        setHomeServerList(view);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        SocketEventListener.removeEvent(SocketEventListener.eType.RELOAD_DM_LIST, eventListener);
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

        if(isSelectedDMPage){
            Log.d("HomeFragment", "setHomeContent: DM");
            serverItemRecyclerView.setAdapter(setDMAdapterItemList());
        } else {
            serverItemRecyclerView.setAdapter(setProjectItemList());
        }

    }

    private DMAdapter setDMAdapterItemList(){
        dmList.clear();
        int i = 0;
        try(Cursor cursor = LocalDBMain.GetTable(DB_DMList.class).getAllOrderByLastTime()){
            LocalDBMain.LOG(cursor.getCount());
            while (cursor.moveToNext()){
                i++;

                int channelId = cursor.getInt(0);
                int otherId = cursor.getInt(1);
                String lastTime = cursor.getString(2);
                String otherUsername = cursor.getString(3);
                LocalDBMain.LOG(i + " " + channelId + " " + otherId + " " + lastTime + " " + otherUsername + " ");

                dmList.add(new DMAdapter.DataModel(otherId, channelId));
            }
        }

        if (dmAdapterMyAdapter == null) {
            dmAdapterMyAdapter = new DMAdapter(dmList);
        } else {
            getActivity().runOnUiThread(() -> {
                dmAdapterMyAdapter.notifyDataSetChanged();
            });
        }
        return dmAdapterMyAdapter;
    }

    private ProjectAdapter setProjectItemList(){
        projectItemList = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            List<String> contentList = new ArrayList<>();
            for (int j = 1; j <= i; j++){
                contentList.add("체널 " + j);
            }
            projectItemList.add(new ProjectAdapter.MyItem("카테고리 " + i, contentList ));
        }
        return projectMyAdapter = new ProjectAdapter(projectItemList);
    }
}