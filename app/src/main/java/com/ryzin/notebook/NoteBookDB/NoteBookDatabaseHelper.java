package com.ryzin.notebook.NoteBookDB;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class NoteBookDatabaseHelper extends SQLiteOpenHelper {
    public NoteBookDatabaseHelper(Context context) {
        super(context, "notebook.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(
                //创建表
                "CREATE TABLE notebook(id INTEGER PRIMARY KEY AUTOINCREMENT, title TEXT, content TEXT, createdAt INTEGER, updatedAt INTEGER)"
        );

        long time = System.currentTimeMillis();
        sqLiteDatabase.execSQL(
                //插入数据
                "INSERT INTO notebook(title, content, createdAt, updatedAt) values ('下乡感想', '虽然我不是支教组的队员，和西江中学的同学并没有多少接触。但是威了不让他们的努力只限于下乡期间', ?, ?)",
                new Object[] { time-70L*1000*60*60*24, time-70L*1000*60*60*24 });
        sqLiteDatabase.execSQL(
                "INSERT INTO notebook(title, content, createdAt, updatedAt) values ('年乘坐高铁的频率', '男：我是通勤人员，上下班嘛就天天坐。她就不一样。所以不应该问我，问里面的旅客好一点。 ', ?, ?)",
                new Object[] { time-25L*1000*60*60*24, time-25L*1000*60*60*24 });
        sqLiteDatabase.execSQL(
                "INSERT INTO notebook(title, content, createdAt, updatedAt) values ('《矛盾论》是一本什么样的书？', '基于文本分析的数据可视化', ?, ?)",
                new Object[] { time, time });
        sqLiteDatabase.execSQL(
                "INSERT INTO notebook(title, content, createdAt, updatedAt) values ('小程序匿名投票系统', '投票时匿名，并且将混淆', ?, ?)",
                new Object[] { time, time });
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {}
}
