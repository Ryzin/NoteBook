package com.ryzin.notebook;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Html;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.ryzin.notebook.NoteBookDB.Note;
import com.ryzin.notebook.NoteBookDB.NoteBookData;
import com.ryzin.notebook.NoteBookDB.NoteSaveStatus;

import java.text.SimpleDateFormat;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class NoteActivity extends AppCompatActivity {
    private TextView timeTextView;
    private EditText titleEditText;
    private EditText contentEditText;

    private ScheduledExecutorService executorService; //延迟/周期执行线程池
    private long currentNoteId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);

        final Drawable upArrow = getResources().getDrawable(R.drawable.ic_arrow_back_black_24dp);
        upArrow.setColorFilter(getResources().getColor(R.color.grey), PorterDuff.Mode.SRC_ATOP);
        getSupportActionBar().setHomeAsUpIndicator(upArrow);

        getSupportActionBar().setTitle(Html.fromHtml("<font color='#3f3f3f' size='18px'>编辑日记</font>"));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        initialize();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executorService.shutdownNow();
        saveNote();
    }

    @Override
    public void onBackPressed() {
        saveNote();
        finish();
    }

    private void initialize() {
        bindViews();

        Intent intent = getIntent();
        currentNoteId = intent.getIntExtra("noteId", -1);
        if (currentNoteId != -1) {
            Note currentNote = new NoteBookData(getApplicationContext()).get((int) currentNoteId);
            showData(currentNote);
        } else {
            showEmptyData();
        }
        setResult(1, new Intent());

        executorService = Executors.newScheduledThreadPool(1);
        executorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                new Thread(new DataPersistenceService()).start(); //开新线程用于更新编辑时间
            }
        }, 5, 5, TimeUnit.SECONDS);
    }

    private void bindViews() {
        timeTextView = findViewById(R.id.timeTextView);
        titleEditText = findViewById(R.id.titleEditText);
        contentEditText = findViewById(R.id.contentEditText);
    }

    @SuppressLint("SetTextI18n")
    private void showData(Note note) {
        timeTextView.setText(getFormattedUpdateTimeString(note.updatedAt));
        titleEditText.setText(note.title);
        contentEditText.setText(note.content);
    }

    //更新最后编辑时间
    private void refreshUpdateTime() {
        if (currentNoteId == -1) throw new IllegalStateException("currentNoteId not specified");
        Note currentNote = new NoteBookData(getApplicationContext()).get((int) currentNoteId);
        timeTextView.setText(getFormattedUpdateTimeString(currentNote.updatedAt));
    }

    private String getFormattedUpdateTimeString(long time) {
        SimpleDateFormat dateformat = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss");
        String dateStr = dateformat.format(time);
        return "最后编辑于 " + dateStr;
    }

    private void showEmptyData() {
        timeTextView.setVisibility(View.GONE);
    }

    private NoteSaveStatus saveNote() {
        if (!titleEditText.getText().toString().trim().equals("")) {
            if (currentNoteId == -1) { //如果是新建
                NoteBookData da = new NoteBookData(getApplicationContext());
                currentNoteId = da.post(new Note(
                        (int) currentNoteId,
                        titleEditText.getText().toString(),
                        contentEditText.getText().toString(),
                        System.currentTimeMillis()));
                return NoteSaveStatus.CREATED;
            } else { //如果保存Note的时候发现已经存在相同记录，返回NO_NEED_TO_SAVE；不同记录，UPDATE；不存在记录CREATE
                return new NoteBookData(getApplicationContext())
                        .put(new Note(
                                (int) currentNoteId,
                                titleEditText.getText().toString(),
                                contentEditText.getText().toString(),
                                System.currentTimeMillis()));
            }
        } else { //
            return NoteSaveStatus.NO_NEED_TO_SAVE;
        }
    }

    private class DataPersistenceService implements Runnable{
        public void run(){
            if (saveNote() != NoteSaveStatus.NO_NEED_TO_SAVE) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        refreshUpdateTime();
                    }
                });
            }
        }
    }
}
