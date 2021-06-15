package com.weibo;

import java.util.Date;

import org.nutz.dao.entity.annotation.*;

/**
 * create table t_content(id int(20) primary key auto_increment  ,uid varchar(50),createtiime date,content varchar(20000));
 */
@Table("t_content")
public class Content {
    @Id
    private long id;
    @Column
    @ColDefine(width=100)
    private String uid;
    @Column
    @ColDefine(width=100)
    private String weiboId;

    @Column
    private Date createtime;
    @Column
    @ColDefine(width=20000)
    private String content;

    public String getWeiboId() {
        return weiboId;
    }

    public void setWeiboId(String weiboId) {
        this.weiboId = weiboId;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public Date getCreatetime() {
        return createtime;
    }

    public void setCreatetime(Date createtime) {
        this.createtime = createtime;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
