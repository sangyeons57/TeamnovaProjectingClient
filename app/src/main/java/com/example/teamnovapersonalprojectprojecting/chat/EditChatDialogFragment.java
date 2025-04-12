package com.example.teamnovapersonalprojectprojecting.chat;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.teamnovapersonalprojectprojecting.R;
import com.example.teamnovapersonalprojectprojecting.socket.SocketConnection;
import com.example.teamnovapersonalprojectprojecting.socket.SocketEventListener;
import com.example.teamnovapersonalprojectprojecting.util.DataManager;
import com.example.teamnovapersonalprojectprojecting.util.JsonUtil;

public class EditChatDialogFragment extends DialogFragment {
    public interface OnEditChatDataButtonClickListener {
        void onEditChatDataButtonClick(int chatId);
    }

    public static final String CHAT_ID = "chatId";
    public static final String CHANNEL_ID = "channelId";
    public static final String WRITER_ID = "writerId";

    public static EditChatDialogFragment Instance(int channelId, int chatId, int writerId) {
        EditChatDialogFragment dialogFragment = new EditChatDialogFragment();
        Bundle args = new Bundle();
        args.putInt(CHANNEL_ID, channelId);
        args.putInt(CHAT_ID, chatId);
        args.putInt(WRITER_ID, writerId);
        dialogFragment.setArguments(args);
        return dialogFragment;
    }

    private Button editChatDataButton;
    private Button removeChatDataButton;

    private OnEditChatDataButtonClickListener onEditChatDataButtonClickListener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnEditChatDataButtonClickListener) {
            onEditChatDataButtonClickListener = (OnEditChatDataButtonClickListener) context;
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialogfragment_edit_chat_item, container, false);
        int channelId = getArguments().getInt(CHANNEL_ID);
        int chatId = getArguments().getInt(CHAT_ID);
        int writerId = getArguments().getInt(WRITER_ID);

        editChatDataButton = view.findViewById(R.id.editChatDataButton);
        removeChatDataButton = view.findViewById(R.id.removeChatDataButton);

        editChatDataButton.setOnClickListener((v)->{
            onEditChatDataButtonClickListener.onEditChatDataButtonClick(chatId);
            dismiss();
        });

        removeChatDataButton.setOnClickListener((v)->{
            SocketConnection.sendMessage(new JsonUtil()
                    .add(JsonUtil.Key.TYPE, SocketEventListener.eType.REMOVE_CHAT_DATA)
                    .add(JsonUtil.Key.CHANNEL_ID, channelId)
                    .add(JsonUtil.Key.CHAT_ID, chatId));
            dismiss();
        });

        if(DataManager.Instance().userId != writerId) {
            editChatDataButton.setVisibility(View.GONE);
            removeChatDataButton.setVisibility(View.GONE);
        } else {
            editChatDataButton.setVisibility(View.VISIBLE);
            removeChatDataButton.setVisibility(View.VISIBLE);
        }

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            getDialog().getWindow().setGravity(Gravity.CENTER);
        }
    }


}
