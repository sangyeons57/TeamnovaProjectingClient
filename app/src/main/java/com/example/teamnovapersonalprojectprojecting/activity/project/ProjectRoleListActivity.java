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
import java.util.Iterator;
import java.util.List;

public class ProjectRoleListActivity extends AppCompatActivity {
    private  int projectId;
    private  String projectName;

    private Button addRoleButton;
    private RecyclerView rolesRecyclerView;

    private RolesAdapter rolesAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_role_list);
        DataManager.Instance().currentContext = this;

        addRoleButton = findViewById(R.id.addRoleButton);
        rolesRecyclerView = findViewById(R.id.membersRecyclerView);
        Intent intent = getIntent();
        projectId = intent.getIntExtra(ProjectSettingDialogFragment.PROJECT_ID, DataManager.NOT_SETUP_I);
        projectName = intent.getStringExtra(ProjectSettingDialogFragment.PROJECT_NAME);


        addRoleButton.setOnClickListener(this::onClickAddRoleButton);

        List<RolesAdapter.Item> itemList = new ArrayList<>();
        rolesAdapter = new RolesAdapter(itemList);
        rolesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        rolesRecyclerView.setAdapter(rolesAdapter);

    }

    @Override
    protected void onResume() {
        super.onResume();
        DataManager.Instance().currentContext = this;

        rolesAdapter.clearRole();
        SocketConnection.sendMessage(new JsonUtil()
                .add(JsonUtil.Key.TYPE, SocketEventListener.eType.GET_PROJECT_DATA)
                .add(JsonUtil.Key.PROJECT_ID, projectId)
                .add(JsonUtil.Key.DATA, new JSONArray()
                        .put("Role")));
        SocketEventListener.addAddEventQueue(SocketEventListener.eType.GET_PROJECT_DATA, new SocketEventListener.EventListenerOnce(SocketEventListener.eType.GET_PROJECT_DATA) {
            @Override
            public boolean runOnce(JsonUtil jsonUtil) {
                JSONObject data = jsonUtil.getJsonObject(JsonUtil.Key.DATA, new JSONObject());
                try {
                    JSONObject role = data.getJSONObject("Role");
                    role.remove("0");

                    Log.d(ProjectRoleListActivity.class.getSimpleName(), role.toString());
                    Iterator<String> keys = role.keys();
                    while (keys.hasNext()) {
                        String key = keys.next();
                        JSONObject jsonObject = role.getJSONObject(key);
                        Log.d(ProjectRoleListActivity.class.getSimpleName(),"Key: " + key + " RoleName: " + jsonObject.getString("RoleName"));
                        rolesAdapter.addRole(new RolesAdapter.Item(jsonObject.getString("RoleName")));
                    }
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
                DataManager.Instance().mainHandler.post(()->{
                    rolesAdapter.notifyDataSetChanged();
                });
                return false;
            }
        });
    }

    private void onClickAddRoleButton(View view){
        Intent intent = new Intent(this, EditRoleActivity.class);
        intent.putExtra(EditRoleActivity.PROJECT_ID, projectId);
        intent.putExtra(EditRoleActivity.IS_ADD_MODE, true);
        startActivity(intent);
    }

    public static class RolesAdapter extends RecyclerView.Adapter<RolesAdapter.RoleViewHolder> {

        private List<Item> itemList;

        public RolesAdapter(List<Item> itemList) {
            this.itemList = itemList;
        }

        @NonNull
        @Override
        public RoleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_button, parent, false);
            return new RoleViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RoleViewHolder holder, int position) {
            holder.roleButton.setText(itemList.get(position).roleName);
            holder.roleButton.setOnClickListener((view)->{
                //이거 클릭시 역할 편집 기능 실행

            });
        }

        @Override
        public int getItemCount() {
            return itemList.size();
        }

        public RolesAdapter clearRole() {
            itemList.clear();
            return this;
        }
        public RolesAdapter addRole(Item item) {
            itemList.add(item);
            DataManager.Instance().mainHandler.post(()->{
                notifyItemInserted(itemList.size() - 1);
            });
            return this;
        }

        public static class RoleViewHolder extends RecyclerView.ViewHolder {
            Button roleButton;

            public RoleViewHolder(@NonNull View itemView) {
                super(itemView);
                roleButton = itemView.findViewById(R.id.button);
            }
        }

        public static class Item {
            public String roleName;
            public Item(String roleName){
                this.roleName = roleName;
            }
        }
    }
}
