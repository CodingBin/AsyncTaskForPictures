package com.zb.newsimooc;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Administrator on 2016/8/12.
 */
public class NewsAdapter extends BaseAdapter implements AbsListView.OnScrollListener {
    private List<NewsBean> mList;
    private LayoutInflater mInflater;
    private ImageLoader mImageLoader;
    private int mStart,mEnd;
    public static String[] urls;
    private boolean mFirstIn;
    public NewsAdapter(Context context, List<NewsBean> data, ListView listView){
        mList = data;
        mInflater = LayoutInflater.from(context);
        mImageLoader = new ImageLoader(listView);
        urls = new String[data.size()];
        for (int i = 0 ; i < data.size(); i ++){
            urls[i] = data.get(i).newsIconUrl;
        }
        mFirstIn = true;
        listView.setOnScrollListener(this);
    }
    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public Object getItem(int i) {
        return mList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder viewHolder = null;
        if (view == null){
            viewHolder = new ViewHolder();
            view = mInflater.inflate(R.layout.item_layout,null);
            viewHolder.ivIcon = (ImageView) view.findViewById(R.id.iv_icon);
            viewHolder.tvContent = (TextView) view.findViewById(R.id.tv_content);
            viewHolder.tvTitle = (TextView) view.findViewById(R.id.tv_title);
            view.setTag(viewHolder);
        }else {
            viewHolder = (ViewHolder) view.getTag();

        }
        viewHolder.ivIcon.setImageResource(R.mipmap.ic_launcher);
        String url = mList.get(i).newsIconUrl;
        viewHolder.ivIcon.setTag(url);
        //new ImageLoader().showImageByThread(viewHolder.ivIcon,url);
        mImageLoader.showImageByAsyncTask(viewHolder.ivIcon,url);
        viewHolder.tvTitle.setText(mList.get(i).newsTitle);
        viewHolder.tvContent.setText(mList.get(i).newContent);
        return view;
    }

    @Override
    public void onScrollStateChanged(AbsListView absListView, int i) {
            if (i == SCROLL_STATE_IDLE){
                //加载可见项
                mImageLoader.loadImages(mStart,mEnd);
            }else {
                //停止任务
                mImageLoader.cancleAllTasks();
            }
    }

    @Override
    public void onScroll(AbsListView absListView, int i, int i1, int i2) {
            mStart = i;
            mEnd = i+i1;
        //第一次显示的时候调用
        if (mFirstIn && i1>0){
            mImageLoader.loadImages(mStart,mEnd);
            mFirstIn = false;
        }
    }

    class ViewHolder{
        public TextView tvTitle,tvContent;
        public ImageView ivIcon;
    }
}
