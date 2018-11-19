package per.edward.wechatautomationutil.datainitialize;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

public class RequestUrlData {
    private static String getUrlData(String tag, String pageIndex, String token) throws IOException {
        String link = "https://www.szwego.com/service/album/get_album_themes_list.jsp?act=single_album&shop_id=A2018010512564930339&tag=[" + tag + "]&page_index=" + pageIndex;
        URL url = new URL(link);

        HttpURLConnection connection = (HttpURLConnection)url.openConnection();
        connection.setRequestProperty("cookie", "token=" + token);
        connection.setRequestMethod("GET");
        connection.connect();
        BufferedReader bReader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
        String line = null;
        StringBuilder stringBuilder = new StringBuilder();
        while ((line = bReader.readLine()) != null){
            stringBuilder.append(line); // 接取返回结果字符串
        }
        bReader.close();
        connection.disconnect();
        return stringBuilder.toString();
    }

    public static String tagAllData(String tag, String token) throws IOException {

        String dataString = "";
        int pageIndex = 1;
        while (true){
            String pageData = getUrlData(tag, String.valueOf(pageIndex), token);
            JSONObject data = JSONObject.parseObject(pageData); //拿到 结果 JSON
            JSONObject result = JSONObject.parseObject(data.getString("result")); //将result转换JSON对象
            JSONArray goodsList = JSONObject.parseArray(result.getString("goods_list"));
            // 判断该请求的返回结果是否为空
            if (goodsList.size() == 0){
                break;
            }else {
                pageIndex += 1;
            }
            dataString += '&' + pageData;
        }

        return dataString.substring(1);
    }

    public static ArrayList<MomentItemBean> dataStringToJsonObject(String info){
        String[] infoList = info.split("&");

        ArrayList<MomentItemBean> allDataList = new ArrayList<MomentItemBean>();  //存放所有图文集合

        for (int k =0; k< infoList.length; k++){
            String pageData = infoList[k];
            JSONObject data = JSONObject.parseObject(pageData); //拿到 结果 JSON
            JSONObject result = JSONObject.parseObject(data.getString("result")); //将result转换JSON对象
            JSONArray goodsList = JSONObject.parseArray(result.getString("goods_list"));
            for (int i =0; i< goodsList.size(); i++) {
                JSONObject goods = goodsList.getJSONObject(i);
                String imgs = goods.getString("imgsSrc").replace("\"", "").replace("[", "").replace("]", "");
                String title = goods.getString("title");
                String[] imgsList = imgs.split(",");
                MomentItemBean momentItemBean = new MomentItemBean();
                momentItemBean.setTilte(title);
                momentItemBean.setUrls(imgsList);

                allDataList.add(momentItemBean); //将该条图文对象 添加进总集合;
            }
        }
        Collections.reverse(allDataList);  //倒序集合,达到最新的图文在最前面
        return allDataList;
    }

}
