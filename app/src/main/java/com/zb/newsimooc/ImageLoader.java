package com.zb.newsimooc;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.LruCache;
import android.widget.ImageView;
import android.widget.ListView;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Administrator on 2016/8/12.
 */
public class ImageLoader {
    private ImageView mImageView;
    private String mUrl;
    private LruCache<String,Bitmap> mCaches;
    private ListView mListView;
    private Set<NewsAsyncTask> mTask;
    public ImageLoader(ListView listView ){
        mListView = listView;
        mTask = new HashSet<>();
        //获取最大可用内存
        int maxMemory = (int) Runtime.getRuntime().maxMemory();
        int cacheSize = maxMemory/4;
        mCaches = new LruCache<String,Bitmap>(cacheSize){
            @Override
            protected int sizeOf(String key, Bitmap value) {
                //在每次存入缓存的时候调用
                return value.getByteCount();
            }
        };
    }
    //增加到缓存
    public void addBitmapToCache(String url,Bitmap bitmap){
        if (getBitmapFromCache(url) == null){
            mCaches.put(url,bitmap);
        }
    }
    public Bitmap getBitmapFromCache(String url){
        return mCaches.get(url);
    }
    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (mImageView.getTag().equals(mUrl)){
                mImageView.setImageBitmap((Bitmap) msg.obj);

            }
        }
    };
    public void showImageByThread(ImageView imageView, final String url){
        mImageView = imageView;
        mUrl = url;
        new Thread(){
            @Override
            public void run() {
                super.run();
                Bitmap bitmap = getBitmapFromUrl(url);
                Message message = Message.obtain();
                message.obj = bitmap;
                mHandler.sendMessage(message);
            }
        }.start();

    }
    public void showImageByAsyncTask(ImageView imageView,String url){
        //从缓存中取出对应的图片
        Bitmap bitmap = getBitmapFromCache(url);
        //如果缓存中没有，那么必须去下载
        if (bitmap == null){
            imageView.setImageResource(R.mipmap.ic_launcher);
        }else {
            imageView.setImageBitmap(bitmap);//此时还在主线程
        }
    }

    public void cancleAllTasks(){
        if (mTask !=null){
            for (NewsAsyncTask task :mTask){
                task.cancel(false);
            }
        }
    }
    public void loadImages(int start,int end){
            for (int i = start;i < end ; i++){
                String url = NewsAdapter.urls[i];
                Bitmap bitmap = getBitmapFromCache(url);
                //如果缓存中没有，那么必须去下载
                if (bitmap == null){
                    NewsAsyncTask task = new NewsAsyncTask(url);
                    task.execute(url);
                    mTask.add(task);
                }else {
                    ImageView imageView = (ImageView) mListView.findViewWithTag(url);
                    imageView.setImageBitmap(bitmap);//此时还在主线程
                }
            }
    }

    private class NewsAsyncTask extends AsyncTask<String ,Void,Bitmap>{
        //private ImageView mImageView;
        private String mUrl;
        public NewsAsyncTask(String url){
           // mImageView = imageView;
            mUrl = url;
        }

        @Override
        protected Bitmap doInBackground(String... strings) {
            String url = strings[0];
            //从网络获取图片
            Bitmap bitmap = getBitmapFromUrl(url);
            if (bitmap!=null){
                //将不在缓存的图片加入缓存
                addBitmapToCache(url,bitmap);
            }
            return bitmap;

        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            ImageView imageView = (ImageView) mListView.findViewWithTag(mUrl);
            if (imageView !=null && bitmap !=null){
                imageView.setImageBitmap(bitmap);
            }
            mTask.remove(this);
        }
    }

    public Bitmap getBitmapFromUrl(String urlString){
        Bitmap bitmap;
        InputStream is = null;
        try {
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            is = new BufferedInputStream(connection.getInputStream());
            bitmap = BitmapFactory.decodeStream(is);
            connection.disconnect();

            return bitmap;
        }catch (IOException e) {
            e.printStackTrace();
        }  finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
