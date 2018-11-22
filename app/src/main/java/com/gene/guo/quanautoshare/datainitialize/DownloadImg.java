package com.gene.guo.quanautoshare.datainitialize;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.yanzhenjie.permission.Action;
import com.yanzhenjie.permission.AndPermission;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.FileCallBack;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import com.gene.guo.quanautoshare.utils.Constant;

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
                            Uri mUri = null;
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
                                try {
                                    mUri = insertSystemGallery(context, response.getAbsolutePath(), String.valueOf(index));
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }else {
                                try {
                                    mUri = insertSystemGallery(context, response.getAbsolutePath(), String.valueOf(index));
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
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

    private static Uri insertSystemGallery(Context context, String imagePath, String picName) throws IOException {
        //生成BITMAP
        FileInputStream fis = new FileInputStream(imagePath);
        Bitmap bitmap  = BitmapFactory.decodeStream(fis);

        String fileName = null;
        String galleryPath= Environment.getExternalStorageDirectory()
                + File.separator + Environment.DIRECTORY_DCIM
                + File.separator +"shoes";
        File fileDir = new File(galleryPath);
        if (!fileDir.exists()){
            fileDir.mkdirs();
        }
        File file = null;
        // 声明输出流
        FileOutputStream outStream = null;
        file = new File(galleryPath + File.separator, picName+ ".jpg");
        // 获得文件相对路径
        fileName = file.toString();
        // 获得输出流，如果文件中有内容，追加内容
        outStream = new FileOutputStream(fileName);
        if (null != outStream) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outStream);
        }
        if (outStream != null) {
            outStream.close();
        }
        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri uri = Uri.fromFile(file);
        intent.setData(uri);
        context.sendBroadcast(intent);

        return uri;
    }

    private static void initImgUrl(ArrayList<String> imgUrls) {
        uriList = new ArrayList<Uri>();
        for (String imgUrl : imgUrls){
            uriList.add(null);
        }
        needDownloadSize = 0;
        downloadedSize = 0;
    }

    /** 删除单个文件
     * @param filePath$Name 要删除的文件的文件名
     * @return 单个文件删除成功返回true，否则返回false
     */
    private static boolean deleteSingleFile(Context context, String filePath$Name) {
        File file = new File(filePath$Name);
        // 如果文件路径所对应的文件存在，并且是一个文件，则直接删除
        if (file.exists() && file.isFile()) {
            if (file.delete()) {
                Log.e("--Method--", "Copy_Delete.deleteSingleFile: 删除单个文件" + filePath$Name + "成功！");
                return true;
            } else {
                Toast.makeText(context, "删除单个文件" + filePath$Name + "失败！", Toast.LENGTH_SHORT).show();
                return false;
            }
        } else {
            Toast.makeText(context, "删除单个文件失败：" + filePath$Name + "不存在！", Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    public static void deleteFile(Context context){
        String filePath = Environment.getExternalStorageDirectory()
                + File.separator + Environment.DIRECTORY_DCIM
                + File.separator + "shoes" + File.separator;
        File dirFile = new File(filePath);

        boolean flag = true;
        // 删除文件夹中的所有文件包括子目录
        File[] files = dirFile.listFiles();
        for (File file : files) {
            // 删除子文件
            if (file.isFile()) {
                flag = deleteSingleFile(context, file.getAbsolutePath());
                if (!flag)
                    break;
            }
        }
        if (!flag) {
            Toast.makeText(context, "删除目录失败！", Toast.LENGTH_SHORT).show();

        }
    };


    private static void checkDownload(Context context) {
        if (downloadedSize >= needDownloadSize){
            progressDialog.dismiss();
            StringBuilder imgUrlPath = new StringBuilder();
            for (int i = 0; i < uriList.size(); i++) {
                imgUrlPath.append(",").append(uriList.get(i).toString());
            }
            String imgPathStr = imgUrlPath.toString();
            SharedPreferences sharedPreferences = context.getSharedPreferences(Constant.WECHAT_STORAGE, Activity.MODE_MULTI_PROCESS);  //调用存储,保存当前图片地址
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(Constant.LASTEST_IMG_PATH, imgPathStr);
            editor.apply();

        }
    }
}
