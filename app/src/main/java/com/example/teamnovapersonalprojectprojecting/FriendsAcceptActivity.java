package com.example.teamnovapersonalprojectprojecting;

import android.content.Intent;
import android.os.Bundle;
import android.service.autofill.UserData;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

public class FriendsAcceptActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private List<DataModel> watingList;
    private ImageButton backButton;

    private WebSocketEcho.OnCallListener onCallListener;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accept_friends);
        this.recyclerView = findViewById(R.id.recyclerview);
        this.backButton = findViewById(R.id.backButton);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        watingList = new ArrayList<>();
        recyclerView.setAdapter(new DataAdapter(watingList));

        this.backButton.setOnClickListener(v -> {
            finish();
        });

        ServerConnectManager serverConnectManager = new ServerConnectManager(ServerConnectManager.Path.FRIENDS.getPath("getWaitingList.php"))
                .add("userId", DataManager.Instance().userId);

        serverConnectManager.postEnqueue(new ServerConnectManager.EasyCallback() {
            @Override
            protected void onGetJson(JSONObject jsonObject) throws IOException, JSONException {
                super.onGetJson(jsonObject);
                if(jsonObject.getString(JsonUtil.Key.STATUS.toString()).equals("success")) {
                    JSONArray jsonArray;
                    try {
                        jsonArray = jsonObject.getJSONArray("data");
                    } catch (JSONException e) {
                        jsonArray = new JSONArray();
                    }
                    Log.d("FriendsAcceptActivity", jsonArray.toString());

                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject waiting = new JSONObject(jsonArray.getString(i));
                        watingList.add(new DataModel(waiting.getString("username")));
                    }
                    runOnUiThread(() -> recyclerView.getAdapter().notifyDataSetChanged());
                } else if (jsonObject.getString(JsonUtil.Key.STATUS.toString()).equals("success_0")){
                } else {
                    ServerConnectManager.Loge(jsonObject.getString("errorMessage"));
                }
            }
        });


        /*
        TODO: 이부분을 볼때 업데이트 되는 데이터는 요청이 발생하지 않아도
         업데이트 되어야 한다.
         따라서 WebsocketEcho부분에 이벤트를 받아서 데이터를 업데이트 되도록 해야한다.
         따라서 이벤트 할당 기능을 만들어야한다.
         */
        onCallListener = (websocketManager)->{
            ServerConnectManager getUserData = new ServerConnectManager(ServerConnectManager.Path.USERS.getPath("getUserData.php"))
                    .add(JsonUtil.Key.USER_ID, websocketManager.getJsonUtil().getString(JsonUtil.Key.USER_ID, ""));
            getUserData.postEnqueue(new ServerConnectManager.EasyCallback(){
                @Override
                protected void onGetJson(JSONObject jsonObject) throws IOException, JSONException {
                    super.onGetJson(jsonObject);
                    if(jsonObject.getString(JsonUtil.Key.STATUS.toString()).equals("success")){
                        JSONObject data = jsonObject.getJSONObject(JsonUtil.Key.DATA.toString());
                        watingList.add(new DataModel(data.getString(JsonUtil.Key.USERNAME.toString())));
                        runOnUiThread(()->{ recyclerView.getAdapter().notifyDataSetChanged(); });
                    } else {
                        ServerConnectManager.Loge(jsonObject.getString(JsonUtil.Key.MESSAGE.toString()));
                    }
                }
            });
        };
        WebSocketEcho.Instance().addEventListener(WebsocketManager.Type.ADD_WAITING, onCallListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        WebSocketEcho.Instance().removeEventListener(WebsocketManager.Type.ADD_WAITING, onCallListener);
    }

    public static class DataAdapter extends RecyclerView.Adapter<DataViewHolder> {
        public List<DataModel> dataModelList;
        public DataAdapter (List<DataModel> dataList) {
            this.dataModelList = dataList;
        }

        @NonNull
        @Override
        public DataViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_accept_friend, parent, false);
            return new DataViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull DataViewHolder holder, int position) {
            DataModel dataModel = dataModelList.get(position);
            holder.nameTextView.setText(dataModel.getName());
        }

        @Override
        public int getItemCount() {
            return dataModelList.size();
        }
    }

    public static class DataViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView;
        ImageView friendProfileImage;

        public DataViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.friendName);
            friendProfileImage = itemView.findViewById(R.id.friendProfileImage);
        }
    }

    public class DataModel {
        private String name;
        public DataModel(String name) {
            this.name = name;
        }

        // Getters
        public String getName() { return name; }
    }
}
