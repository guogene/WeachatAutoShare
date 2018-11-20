package per.edward.wechatautomationutil.datainitialize;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;

import com.yanzhenjie.permission.Action;
import com.yanzhenjie.permission.AndPermission;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.FileCallBack;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;

public class DownloadImg {
    private static int needDownloadSize = 0;
    private static int downloadedSize = 0;
    private static String PIC_NAME = "tag";

    private static ArrayList<Uri> uriList;
    private static ProgressDialog progressDialog;

    public static ArrayList<Uri> permissonDownload(final Context context, final ArrayList<String> imgUrls){
        initImgUrl(imgUrls);
        AndPermission.with(context)
                .runtime()
                .permission(Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE)
                .onGranted(new Action<List<String>>() {
                    @Override
                    public void onAction(List<String> data) {
                        startDownImg(imgUrls, context);
                    }
                })
                .start();
        return uriList;
    }

    private static void startDownImg(ArrayList<String> imgUrls, Context context) {
        needDownloadSize = imgUrls.size();
        downloadedSize = 0;
        // 开始下载图片

        progressDialog = new ProgressDialog(context);
        progressDialog.setTitle("正在下载图片....");
        progressDialog.setIndeterminate(true);
        progressDialog.show();
        for (int i = 0; i < needDownloadSize; i++) {
            if (!TextUtils.isEmpty(imgUrls.get(i))){
                okHttpSaveImg(imgUrls.get(i), i, context);
            }else {
                needDownloadSize --;
            }
        }


    }

    private static void okHttpSaveImg(String imgUrl, final int index, final Context context) {
        OkHttpUtils.get()
                .url(imgUrl)
                .build()
                .execute(new FileCallBack(context.getFilesDir().getAbsolutePath(), index+"img.jpg") {
                    @Override
                    public void onError(Call call, Exception e, int id) {
                        needDownloadSize --;
                        checkDownload(context);
                    }

                    @Override
                    public void onResponse(File response, int id) {
                        if (response != null){
                            Uri mUri;
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
                                mUri = Uri.parse(insertImageToSystem(context, response.getAbsolutePath()));
                            }else {
                                mUri = Uri.parse(insertImageToSystem(context, response.getAbsolutePath()));
                            }
                            try {
                                uriList.set(index, mUri);
                                downloadedSize++;
                                checkDownload(context);
                            }catch (Exception ignored){}
                        }
                    }
                });
    }

    private static String insertImageToSystem(Context context, String imagePath) {
        String url = "";
        try {
            if (context != null){
                url = MediaStore.Images.Media.insertImage(context.getContentResolver(), imagePath, PIC_NAME, "描述");
            }
        }catch (FileNotFoundException e){
            e.printStackTrace();
        }
        return url;
    }

    private static void initImgUrl(ArrayList<String> imgUrls) {
        uriList = new ArrayList<Uri>();
        for (String imgUrl : imgUrls){
            uriList.add(null);
        }
        needDownloadSize = 0;
        downloadedSize = 0;
    }

    public static boolean deleteFile() throws URISyntaxException {
        for (int i = 0; i < uriList.size(); i++) {
            URI fileName = new URI(uriList.get(i).toString());
            File file = new File(fileName);
            // 如果文件路径所对应的文件存在，并且是一个文件，则直接删除
            if (file.exists() && file.isFile()) {
                if (file.delete()) {
                }
            }
        }
        return true;
    };

    private static void checkDownload(Context context) {
        if (downloadedSize >= needDownloadSize){
            progressDialog.dismiss();
        }
    }
}
