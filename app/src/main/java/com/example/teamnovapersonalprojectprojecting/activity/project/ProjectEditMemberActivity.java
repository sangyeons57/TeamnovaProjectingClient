package com.example.teamnovapersonalprojectprojecting.activity.project;

import android.content.Intent;
import android.os.Bundle;
import android.os.Debug;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

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

public class ProjectEditMemberActivity extends AppCompatActivity {
    public static final String PROJECT_ID = "PROJECT_ID";
    public static final String MEMBER_ID = "MEMBER_ID";
    public static final String USERNAME = "USERNAME";
    public static final String ROLE_LIST = "ROLE_LIST";

    private int projectId;
    private String memberId;
    private List<Integer> roleList;

    private RecyclerView rolesRecyclerView;
    private Button cancelButton;
    private Button saveButton;
    private TextView userNameTextView;

    private CheckBoxAdapter checkBoxAdapter;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_member);
        DataManager.Instance().currentContext = this;

        rolesRecyclerView = findViewById(R.id.rolesRecyclerView);
        cancelButton = findViewById(R.id.cancelButton);
        saveButton = findViewById(R.id.saveButton);
        userNameTextView = findViewById(R.id.userNameTextView);


        Intent intent = getIntent();
        this.memberId = intent.getStringExtra(MEMBER_ID);
        this.roleList = intent.getIntegerArrayListExtra(ROLE_LIST);
        this.projectId = intent.getIntExtra(PROJECT_ID, DataManager.NOT_SETUP_I);

        List<CheckBoxAdapter.Item> itemList = new ArrayList<>();
        checkBoxAdapter = new CheckBoxAdapter(itemList);
        rolesRecyclerView.setAdapter(checkBoxAdapter);
        rolesRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        userNameTextView.setText(intent.getStringExtra(USERNAME));

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
                        int intKey = Integer.parseInt(key);
                        JSONObject jsonObject = role.getJSONObject(key);

                        checkBoxAdapter.addItem(new CheckBoxAdapter.Item(intKey, jsonObject.getString("RoleName"), roleList.contains(intKey)));
                    }
                    DataManager.Instance().mainHandler.post(()->{
                        checkBoxAdapter.notifyDataSetChanged();
                    });
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
                return false;
            }
        });

        cancelButton.setOnClickListener((view)->{finish();});
        saveButton.setOnClickListener((view)->{
            SocketConnection.sendMessage(false, new JsonUtil()
                    .add(JsonUtil.Key.TYPE, SocketEventListener.eType.SET_MEMBER_ROLE)
                    .add(JsonUtil.Key.ID, memberId)
                    .add(JsonUtil.Key.DATA, new JSONArray(checkBoxAdapter.getSelectedItems())));
            finish();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        DataManager.Instance().currentContext = this;
    }

    public static class CheckBoxAdapter extends RecyclerView.Adapter<CheckBoxAdapter.ViewHolder> {

        private final List<Item> itemList;

        public CheckBoxAdapter(List<Item> itemList) {
            this.itemList = itemList;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_edit_member_role_element, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Item item = itemList.get(position);
            holder.checkBox.setText(item.roleName);

            // 선택 상태를 CheckBox에 반영
            holder.checkBox.setChecked(item.isSelected);

            // CheckBox 선택 시 선택 상태 업데이트
            holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                item.isSelected = isChecked;
            });
        }

        @Override
        public int getItemCount() {
            return itemList.size();
        }

        public CheckBoxAdapter addItem(Item item) {
            itemList.add(item);
            return this;
        }

        // 선택된 항목 리스트 반환
        public List<Integer> getSelectedItems() {
            List<Integer> selectedOptions = new ArrayList<>();
            for (Item item : itemList) {
                if (item.isSelected) {
                    selectedOptions.add(item.roleId);
                }
            }
            return selectedOptions;
        }

        public static class ViewHolder extends RecyclerView.ViewHolder {
            CheckBox checkBox;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                checkBox = itemView.findViewById(R.id.checkbox);
            }
        }

        public static class Item {
            public int roleId;
            public String roleName;
            public boolean isSelected;
            public Item(int roleId, String roleName, boolean isSelected) {
                this.roleId = roleId;
                this.roleName = roleName;
                this.isSelected = isSelected;
            }
        }
    }
}
