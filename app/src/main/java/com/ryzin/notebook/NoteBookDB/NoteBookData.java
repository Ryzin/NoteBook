package com.ryzin.notebook.NoteBookDB;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import androidx.annotation.NonNull;

import java.util.LinkedList;
import java.util.List;

public class NoteBookData {
    private final Context applicationContext;

    public NoteBookData(Context context) {
        applicationContext = context;
    }

    private SQLiteDatabase getDatabase() {
        return new NoteBookDatabaseHelper(applicationContext).getReadableDatabase();
    }

    public List<Note> getAll() {
        SQLiteDatabase db = getDatabase();
        //创建游标对象
        Cursor cursor = db.query(
                "notebook",
                 new String[] {"id", "title", "content", "updatedAt"},
                "",
                null,
                null,
                null,
                null);

        List<Note> results = new LinkedList<>();
        //利用游标遍历所有数据对象，存放到List<Node>
        while (cursor.moveToNext()) {
            results.add(new Note(
                    cursor.getInt(0), // id
                    cursor.getString(1), // title
                    cursor.getString(2), // content
                    cursor.getLong(3) // updatedAt
            ));
        }
        cursor.close();
        db.close();
        return results;
    }

    //通过索引获得单条记录
    public Note get(int id) {
        List<Note> notes = execQueryingSql("SELECT * FROM notebook WHERE id=" + id);
        if (notes.isEmpty()) return null;
        return notes.get(0);
    }

    public long post(Note note) {
        SQLiteDatabase db = getDatabase();
        long l = innerPost(db, note);
        //innerPost return the row ID of the newly inserted row, or -1 if an error occurred
        db.close();
        return l;
    }

    public void post(List<Note> notes) {
        SQLiteDatabase db = getDatabase();
        for (Note note : notes) {
            innerPost(db, note);
        }
        db.close();
    }

    protected long innerPost(SQLiteDatabase db, Note note) {
        ContentValues values = new ContentValues();
        values.put("title", note.title);
        values.put("content", note.content);
        values.put("updatedAt", note.updatedAt);
        /*
        table: 要插入数据的表的名称
        nullColumnHack：当values参数为空或者里面没有内容的时候，我们insert是会失败的（
        底层数据库不允许插入一个空行），为了防止这种情况，我们要在这里指定一个列名，
        到时候如果发现将要插入的行为空行时，就会将你指定的这个列名的值设为null，然后
        再向数据库中插入。values:一个ContentValues对象，类似一个map.通过键值对的形式存储值。
         */
        return db.insert("notebook", null, values);
    }

    //更新记录
    public void update(Note note) {
        SQLiteDatabase db = getDatabase();
        ContentValues values = new ContentValues();
        values.put("title", note.title);
        values.put("content", note.content);
        values.put("updatedAt", note.updatedAt);
        db.update("notebook", values, "id=?", new String[] {note.id+""});
        db.close();
    }


    public NoteSaveStatus put(Note note) {
        List<Note> notes = execQueryingSql("SELECT * from notebook WHERE id=" + note.id);
        if (notes.isEmpty()) {
            post(note);
            return NoteSaveStatus.CREATED;
        } else { //如果查询语句中找到了对应记录，内容相同的话，就无需保存
            Note oldNote = notes.get(0);
            if (oldNote.title.equals(note.title) && oldNote.content.equals(note.content)) {
                return NoteSaveStatus.NO_NEED_TO_SAVE;
            } else {
                update(note);
                return NoteSaveStatus.UPDATED;
            }
        }
    }

    public void delete(long id) {
        SQLiteDatabase db = getDatabase();
        /*
        @return the number of rows affected if a whereClause is passed in, 0
     *         otherwise. To remove all rows and get a count pass "1" as the
     *         whereClause.
         */
        db.delete("notebook", "id=?", new String[] {id+""});
        db.close();
    }

    public void clear() {
        SQLiteDatabase db = getDatabase();
        db.execSQL("DELETE FROM notebook");
        db.close();
    }

    /**
     * 执行非破坏性SQL语句
     * @param sql SQL语句
     * @return 查询到的结果
     * @throws SQLException
     */
    protected List<Note> execQueryingSql(@NonNull String sql) throws SQLException {
        List<Note> list = new LinkedList<>();
        SQLiteDatabase db = getDatabase();
        Cursor cursor = db.rawQuery(sql, new String[] {});
        while (cursor.moveToNext()) {
            list.add(new Note(
                    cursor.getInt(0),
                    cursor.getString(1),
                    cursor.getString(2),
                    cursor.getLong(4)
            ));
        }
        cursor.close();
        db.close();
        return list;
    }

}
