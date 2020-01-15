package com.ryzin.notebook.MyRecyclerView;

import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.ryzin.notebook.R;

class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    public TextView mTimeTextView;
    public TextView mTitleTextView;
    public TextView mContentTextView;
    private MyItemClickListener mListener;

    public MyViewHolder(View rootView, MyItemClickListener listener) {
        super(rootView);
        mTitleTextView = rootView.findViewById(R.id.title);
        mContentTextView =  rootView.findViewById(R.id.content);
        mTimeTextView = rootView.findViewById(R.id.date);
        this.mListener = listener;
        rootView.setOnClickListener(this);
    }

    /**
     * 点击监听
     */
    @Override
    public void onClick(View v) {
        if(mListener != null){
            mListener.onItemClick(v, getPosition());
        }
    }
}