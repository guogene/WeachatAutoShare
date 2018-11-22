package per.edward.wechatautomationutil;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ImageView;
import com.bumptech.glide.Glide;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;

import per.edward.wechatautomationutil.datainitialize.DownloadImg;
import per.edward.wechatautomationutil.datainitialize.MomentItemBean;
import per.edward.wechatautomationutil.utils.Constant;
import per.edward.wechatautomationutil.datainitialize.RequestUrlData;


/**
 * 注意事项
 * 1、Android设备必须安装微信app
 * 2、Android Sdk Version
 * <p>
 * Created by Edward on 2018-11-20.
 */
public class MainActivity extends AppCompatActivity {
    EditText edit, editIndex, editToken, editTag;
    TextView msgCount, msgResidue;
    static ProgressBar progressBar;

    private ArrayList<MomentItemBean> allMsg;
    private String[] imgUrls;
    private ArrayList<Integer> ids;
    private ProgressHandler pBarHandler;
    private UpdateViewHandler updateHanderl;
    private Thread updateDataThread;
    private String tag = "test";
    private Boolean theardStatus = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        pBarHandler = new ProgressHandler(this);
        updateHanderl = new UpdateViewHandler(this);
        updateDataThread = new Thread(new sendControllerThread());
        updateDataThread.start();
        initView();
    }

    private void initView() {
        edit = findViewById(R.id.edit);
        editIndex = findViewById(R.id.edit_index);
        editToken = findViewById(R.id.edit_token);
        editTag = findViewById(R.id.edit_tag);
        msgCount = findViewById(R.id.msg_count);
        msgResidue = findViewById(R.id.msg_residue);
        progressBar = findViewById(R.id.progressBar);

        findViewById(R.id.open_accessibility_setting).setOnClickListener(clickListener);
        findViewById(R.id.btn_send).setOnClickListener(clickListener);
        findViewById(R.id.btn_paues).setOnClickListener(clickListener);
        findViewById(R.id.btn_reset).setOnClickListener(clickListener);
        findViewById(R.id.btn_update).setOnClickListener(clickListener);
        checkDataInit();
    }

    View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.open_accessibility_setting:
                    OpenAccessibilitySettingHelper.jumpToSettingPage(getBaseContext());
                    break;
                case R.id.btn_send:
                    saveData();
                    break;
                case R.id.btn_paues:
                    pauseSendStatus();
                    break;
                case R.id.btn_update:
                    progressBar.setVisibility(View.VISIBLE);  //显示加载框
                    new Thread(new updateDataThread()).start();  //开启更新数据请求线程
                    break;
            }
        }
    };

    private class updateDataThread implements Runnable{
        @Override
        public void run() {
            try {
                updateData();
                pBarHandler.sendEmptyMessage(0);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private class sendControllerThread implements Runnable{
        @Override
        public void run() {
                while (true){
                    synchronized (this){
                        try {
                            this.wait(3000);
                            SharedPreferences sharedPreferences = getSharedPreferences(Constant.WECHAT_STORAGE, Activity.MODE_MULTI_PROCESS);  //创建储存读取
                            int sent = sharedPreferences.getInt(Constant.SENT, 0);
                            int sending = sharedPreferences.getInt(Constant.SENDING, 1);

                            if (sent == sending && theardStatus){
                                this.wait(10000);
                                updateHanderl.sendEmptyMessage(0);
                            }
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                }

        }
    }

    /**
     * 更新数据
     * @throws IOException
     */
    public void updateData() throws IOException {
        SharedPreferences sharedPreferences = getSharedPreferences(Constant.WECHAT_STORAGE, Activity.MODE_MULTI_PROCESS);  //创建储存读取
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();  // 清空之前保存信息
        String token = editToken.getText().toString();
        String tag = editTag.getText().toString();
        String[] tagList = tag.split(",");

        editor.putString("token", token);
        editor.putString("tag", tag);
        editor.putInt(Constant.INDEX, 0);
        editor.putInt(Constant.SENDING, 1);
        editor.putInt(Constant.SENT, 0);

        for (int i = 0; i < tagList.length; i++){
            String tagData = RequestUrlData.tagAllData(tagList[i], token);
            editor.putString(tagList[i], tagData);
        }
        editor.apply();
    }

    /**
     * 更新当前图文配置 并刷新界面
     */
    public void checkDataInit(){
        SharedPreferences sharedPreferences = getSharedPreferences(Constant.WECHAT_STORAGE, Activity.MODE_MULTI_PROCESS);  //创建储存读取
        String token = sharedPreferences.getString("token", null);
        if(token == null){

            Toast.makeText(getBaseContext(), "数据为空请更新数据", Toast.LENGTH_SHORT).show();
        }else {

            String[] tagList = sharedPreferences.getString("tag", "").split(",");
            // 把所有数据解析成 对象保存起来
            ArrayList<MomentItemBean> dataList = new ArrayList<>();
            for (int i = 0; i< tagList.length; i++){
                String tagStringData = sharedPreferences.getString(tagList[i], "");
                ArrayList<MomentItemBean> tagListObject = RequestUrlData.dataStringToJsonObject(tagStringData);
                dataList.addAll(tagListObject);
            }
            allMsg = dataList;
            // 给界面赋值
            int sent = sharedPreferences.getInt(Constant.SENT, 0);
            edit.setText(allMsg.get(sent).getTilte());
            editIndex.setText(String.valueOf(sharedPreferences.getInt(Constant.SENDING, 1)));

            // 统计数字到界面
            int msgLength = dataList.size();
            String updateMsgCount = "总共条数: " + String.valueOf(msgLength);
            String updateMsgResidue = "剩余: " + String.valueOf(msgLength - sent);
            msgCount.setText(updateMsgCount);
            msgResidue.setText(updateMsgResidue);

            //添加图片控件进数组
            ids = new ArrayList<>();
            ids.add(R.id.img1);
            ids.add(R.id.img2);
            ids.add(R.id.img3);
            ids.add(R.id.img4);
            ids.add(R.id.img5);
            ids.add(R.id.img6);
            ids.add(R.id.img7);
            ids.add(R.id.img8);
            ids.add(R.id.img9);

            imgUrls = allMsg.get(sent).getUrls();
            glideImgs(); //渲染图片到界面
        }

    }

    /**
     * 渲染图片
     */
    public void glideImgs(){
        for (int i = 0; i < imgUrls.length; i++){
            Glide.with(this).load(imgUrls[i]).into((ImageView) findViewById(ids.get(i)));
        }
    }

    /**
     * 暂停发送
     */
    private void pauseSendStatus() {
        SharedPreferences sharedPreferences = getSharedPreferences(Constant.WECHAT_STORAGE, Activity.MODE_MULTI_PROCESS);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(Constant.STATUS, true);
        if (editor.commit()){
            DownloadImg.deleteFile(this);
            theardStatus = false;  //控制总体线程状态
            Toast.makeText(getBaseContext(), "暂停自动发送", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * 对批量发送的上下条控制 图文的参数
     */
    private void ImgInitController(){
        SharedPreferences sharedPreferences = getSharedPreferences(Constant.WECHAT_STORAGE, Activity.MODE_MULTI_PROCESS);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        int sent = sharedPreferences.getInt(Constant.SENT, 0);
        int sending = sharedPreferences.getInt(Constant.SENDING, 0);
        checkDataInit(); // 更新界面
        // 判断是否第一次发送
        if (sent == 0){
            String title = allMsg.get(0).getTilte();
            editor.putString(Constant.CONTENT, title).apply();
            ArrayList<String> urls = new ArrayList<String>(Arrays.asList(allMsg.get(0).getUrls()));
            DownloadImg.permissonDownload(this, urls);
            Log.v(tag, "url的长度" + urls.size());
            editor.putInt(Constant.COUNT, urls.size());
        }else if (sent == allMsg.size() - 1){  // 判断最后一条发送完毕
            DownloadImg.deleteFile(this);
        } else {    //判断发送到中间段,先删除上一图文,在加载这次图文
            DownloadImg.deleteFile(this);
            // 赋值图文参数
            String title = allMsg.get(sent).getTilte();
            ArrayList<String> urls = new ArrayList<String>(Arrays.asList(allMsg.get(sent).getUrls()));
            //更新界面
            String updateMsgResidue = "剩余: " + String.valueOf(allMsg.size() - sending);
            String updateSending = sending + "";
            edit.setText(title);
            editIndex.setText(updateSending);
            msgResidue.setText(updateMsgResidue);

            //下载图片，赋值参数
            DownloadImg.permissonDownload(this, urls);
            editor.putInt(Constant.COUNT, urls.size());
            editor.putString(Constant.CONTENT, title);
            editor.apply();
        }
    }

    private void saveData() {
        SharedPreferences sharedPreferences = getSharedPreferences(Constant.WECHAT_STORAGE, Activity.MODE_MULTI_PROCESS);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        String indexText = editIndex.getText().toString();
        int sendIndex;
        if (indexText.equals("")){
            sendIndex = 1;
        }else {
            sendIndex = Integer.valueOf(indexText);
        }
        if (sendIndex < 1){
            sendIndex = 1;
        }else if (sendIndex > allMsg.size()){
            Toast.makeText(getBaseContext(), "超出总条数范围,请重新填写", Toast.LENGTH_LONG).show();
            return;
        }
        editor.putBoolean(Constant.STATUS, false);
        editor.putInt(Constant.SENDING, sendIndex);
        editor.putInt(Constant.SENT, sendIndex - 1);

        if (editor.commit()) {
            theardStatus = true;  //控制总体线程状态
            ImgInitController();
            openWeChatApplication();//打开微信应用
        } else {
            Toast.makeText(getBaseContext(), "保存失败", Toast.LENGTH_LONG).show();
        }
    }

    private void openWeChatApplication() {
        PackageManager packageManager = getBaseContext().getPackageManager();
        Intent it = packageManager.getLaunchIntentForPackage("com.tencent.mm");
        startActivity(it);
    }

    @Override
    protected void onDestroy() {
        // Remove all Runnable and Message.
        pBarHandler.removeCallbacksAndMessages(null);
        updateHanderl.removeCallbacksAndMessages(null);
        updateDataThread.interrupt();
        super.onDestroy();
    }

    static class ProgressHandler extends Handler {
        // WeakReference to the outer class's instance.
        private WeakReference<MainActivity> mOuter;

        public ProgressHandler(MainActivity activity) {
            mOuter = new WeakReference<MainActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            MainActivity outer = mOuter.get();
            if (outer != null) {
                // Do something with outer as your wish.
                progressBar.setVisibility(View.GONE);
            }
        }
    }

    @SuppressLint("HandlerLeak")
    class UpdateViewHandler extends Handler {
        // WeakReference to the outer class's instance.
        private WeakReference<MainActivity> mOuter;

        public UpdateViewHandler(MainActivity activity) {
            mOuter = new WeakReference<MainActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            MainActivity outer = mOuter.get();
            if (outer != null) {
                // Do something with outer as your wish.
                SharedPreferences sharedPreferences = getSharedPreferences(Constant.WECHAT_STORAGE, Activity.MODE_MULTI_PROCESS);
                String newIndex = sharedPreferences.getInt(Constant.SENDING, 1) + 1 + "";
                editIndex.setText(newIndex);
                saveData();
            }
        }
    }

}
