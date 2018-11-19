package per.edward.wechatautomationutil;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ImageView;
import com.bumptech.glide.Glide;

import java.io.IOException;
import java.util.ArrayList;

import per.edward.wechatautomationutil.datainitialize.MomentItemBean;
import per.edward.wechatautomationutil.utils.Constant;
import per.edward.wechatautomationutil.utils.LogUtil;
import per.edward.wechatautomationutil.datainitialize.RequestUrlData;


/**
 * 注意事项
 * 1、Android设备必须安装微信app
 * 2、Android Sdk Version
 * <p>
 * Created by Edward on 2018-03-15.
 */
public class MainActivity extends AppCompatActivity {
    EditText edit, editIndex, editToken, editTag;
    TextView msgCount, msgResidue;
    private ArrayList<MomentItemBean> allMsg;
    private String[] imgUrls;
    private ArrayList<Integer> ids;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    private void initView() {
        edit = findViewById(R.id.edit);
        editIndex = findViewById(R.id.edit_index);
        editToken = findViewById(R.id.edit_token);
        editTag = findViewById(R.id.edit_tag);

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
                    try {
                        updateData();
                        Toast.makeText(getBaseContext(), "更新数据成功", Toast.LENGTH_SHORT).show();
                    } catch (IOException e) {
                        Toast.makeText(getBaseContext(), "更新数据失败", Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                    break;
            }
        }
    };

    public void updateData() throws IOException {
        SharedPreferences sharedPreferences = getSharedPreferences(Constant.WECHAT_STORAGE, Activity.MODE_MULTI_PROCESS);  //创建储存读取
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();  // 清空之前保存信息
        String token = editToken.getText().toString();
        String tag = editTag.getText().toString();
        String[] tagList = tag.split(",");

        editor.putString("token", token);
        editor.putString("tag", tag);
        editor.putString("index", "0");

        for (int i = 0; i < tagList.length; i++){
            String tagData = RequestUrlData.tagAllData(tagList[i], token);
            editor.putString(tagList[i], tagData);
        }
        if(editor.commit()){
            checkDataInit();
            return;
        }
    }

    @SuppressLint("SetTextI18n")
    public boolean checkDataInit(){
        SharedPreferences sharedPreferences = getSharedPreferences(Constant.WECHAT_STORAGE, Activity.MODE_MULTI_PROCESS);  //创建储存读取
        String token = sharedPreferences.getString("token", null);
        if(token == null){
            Toast.makeText(getBaseContext(), "数据为空请更新数据", Toast.LENGTH_SHORT).show();
            return false;
        }

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
        edit.setText(allMsg.get(0).getTilte());
        editIndex.setText(sharedPreferences.getString("index", "0"));

        // 统计数字到界面
        int msgLength = dataList.size();
        msgCount.setText("总共条数：" + String.valueOf(msgLength));
        msgResidue.setText("剩余: " + String.valueOf(msgLength));

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

        imgUrls = allMsg.get(0).getUrls();
        glideImgs(); //渲染图片到界面
        return true;
    }

    public void glideImgs(){
        for (int i = 0; i < imgUrls.length; i++){
            Glide.with(this).load(imgUrls[i]).into((ImageView) findViewById(ids.get(i)));
        }
    }


    private void pauseSendStatus(){
        SharedPreferences sharedPreferences = getSharedPreferences(Constant.WECHAT_STORAGE, Activity.MODE_MULTI_PROCESS);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(Constant.STATUS, true);
        if (editor.commit()){
            Toast.makeText(getBaseContext(), "暂停自动发送", Toast.LENGTH_LONG).show();
        }
    }

    private void saveData() {
        if (!checkDataInit()) {
            return;
        }


        SharedPreferences sharedPreferences = getSharedPreferences(Constant.WECHAT_STORAGE, Activity.MODE_MULTI_PROCESS);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(Constant.CONTENT, edit.getText().toString());
        editor.putBoolean(Constant.STATUS, false);
        if (editor.commit()) {
            Toast.makeText(getBaseContext(), "保存成功", Toast.LENGTH_LONG).show();
            AccessibilitySampleService.flag = false;
            if (AccessibilitySampleService.flag){
                LogUtil.e("flag = true");
            }else {
                LogUtil.e("flag = false");
            }

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
}
