package com.lhzw.watchloc.dialog;

import android.app.AlertDialog;
import android.content.Context;
import android.content.LoRaManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

import com.lhzw.watchloc.R;

import java.io.UnsupportedEncodingException;

public class SendMessageDialog extends AlertDialog implements View.OnClickListener, TextWatcher {
    private TextView et_content;
    private TextView tv_cancel;
    private TextView tv_send;
    private LoRaManager loRaManager;
    private byte[] mask;
    private Context mContext;
    private String beforeContent;

    private SendMessageDialog(Context context, int theme) {
        super(context, theme);
        // TODO Auto-generated constructor stub

    }

    public SendMessageDialog(Context context, LoRaManager loRaManager, byte[] mask) {
        super(context);
        mContext = context;
        this.loRaManager = loRaManager;
        this.mask = mask;
        // TODO Auto-generated constructor stub
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_send_message);
        init();
    }

    public void showDialog() {
        show();
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
    }

    private void cancelDialog() {
        cancel();
    }

    private void init() {
        setCancelable(false);
        et_content = (TextView) findViewById(R.id.et_content);
        tv_cancel = (TextView) findViewById(R.id.tv_cancel);
        tv_send = (TextView) findViewById(R.id.tv_send);
    }

    public void setListener() {
        tv_cancel.setOnClickListener(this);
        tv_send.setOnClickListener(this);
        et_content.addTextChangedListener(this);
    }

    private void sendContent() {
        try {
            String content = et_content.getText().toString();
            if (content.isEmpty()) {
                Toast.makeText(mContext, "发送内容不能为空", Toast.LENGTH_SHORT).show();
                return;
            }
            if (loRaManager != null && mask != null) {
                loRaManager.sendMessage(mask, content.getBytes("GB2312"));
                Toast.makeText(mContext, "发送内容成功", Toast.LENGTH_SHORT).show();
            }
            cancelDialog();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    private void clear() {
        et_content = null;
        tv_cancel = null;
        tv_send = null;
        loRaManager = null;
        mask = null;
        mContext = null;
        beforeContent = null;
    }

    @Override
    public void onClick(View v) {
        InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm.isActive()) {
            imm.hideSoftInputFromWindow(et_content.getApplicationWindowToken(), 0);
        }
        switch (v.getId()) {
            case R.id.tv_cancel:
                cancelDialog();
                break;
            case R.id.tv_send:
                sendContent();
                break;
        }
        clear();
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        beforeContent = s.toString();
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        if (!s.toString().isEmpty()) {
            try {
                int len = s.toString().getBytes("GB2312").length;
                if (len > 40) {
                    et_content.setText(beforeContent);
                    Toast.makeText(mContext, "发送长度不能超过20字符", Toast.LENGTH_SHORT).show();
                }
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
    }
}
