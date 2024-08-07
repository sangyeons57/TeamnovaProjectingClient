package com.example.teamnovapersonalprojectprojecting;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.teamnovapersonalprojectprojecting.ui.home.FriendAddDialogFragment;
import com.example.teamnovapersonalprojectprojecting.ui.profile.ChangeStatusDialogFragment;
import com.example.teamnovapersonalprojectprojecting.util.DataManager;
import com.example.teamnovapersonalprojectprojecting.util.JsonUtil;
import com.example.teamnovapersonalprojectprojecting.util.ServerConnectManager;
import com.example.teamnovapersonalprojectprojecting.util.WebSocketEcho;
import com.example.teamnovapersonalprojectprojecting.util.WebsocketManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FriendsActivity extends AppCompatActivity {
    private Button acceptFriendButton;
    private ImageButton backButton;
    private TextView addFriendTextView;
    private RecyclerView recyclerView;

    private List<DataModel> friendsList;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);
        this.recyclerView = findViewById(R.id.recyclerview);
        acceptFriendButton = findViewById(R.id.acceptFriendButton);
        backButton = findViewById(R.id.backButton);
        addFriendTextView = findViewById(R.id.addFriedTextView);

        friendsList = new ArrayList<>();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new FriendsActivity.DataAdapter(friendsList));

        acceptFriendButton.setOnClickListener(v -> {
            startActivity(new Intent(FriendsActivity.this, FriendsAcceptActivity.class));
        });

        backButton.setOnClickListener(v -> {
            finish();
        });

        addFriendTextView.setOnClickListener(v -> {
            FriendAddDialogFragment dialogFragment = new FriendAddDialogFragment();
            dialogFragment.show(getSupportFragmentManager(), "FriendAddDialogFragment");
        });

        ServerConnectManager serverConnectManager = new ServerConnectManager(ServerConnectManager.Path.FRIENDS.getPath("getFriendsList.php"))
                .add(JsonUtil.Key.USER_ID, DataManager.Instance().userId);

        serverConnectManager.postEnqueue(new ServerConnectManager.EasyCallback() {
            @Override
            protected void onGetJson(JSONObject jsonObject) throws IOException, JSONException {
                super.onGetJson(jsonObject);
                if (jsonObject.getString(JsonUtil.Key.STATUS.toString()).equals("success")) {
                    JSONArray jsonArray;
                    try {
                        jsonArray = jsonObject.getJSONArray("data");
                    } catch (JSONException e) {
                        jsonArray = new JSONArray();
                    }
                    Log.d("FriendsActivity", jsonArray.toString());

                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject waiting = new JSONObject(jsonArray.getString(i));
                        friendsList.add(new FriendsActivity.DataModel(waiting.getString(JsonUtil.Key.ID.toString()), waiting.getString(JsonUtil.Key.USERNAME.toString())));
                    }
                    runOnUiThread(() -> recyclerView.getAdapter().notifyDataSetChanged());
                } else if (jsonObject.getString(JsonUtil.Key.STATUS.toString()).equals("success_0")) {
                } else {
                    ServerConnectManager.Loge(jsonObject.getString("errorMessage"));
                }
            }
        });
    }

    public static class DataAdapter extends RecyclerView.Adapter<FriendsActivity.DataViewHolder> {
        public List<FriendsActivity.DataModel> dataModelList;
        public DataAdapter (List<FriendsActivity.DataModel> dataList) {
            this.dataModelList = dataList;
        }

        @NonNull
        @Override
        public FriendsActivity.DataViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_friend, parent, false);
            return new FriendsActivity.DataViewHolder(view, this);
        }

        @Override
        public void onBindViewHolder(@NonNull FriendsActivity.DataViewHolder holder, int position) {
            holder.setData(dataModelList.get(position));
        }

        @Override
        public int getItemCount() {
            return dataModelList.size();
        }
    }

    public static class DataViewHolder extends RecyclerView.ViewHolder {
        private FriendsActivity.DataAdapter dataAdapter;
        FriendsActivity.DataModel dataModel;
        TextView nameTextView;
        ImageView friendProfileImage;
        String userId;

        public DataViewHolder(@NonNull View itemView, FriendsActivity.DataAdapter dataAdapter) {
            super(itemView);
            this.dataAdapter = dataAdapter;
            nameTextView = itemView.findViewById(R.id.friendName);
            friendProfileImage = itemView.findViewById(R.id.friendProfileImage);
            itemView.findViewById(R.id.DMButton).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                }
            });
        }

        public void setData(FriendsActivity.DataModel dataModel){
            this.dataModel = dataModel;
            nameTextView.setText(dataModel.getName());
            userId = dataModel.getUserId();
        }
    }

    public class DataModel {
        private String userId;
        private String name;
        public DataModel(String userId,String name) {
            this.userId = userId;
            this.name = name;
        }

        // Getters
        public String getName() { return name; }
        public String getUserId() { return userId; }
    }
}
