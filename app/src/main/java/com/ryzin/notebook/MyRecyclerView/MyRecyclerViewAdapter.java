package com.ryzin.notebook.MyRecyclerView;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import com.ryzin.notebook.R;
import com.ryzin.notebook.NoteBookDB.Note;

import java.text.SimpleDateFormat;
import java.util.List;

public class MyRecyclerViewAdapter extends RecyclerView.Adapter<MyViewHolder>{
    private final String TAG = "MyRecyclerViewAdapter";
    private List<Note> notes;
    private Context mContext;

    private MyItemClickListener mItemClickListener;

    public MyRecyclerViewAdapter( Context context , List<Note> notes)
    {
        this.mContext = context;
        this.notes = notes;

        Log.i(TAG, "MyRecyclerViewAdapter");
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup viewGroup, int i )
    {
        // 给ViewHolder设置布局文件
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.card_item, viewGroup, false);
        return new MyViewHolder(v, mItemClickListener);
    }

    @Override
    public void onBindViewHolder( MyViewHolder viewHolder, int pos )
    {
        // 给ViewHolder设置元素
        Note note = notes.get(pos);

        Log.i(TAG, "位置：" + pos);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss");
        String date = dateFormat.format(note.updatedAt);
        viewHolder.mTimeTextView.setText(date.substring(0, 11));
        viewHolder.mTitleTextView.setText(note.title);
        viewHolder.mContentTextView.setText(note.content);
    }

    @Override
    public int getItemCount()
    {
        // 返回数据总数
        return notes == null ? 0 : notes.size();
    }

    /**
     * 设置Item点击监听
     * @param listener
     */
    public void setOnItemClickListener(MyItemClickListener listener){
        this.mItemClickListener = listener;
    }

}
