package per.edward.wechatautomationutil;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.ImageView;
import com.bumptech.glide.Glide;
import java.util.ArrayList;

import per.edward.wechatautomationutil.utils.Constant;
import per.edward.wechatautomationutil.AccessibilitySampleService;
import per.edward.wechatautomationutil.utils.LogUtil;

/**
 * 注意事项
 * 1、Android设备必须安装微信app
 * 2、Android Sdk Version
 * <p>
 * Created by Edward on 2018-03-15.
 */
public class MainActivity extends AppCompatActivity {
    EditText edit, editIndex, editCount;
    private ArrayList<String> urls;
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
        editCount = findViewById(R.id.edit_count);

        findViewById(R.id.open_accessibility_setting).setOnClickListener(clickListener);
        findViewById(R.id.btn_save).setOnClickListener(clickListener);
        initImgs();
    }

    /**
     *  渲染当前朋友圈的图文
     */
    private void initImgs(){

        edit.setText("【VALENTINO】p400\uD83D\uDCB0华伦天奴  顶级版本。17ssRockstud经典裸靴铆钉系列 ，明星同款。面料2种：黑胎牛黑荔枝纹。内里：印度羊皮，意大利特殊改色真皮底、35-40跟高3种:2公分，6公分，9公分。7033-0401，7032-0601，7041-0401");

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

        urls = new ArrayList<>();
        urls.add("https://xcimg.szwego.com/1535773806_2579627491_0?imageView2/2/format/jpg");
        urls.add("https://xcimg.szwego.com/1535773807_958498888_1?imageView2/2/format/jpg");
        urls.add("https://xcimg.szwego.com/1535773807_3749844693_2?imageView2/2/format/jpg");
        urls.add("https://xcimg.szwego.com/1535773807_1926767108_3?imageView2/2/format/jpg");
        urls.add("https://xcimg.szwego.com/1535773807_1084069846_4?imageView2/2/format/jpg");
        urls.add("https://xcimg.szwego.com/1535773807_2656625861_5?imageView2/2/format/jpg");
        urls.add("https://xcimg.szwego.com/1535773807_1002556768_6?imageView2/2/format/jpg");
        urls.add("https://xcimg.szwego.com/1535773807_120051631_7?imageView2/2/format/jpg");

        for (int i = 0; i < urls.size(); i++){
            Glide.with(this).load(urls.get(i)).into((ImageView) findViewById(ids.get(i)));
        }

    }

    View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.open_accessibility_setting:
                    OpenAccessibilitySettingHelper.jumpToSettingPage(getBaseContext());
                    break;
                case R.id.btn_save:
                    saveData();
                    break;
            }
        }
    };

    public boolean checkParams() {
        if (TextUtils.isEmpty(editIndex.getText().toString())) {
            Toast.makeText(getBaseContext(), "起始下标不能为空", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (TextUtils.isEmpty(editCount.getText().toString())) {
            Toast.makeText(getBaseContext(), "图片总数不能为空", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (Integer.valueOf(editCount.getText().toString()) > 9) {
            Toast.makeText(getBaseContext(), "图片总数不能超过9张", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void saveData() {
        if (!checkParams()) {
            return;
        }

        int index = Integer.valueOf(editIndex.getText().toString());
        int count = Integer.valueOf(editCount.getText().toString());

        SharedPreferences sharedPreferences = getSharedPreferences(Constant.WECHAT_STORAGE, Activity.MODE_MULTI_PROCESS);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(Constant.CONTENT, edit.getText().toString());
        editor.putInt(Constant.INDEX, index);
        editor.putInt(Constant.COUNT, count);
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
