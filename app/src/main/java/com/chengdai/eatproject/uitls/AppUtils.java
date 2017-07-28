package com.chengdai.eatproject.uitls;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;
import android.widget.Button;

import com.jakewharton.rxbinding2.view.RxView;
import com.jakewharton.rxbinding2.widget.RxTextView;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

/**
 * Created by 李先俊 on 2017/6/8.
 */

public class AppUtils {

    static final Charset US_ASCII = Charset.forName("US-ASCII");
    static final Charset UTF_8 = Charset.forName("UTF-8");
    static String readFully(Reader reader) throws IOException {
        try {
            StringWriter writer = new StringWriter();
            char[] buffer = new char[1024];
            int count;
            while ((count = reader.read(buffer)) != -1) {
                writer.write(buffer, 0, count);
            }
            return writer.toString();
        } finally {
            reader.close();
        }
    }
    /**
     * Deletes the contents of {@code dir}. Throws an IOException if any file
     * could not be deleted, or if {@code dir} is not a readable directory.
     */
    static void deleteContents(File dir) throws IOException
    {
        File[] files = dir.listFiles();
        if (files == null)
        {
            throw new IOException("not a readable directory: " + dir);
        }
        for (File file : files)
        {
            if (file.isDirectory())
            {
                deleteContents(file);
            }
            if (!file.delete())
            {
                throw new IOException("failed to delete file: " + file);
            }
        }
    }
    static void closeQuietly(/*Auto*/Closeable closeable)
    {
        if (closeable != null)
        {
            try
            {
                closeable.close();
            } catch (RuntimeException rethrown)
            {
                throw rethrown;
            } catch (Exception ignored)
            {
            }
        }
    }


    /**
     * 判断一个Activity 是否存在
     *
     * @param clz
     * @return
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    public static boolean isActivityExist(Activity clz) {

        Activity activity = clz;
        if (activity == null) {
           return false;
        }

        if(activity.isFinishing()){
           return false;
        }

        if (!getAndroidVersion(Build.VERSION_CODES.JELLY_BEAN_MR1)) //如果版本小于 4.2
        {
            return true;
        }

        if (activity.isDestroyed()) {
          return false;
         }

        return true;
    }


    /*获取版本信息*/
    public static String getPackgeName(Context context) {
        String versionName = "";
        try {
            PackageManager pm = context.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(context.getPackageName(), 0);
            versionName = pi.packageName;
            if (versionName == null || versionName.length() <= 0) {
                return "";
            }
        } catch (Exception e) {
            Log.e("VersionInfo", "Exception", e);
        }
        return versionName;
    }

    /**
     * 根据包名跳转到系统自带的应用程序信息界面
     *
     * @param activity
     */
    public static void startDetailsSetting(Activity activity) {
        Uri packageURI = Uri.parse("package:" + getPackgeName(activity));
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, packageURI);
        activity.startActivity(intent);
        activity.finish();
    }

    public static Boolean getAndroidVersion(int version) {
        if (Build.VERSION.SDK_INT >= version) {
            return true;

        } else {
            return false;
        }
    }
    /**
     * 验证码倒计时
     * @param count  秒数
     * @param btn   按钮
     * @return
     */
    public static Disposable startCodeDown(int count, Button btn){
        return Observable.interval(1, TimeUnit.SECONDS, AndroidSchedulers.mainThread())    // 创建一个按照给定的时间间隔发射从0开始的整数序列
                .take(count)//只发射开始的N项数据或者一定时间内的数据
                .doOnSubscribe(disposable -> {
                    RxView.enabled(btn).accept(false);
                    RxTextView.text(btn).accept("60秒后重发");

                })
                .subscribe(aLong ->{
                    RxView.enabled(btn).accept(false);
                    RxTextView.text(btn).accept((count - aLong) + "秒后重发");
                } ,throwable -> {
                    RxView.enabled(btn).accept(true);
                    RxTextView.text(btn).accept("重发验证码");
                },() -> {
                    RxView.enabled(btn).accept(true);
                    RxTextView.text(btn).accept("重发验证码");
                });
    }


    public static int getAppVersion(Context context)
    {
        try
        {
            PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return info.versionCode;
        } catch (PackageManager.NameNotFoundException e)
        {
            e.printStackTrace();
        }
        return 1;
    }
    public static String hashKeyForDisk(String key)
    {
        String cacheKey;
        try
        {
            final MessageDigest mDigest = MessageDigest.getInstance("MD5");
            mDigest.update(key.getBytes());
            cacheKey = bytesToHexString(mDigest.digest());
        } catch (NoSuchAlgorithmException e)
        {
            cacheKey = String.valueOf(key.hashCode());
        }
        return cacheKey;
    }
    public static String bytesToHexString(byte[] bytes)
    {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bytes.length; i++)
        {
            String hex = Integer.toHexString(0xFF & bytes[i]);
            if (hex.length() == 1)
            {
                sb.append('0');
            }
            sb.append(hex);
        }
        return sb.toString();
    }
    public static byte[] bitmap2Bytes(Bitmap bm)
    {
        if (bm == null)
        {
            return null;
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.PNG, 100, baos);
        return baos.toByteArray();
    }
    public static Bitmap bytes2Bitmap(byte[] bytes)
    {
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }
    /**
     * Drawable → Bitmap
     */
    public static Bitmap drawable2Bitmap(Drawable drawable)
    {
        if (drawable == null)
        {
            return null;
        }
        // 取 drawable 的长宽
        int w = drawable.getIntrinsicWidth();
        int h = drawable.getIntrinsicHeight();
        // 取 drawable 的颜色格式
        Bitmap.Config config = drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565;
        // 建立对应 bitmap
        Bitmap bitmap = Bitmap.createBitmap(w, h, config);
        // 建立对应 bitmap 的画布
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, w, h);
        // 把 drawable 内容画到画布中
        drawable.draw(canvas);
        return bitmap;
    }
    /*
         * Bitmap → Drawable
         */
    @SuppressWarnings("deprecation")
    public static Drawable bitmap2Drawable(Bitmap bm)
    {
        if (bm == null)
        {
            return null;
        }
        BitmapDrawable bd = new BitmapDrawable(bm);
        bd.setTargetDensity(bm.getDensity());
        return new BitmapDrawable(bm);
    }

}
