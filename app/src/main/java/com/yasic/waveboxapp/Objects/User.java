package com.yasic.waveboxapp.Objects;

/**
 * Created by ESIR on 2016/3/6.
 */
public class User {

    public User(String userNickName,String userAccount,String lastMessage){
        this.userNickName = userNickName;
        this.userAccount = userAccount;
        this.lastMessage = lastMessage;
    }

    public User(String userNickName){
        this.userNickName = userNickName;
        this.userAccount = "";
        this.lastMessage = "";
    }

    /**
     * 用户昵称
     */
    private String userNickName;

    /**
     * 用户头像
     */
    private String userAccount;

    /**
     * 用户最新消息
     */
    private String lastMessage;

    public String getUserNickName() {
        return userNickName;
    }

    public String getUserAccount() {
        return userAccount;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setUserNickName(String userNickName) {
        this.userNickName = userNickName;
    }

    public void setUserAccount(String userAccount) {
        this.userAccount = userAccount;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }
}
