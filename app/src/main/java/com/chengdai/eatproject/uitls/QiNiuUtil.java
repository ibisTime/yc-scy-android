package com.chengdai.eatproject.uitls;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.util.Log;

import com.chengdai.eatproject.model.common.model.QiniuGetTokenModel;
import com.chengdai.eatproject.uitls.nets.RetrofitUtils;
import com.chengdai.eatproject.uitls.nets.RxTransformerHelper;
import com.chengdai.eatproject.widget.appmanager.MyConfig;
import com.chengdai.eatproject.widget.appmanager.SPUtilHelpr;
import com.qiniu.android.http.ResponseInfo;
import com.qiniu.android.storage.Configuration;
import com.qiniu.android.storage.UploadManager;

import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import id.zelory.compressor.Compressor;
import io.reactivex.Observable;


/**
 * Created by LeiQ on 2016/12/29.
 */

public class QiNiuUtil {

    private static final String ANDROID = "ANDROID";
    private static  final String IOS = "IOS";
    private String token = "";
    private Context context;

    static String size = "";
    static String imageWidth = "";
    static String imageHeight = "";


    public QiNiuUtil(Context context){
        this.context = context;
    }

    /**
     * 图片单张上传
     * @param callBack
     * @param url
     */
    private void uploadSingle(final QiNiuCallBack callBack , String url,String token){

        if(url.indexOf(ANDROID) == -1 || url.indexOf(IOS) == -1){

            Configuration config = new Configuration.Builder().build();
            UploadManager uploadManager = new UploadManager(config);
            String key = ANDROID + timestamp() + getImageWidthHeight(url) + ".jpg";


            LogUtil.E("图片");
            uploadManager.put(url, key, token,

                  (key1, info, res) -> {

                      //res包含hash、key等信息，具体字段取决于上传策略的设置
                      if(info !=null && info.isOK())
                      {
                          if(callBack !=null){
                              callBack.onSuccess(key, info, res);
                          }

                      } else{
                          if(callBack !=null){
                              callBack.onFal("token失败");
                          }
                          Log.i("QiNiu", "Upload Fail");
                          Log.i("QiNiu", "key="+key);
                          Log.i("QiNiu", "res="+res);
                          Log.i("QiNiu", "info="+info);
                      }

                  },null);
        }

    }


    /**
     * 获取七牛token
     * @return
     */
    public Observable<QiniuGetTokenModel> getQiniuToeknRequest(){
        Map<String,String> object=new HashMap<>();
        object.put("token", SPUtilHelpr.getUserToken());
        object.put("systemCode", MyConfig.SYSTEMCODE);
        return RetrofitUtils.getLoaderServer().getQiniuTOken("807900", StringUtils.getJsonToString(object))
                .compose(RxTransformerHelper.applySchedulerResult(null));
    }

    /**
     * 获取七牛url
     * @param callBack
     */
    public void getQiniuURL(QiNiuCallBack callBack,String data){

        getQiniuToeknRequest()
                .subscribe(r -> {
                    if(r==null ||  TextUtils.isEmpty(r.getUploadToken())){
                        return;
                    }
                    token=r.getUploadToken();
                    Compressor(callBack,data,token);

                },throwable -> {
                    callBack.onFal("图片上传失败,请选择正确的图片");
                });

    }


    //多张图片上传
    public void updataeImage(List<String> dataList,String mToekn,QiNiuCallBack callBack){

        for(int i=0;i<dataList.size();i++) {
            String imgPath = dataList.get(i);
            if(TextUtils.isEmpty(imgPath)){
                continue;
            }

            try {

                Compressor(callBack,imgPath,mToekn);
            }catch (Exception e){
                if(callBack!=null){
                    callBack.onFal("图片上传失败,请选择正确的图片");
                }
            }

        }

    }


    public static String getImageWidthHeight(String path){
        BitmapFactory.Options options = new BitmapFactory.Options();

        /**
         * 最关键在此，把options.inJustDecodeBounds = true;
         * 这里再decodeFile()，返回的bitmap为空，但此时调用options.outHeight时，已经包含了图片的高了
         */
        options.inJustDecodeBounds = true;
        Bitmap bitmap = BitmapFactory.decodeFile(path, options); // 此时返回的bitmap为null
        /**
         *options.outHeight为原始图片的高
         */
        imageWidth = options.outWidth + "";
        imageHeight = options.outHeight + "";
        size = "_" + imageWidth + "_" + imageHeight;

        System.out.print("size = _" + imageWidth + "_" + imageHeight);
        return size;
    }


    private static String timestamp(){
        String time = System.currentTimeMillis()+"";

        return "_"+time;
    }


    private void Compressor(QiNiuCallBack callBack,String data,String token){
        File compressedImageFile = Compressor.getDefault(context).compressToFile(new File(data));
        uploadSingle(callBack,compressedImageFile.getAbsolutePath(),token);

    }

    public interface QiNiuCallBack {
        void onSuccess(String key, ResponseInfo info, JSONObject res);
        void onFal(String info);
    }


}
