package com.ryzin.notebook;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import com.ryzin.notebook.MyRecyclerView.MyItemClickListener;
import com.ryzin.notebook.MyRecyclerView.MyRecyclerViewAdapter;
import com.ryzin.notebook.NoteBookDB.Note;

import com.ryzin.notebook.MusicService.PlayMusicService;

import com.ryzin.notebook.NoteBookDB.NoteBookData;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;



public class ScrollingActivity extends AppCompatActivity {
    private static final String TAG = "ScrollingActivity";

    private RecyclerView recyclerView;
    private MyRecyclerViewAdapter myRecyclerViewAdapter;
    private List<Note> notes = new ArrayList<>();
    private TextView noNoteHintTextView;

    //规定开始音乐、暂停音乐、结束音乐的标志
    public static final int PLAY_MUSIC = 1;
    public static final int PAUSE_MUSIC = 2;
    public static final int STOP_MUSIC = 3;

    private int playStatus = 3;
    private MyBroadCastReceiver receiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scrolling);
        Toolbar toolbar =  findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //用于音乐播放
        receiver = new MyBroadCastReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.complete");
        registerReceiver(receiver, filter); //注册

        //音乐播放按钮
        FloatingActionButton fab_play_music = findViewById(R.id.fab_play_music);
        fab_play_music.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch(playStatus) {
                    case PLAY_MUSIC:
                        setMusic(PAUSE_MUSIC);
                        playStatus = PAUSE_MUSIC;
                        break;
                    case STOP_MUSIC:
                    case PAUSE_MUSIC:
                        setMusic(PLAY_MUSIC);
                        playStatus = PLAY_MUSIC;
                        break;
                }

            }
        });

        //新建日记
        FloatingActionButton fab_new_note = findViewById(R.id.fab_new_note);
        fab_new_note.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ScrollingActivity.this, NoteActivity.class);
                //startActivityForResult(intent, 1);
                startActivity(intent);
            }
        });

        //日记列表
        recyclerView = findViewById(R.id.recyclerView);
        // 设置LinearLayoutManager
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        // 设置ItemAnimator
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        // 设置固定大小
        recyclerView.setHasFixedSize(true);

        //提示没有笔记
        noNoteHintTextView = findViewById(R.id.noNoteHintTextView);

        initSideDeleteAndDragSort(); //初始化滑动删除和拖动排序
        loadData(); //加载数据
        refreshNoteViews(); //更新RecyclerViewAdapter
    }

    //初始化滑动删除和拖动排序
    private void initSideDeleteAndDragSort() {
        // 实现左边侧滑删除 和 拖动排序
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.Callback() {
            @Override
            public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                // 获取触摸响应的方向   包含两个 1.拖动dragFlags 2.侧滑删除swipeFlags
                // 代表只能是向左侧滑删除，当前可以是这样ItemTouchHelper.LEFT|ItemTouchHelper.RIGHT
                int swipeFlags = ItemTouchHelper.LEFT;


                // 拖动
                int dragFlags = 0;
                if (recyclerView.getLayoutManager() instanceof GridLayoutManager) {
                    // GridView 样式四个方向都可以
                    dragFlags = ItemTouchHelper.UP | ItemTouchHelper.LEFT |
                            ItemTouchHelper.DOWN | ItemTouchHelper.RIGHT;
                } else {
                    // ListView 样式不支持左右
                    dragFlags = ItemTouchHelper.UP |
                            ItemTouchHelper.DOWN;
                }

                return makeMovementFlags(dragFlags, swipeFlags);
            }

            /**
             * 拖动的时候不断的回调方法
             */
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                // 获取原来的位置
                int fromPosition = viewHolder.getAdapterPosition();
                // 得到目标的位置
                int targetPosition = target.getAdapterPosition();
                if (fromPosition > targetPosition) {
                    for (int i = fromPosition; i < targetPosition; i++) {
                        Collections.swap(notes, i, i + 1);// 改变实际的数据集
                    }
                } else {
                    for (int i = fromPosition; i > targetPosition; i--) {
                        Collections.swap(notes, i, i - 1);// 改变实际的数据集
                    }
                }
                myRecyclerViewAdapter.notifyItemMoved(fromPosition, targetPosition);
                return true;
            }

            /**
             * 侧滑删除后会回调的方法
             */
            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                // 获取当前删除的位置
                int position = viewHolder.getAdapterPosition();

                final int deletingDiaryId = notes.get(position).id;
                final Handler handler = new Handler();
                final Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        new NoteBookData(getApplicationContext()).delete(deletingDiaryId);
                    }
                };
                handler.postDelayed(runnable, 3500);

                notes.remove(position);
                myRecyclerViewAdapter.notifyItemRemoved(position);

                Snackbar.make(viewHolder.itemView, "已删除日记", Snackbar.LENGTH_LONG)
                        .setAction("撤销", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                handler.removeCallbacks(runnable);
                                loadData();
                                refreshNoteViews();
                            }
                        }).show();
            }

            /**
             * 拖动选择状态改变回调
             */
            @Override
            public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
                if (actionState != ItemTouchHelper.ACTION_STATE_IDLE) {
                    // ItemTouchHelper.ACTION_STATE_IDLE 看看源码解释就能理解了
                    // 侧滑或者拖动的时候背景设置为灰色
                    //viewHolder.itemView.setBackgroundColor(Color.GRAY);
                }
            }


            /**
             * 回到正常状态的时候回调
             */
            @Override
            public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                // 正常默认状态下背景恢复默认
                viewHolder.itemView.setBackgroundColor(0);
                ViewCompat.setTranslationX(viewHolder.itemView,0);
            }
        });

        // attach
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }

    private void loadData() {
        notes = new NoteBookData(getApplicationContext()).getAll();
    }

    // 更新适配器
    private void refreshNoteViews() {
        myRecyclerViewAdapter = new MyRecyclerViewAdapter(this, notes);

        myRecyclerViewAdapter.setOnItemClickListener(new MyItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Intent intent = new Intent(getApplicationContext(), NoteActivity.class);
                intent.putExtra("noteId", notes.get(position).id);
                startActivityForResult(intent, 1);
            }
        });

        // 为mRecyclerView设置适配器
        recyclerView.setAdapter(myRecyclerViewAdapter);

        if (!notes.isEmpty()) {
            noNoteHintTextView.setVisibility(View.GONE);
        } else {
            noNoteHintTextView.setVisibility(View.VISIBLE);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_scrolling, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == 1) {
            loadData();
            refreshNoteViews();
        }
    }

    private void setMusic(int type) {
        //启动服务，播放音乐
        Intent musicIntent = new Intent(this, PlayMusicService.class);
        musicIntent.putExtra("type", type);
        startService(musicIntent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }

    class MyBroadCastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Toast.makeText(context,"音乐播放结束", Toast.LENGTH_SHORT).show();
            playStatus = STOP_MUSIC;
        }
    }
}
