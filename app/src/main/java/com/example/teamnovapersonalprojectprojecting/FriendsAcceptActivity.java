package com.example.teamnovapersonalprojectprojecting;

import android.content.Intent;
import android.database.sqlite.SQLiteDatatypeMismatchException;
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
                        watingList.add(new FriendsAcceptActivity.DataModel(waiting.getString(JsonUtil.Key.ID.toString()), waiting.getString(JsonUtil.Key.USERNAME.toString())));
                    }
                    runOnUiThread(() -> recyclerView.getAdapter().notifyDataSetChanged());
                } else if (jsonObject.getString(JsonUtil.Key.STATUS.toString()).equals("success_0")){
                } else {
                    ServerConnectManager.Loge(jsonObject.getString("errorMessage"));
                }
            }
        });

        WebSocketEcho.Instance().addEventListener(WebsocketManager.Type.REMOVE_WAITING_DATA, (websocketManager)->{
            for (DataModel dataModel: watingList) {
                if(dataModel.getUserId().equals(websocketManager.getJsonUtil().getString(JsonUtil.Key.USER_ID, ""))){
                    watingList.remove(dataModel);
                    runOnUiThread(() -> recyclerView.getAdapter().notifyDataSetChanged());
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
            JsonUtil data = websocketManager.getJsonUtil();
            watingList.add(new DataModel(data.getString(JsonUtil.Key.USER_ID, ""),data.getString(JsonUtil.Key.USERNAME, "AddWaiting 문제 발생")));
            runOnUiThread(() -> recyclerView.getAdapter().notifyDataSetChanged());
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
            return new DataViewHolder(view, this);
        }

        @Override
        public void onBindViewHolder(@NonNull DataViewHolder holder, int position) {
            holder.setData(dataModelList.get(position));
        }

        @Override
        public int getItemCount() {
            return dataModelList.size();
        }
    }

    public static class DataViewHolder extends RecyclerView.ViewHolder {
        private DataAdapter dataAdapter;
        DataModel dataModel;
        TextView nameTextView;
        ImageView friendProfileImage;
        String userId;

        public DataViewHolder(@NonNull View itemView, DataAdapter dataAdapter) {
            super(itemView);
            this.dataAdapter = dataAdapter;
            nameTextView = itemView.findViewById(R.id.friendName);
            friendProfileImage = itemView.findViewById(R.id.friendProfileImage);
            itemView.findViewById(R.id.acceptFriendButton).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    WebsocketManager.Log(DataManager.Instance().userId + " " + userId);
                    WebsocketManager.Generate(WebSocketEcho.Instance().getWebsocket()).setJsonUtil(new JsonUtil()
                            .add(JsonUtil.Key.USER_ID, DataManager.Instance().userId)
                            .add(JsonUtil.Key.USER_ID1, userId))
                            .Send(WebsocketManager.Type.ADD_FRIEND_ON_WAITING);
                    dataAdapter.dataModelList.remove(dataModel);
                    dataAdapter.notifyDataSetChanged();
                }
            });
        }

        public void setData(DataModel dataModel){
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
