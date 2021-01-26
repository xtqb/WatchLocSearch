package com.lhzw.watchloc;

import android.app.CPManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

/**
 * Created by heCunCun on 2021/1/18
 */
public class CpInfoActivity extends AppCompatActivity {
    private static final String TAG = "CpInfoActivity";
    private CPManager mCPManager;
    private TextView upTxt;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cp_info);
        upTxt =(TextView) findViewById(R.id.tv_info);
        Button btClear =(Button) findViewById(R.id.btn_clear);
        mCPManager = (CPManager)getSystemService("cpmanager");
        refresh();
        btClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCPManager.loraCountsReset();
                refresh();
            }
        });



    }

    private void refresh() {
        upTxt.setText("");
        byte[] re2 = mCPManager.getLoraPackCounts();
        Log.d(TAG,"get length = " + re2.length);

        if(re2 == null) {
            String packCount = new String("获取统计失败");
            upTxt.append(packCount);
            return;
        }

        for(int n = 3;n<re2.length;n++){
            Log.d(TAG, "re2[" + n + "] =" + re2[n]);
        }
        if(re2.length >= 42){
            int a = 0;
            //a += ((re2[3]) | ((re2[4])<< 8) | ((re2[5]) << 16) | ((re2[6]) <<24);
            a += (re2[3] & 0xFF);
            a += (re2[4] & 0xFF) << 8;
            a += (re2[5] & 0xFF) << 16;
            a += (re2[6] & 0xFF) << 24;

            int b = 0;
            b += (re2[7] & 0xFF);
            b += (re2[8] & 0xFF) << 8;
            b += (re2[9] & 0xFF) << 16;
            b += (re2[10] & 0xFF) << 24;

            int c = 0;
            c += (re2[11] & 0xFF);
            c += (re2[12] & 0xFF) << 8;
            c += (re2[13] & 0xFF) << 16;
            c += (re2[14] & 0xFF) << 24;

            int d = 0;
            d += (re2[15] & 0xFF);
            d += (re2[16] & 0xFF) << 8;
            d += (re2[17] & 0xFF) << 16;
            d += (re2[18] & 0xFF) << 24;

            int e1 = 0;
            e1 += (re2[19] & 0xFF);
            e1 += (re2[20] & 0xFF) << 8;
            e1 += (re2[21] & 0xFF) << 16;
            e1 += (re2[22] & 0xFF) << 24;

            int f = 0;
            f += (re2[23] & 0xFF);
            f += (re2[24] & 0xFF) << 8;
            f += (re2[25] & 0xFF) << 16;
            f += (re2[26] & 0xFF) << 24;

            int g = 0;
            g += (re2[27] & 0xFF);
            g += (re2[28] & 0xFF) << 8;
            g += (re2[29] & 0xFF) << 16;
            g += (re2[30] & 0xFF) << 24;

            int h = 0;
            h += (re2[31] & 0xFF);
            h += (re2[32] & 0xFF) << 8;
            h += (re2[33] & 0xFF) << 16;
            h += (re2[34] & 0xFF) << 24;

            int i = 0;
            i += (re2[35] & 0xFF);
            i += (re2[36] & 0xFF) << 8;
            i += (re2[37] & 0xFF) << 16;
            i += (re2[38] & 0xFF) << 24;

            int j = 0;
            j += (re2[39] & 0xFF);
            j += (re2[40] & 0xFF) << 8;
            j += (re2[41] & 0xFF) << 16;
            j += (re2[42] & 0xFF) << 24;

            int k = 0;
            k += (re2[43] & 0xFF);
            k += (re2[44] & 0xFF) << 8;
            k += (re2[45] & 0xFF) << 16;
            k += (re2[46] & 0xFF) << 24;
            Log.d(TAG, "get count = " + a + ","
                    + b + ","  + c + ","  + d + ","
                    + e1 + ","  + f + ","  + g + ","
                    + h + ","  + i + ","  + j + "," + k + "\n"
            );


            String packCount = String.valueOf(a);
            upTxt.append("recv_times: " + packCount + "\n");

            packCount = String.valueOf(b);
            upTxt.append("recv_packs: " +packCount + "\n");

            packCount = String.valueOf(c);
            upTxt.append("uart_send_times:" +packCount + "\n");


            packCount = String.valueOf(d);
            upTxt.append("uart_recv_times: " +packCount + "\n");

            packCount = String.valueOf(e1);
            upTxt.append("send_times: " +packCount + "\n");

            packCount = String.valueOf(f);
            upTxt.append("send_packs: " +packCount + "\n");


            packCount = String.valueOf(g);
            upTxt.append("sos_recv_packs: " +packCount + "\n");

            packCount = String.valueOf(h);
            upTxt.append("cmd_send_packs: " +packCount + "\n");

            packCount = String.valueOf(i);
            upTxt.append("sync_fail: " +packCount + "\n");

            packCount = String.valueOf(j);
            upTxt.append("sync_timeout: " +packCount + "\n");

            packCount = String.valueOf(k);
            upTxt.append("recv_timeout: " + packCount + "\n");
        }
    }
}
