package com.example.teamnovapersonalprojectprojecting.activity.project;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.teamnovapersonalprojectprojecting.R;
import com.example.teamnovapersonalprojectprojecting.socket.SocketConnection;
import com.example.teamnovapersonalprojectprojecting.socket.SocketEventListener;
import com.example.teamnovapersonalprojectprojecting.util.DataManager;
import com.example.teamnovapersonalprojectprojecting.util.JsonUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ProjectEditMemberListActivity extends AppCompatActivity {
    private MembersAdapter membersAdapter;

    private RecyclerView membersRecyclerView;

    private int projectId;
    private String projectName;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_member_list);
        DataManager.Instance().currentContext = this;

        membersRecyclerView = findViewById(R.id.membersRecyclerView);

        Intent intent = getIntent();
        projectId = intent.getIntExtra(ProjectSettingDialogFragment.PROJECT_ID, DataManager.NOT_SETUP_I);
        projectName = intent.getStringExtra(ProjectSettingDialogFragment.PROJECT_NAME);

        List<MembersAdapter.Item> itemList = new ArrayList<>();
        membersAdapter = new MembersAdapter(itemList, projectId);
        membersRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        membersRecyclerView.setAdapter(membersAdapter);

    }

    @Override
    protected void onResume() {
        super.onResume();
        DataManager.Instance().currentContext = this;

        SocketConnection.sendMessage(new JsonUtil()
                .add(JsonUtil.Key.TYPE, SocketEventListener.eType.GET_PROJECT_MEMBER)
                .add(JsonUtil.Key.PROJECT_ID, projectId));

        SocketEventListener.addAddEventQueue(SocketEventListener.eType.GET_PROJECT_MEMBER, new SocketEventListener.EventListenerOnce(SocketEventListener.eType.GET_PROJECT_MEMBER) {
            @Override
            public boolean runOnce(JsonUtil jsonUtil) {
                JSONArray data = jsonUtil.getJsonArray(JsonUtil.Key.DATA, new JSONArray());
                membersAdapter.clearMember();
                try {
                    for(int i = 0; i < data.length(); i++) {
                        JSONObject jsonObject = data.getJSONObject(i);
                        membersAdapter.addMember(new MembersAdapter.Item(
                                jsonObject.getString("username"),
                                jsonObject.getString("memberId"),
                                DataManager.JsonArrayToIntegerList(jsonObject.getJSONArray("roleList")))
                        );
                    }
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }

                DataManager.Instance().mainHandler.post(()->{
                    membersAdapter.notifyDataSetChanged();
                });
                return false;
            }
        });
    }

    public static class MembersAdapter extends RecyclerView.Adapter<MembersAdapter.RoleViewHolder> {

        private List<MembersAdapter.Item> itemList;
        private int projectId;

        public MembersAdapter(List<MembersAdapter.Item> itemList, int projectId) {
            this.itemList = itemList;
            this.projectId = projectId;
        }

        @NonNull
        @Override
        public MembersAdapter.RoleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_button, parent, false);
            return new MembersAdapter.RoleViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull MembersAdapter.RoleViewHolder holder, int position) {
            holder.memberButton.setText(itemList.get(position).username);
            holder.memberButton.setOnClickListener((view)->{
                //이거 클릭시 역할 편집 기능 실행
                Intent intent = new Intent(DataManager.Instance().currentContext, ProjectEditMemberActivity.class);
                intent.putExtra(ProjectEditMemberActivity.USERNAME, itemList.get(position).username);
                intent.putExtra(ProjectEditMemberActivity.PROJECT_ID, projectId);
                intent.putExtra(ProjectEditMemberActivity.MEMBER_ID, itemList.get(position).memberId);
                intent.putIntegerArrayListExtra(ProjectEditMemberActivity.ROLE_LIST, itemList.get(position).roleData);
                DataManager.Instance().currentContext.startActivity(intent);
            });
        }

        @Override
        public int getItemCount() {
            return itemList.size();
        }

        public MembersAdapter clearMember(){
            itemList.clear();
            return this;
        }
        public MembersAdapter addMember(MembersAdapter.Item item) {
            itemList.add(item);
            return this;
        }

        public static class RoleViewHolder extends RecyclerView.ViewHolder {
            Button memberButton;

            public RoleViewHolder(@NonNull View itemView) {
                super(itemView);
                memberButton = itemView.findViewById(R.id.button);
            }
        }

        public static class Item {
            public String username;
            public String memberId;
            public ArrayList<Integer> roleData;

            public Item(String username, String memberId, List<Integer> roleData){
                this.username = username;
                this.memberId = memberId;
                this.roleData = (ArrayList<Integer>) roleData;
            }
        }
    }
}
