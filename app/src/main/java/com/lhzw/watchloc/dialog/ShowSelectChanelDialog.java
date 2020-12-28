package com.lhzw.watchloc.dialog;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.lhzw.watchloc.R;


public class ShowSelectChanelDialog extends AlertDialog implements AdapterView.OnItemClickListener {
    private Button bt_back;
    private ListView listview;
    private onChannelItemClickListener onItemListner;

    private ShowSelectChanelDialog(Context context, int theme) {
        super(context, theme);
        // TODO Auto-generated constructor stub
    }

    public ShowSelectChanelDialog(Context context) {
        super(context);
        // TODO Auto-generated constructor stub
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_select_channel);
        init();
    }

    private void init() {
        setCancelable(false);
        getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        bt_back = (Button) findViewById(R.id.bt_back);
        listview = (ListView) findViewById(R.id.listview);
    }

    public void setAdapter(BaseAdapter adapter, View.OnClickListener listener) {
        listview.setAdapter(adapter);
        listview.setOnItemClickListener(this);
        bt_back.setOnClickListener(listener);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if(onItemListner != null) {
            onItemListner.onChannelClick(position);
        }
        Log.e("Tag", position + "   " + id);
        this.dismiss();
    }

    public void setOnSelecteChannelClickListener(onChannelItemClickListener onItemListner){
        this.onItemListner = onItemListner;
    }

    public interface onChannelItemClickListener{
        public void onChannelClick(int pos);
    }

    public void setConten(String content) {
        bt_back.setText(content);
    }
}
