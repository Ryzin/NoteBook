package com.ryzin.notebook.NoteBookDB;

public class Note {
    public Integer id; //key
    public String title; //标题
    public String content; //内容
    public Long updatedAt; //修改时间

    public Note(int id, String title, String content, long updatedAt)
    {
        this.id = id;
        this.title = title;
        this.content = content;
        this.updatedAt = updatedAt;
    }
}

