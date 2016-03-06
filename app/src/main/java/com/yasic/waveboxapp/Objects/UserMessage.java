package com.yasic.waveboxapp.Objects;

/**
 * Created by ESIR on 2016/3/6.
 */
public class UserMessage {
    /**
     * 消息内容
     */
    private String message;

    /**
     * 消息发送者昵称
     */
    private String posterNickName;

    /**
     * 发送者账号
     */
    private String posterAccount;

    /**
     * 接收者昵称
     */
    private String receiverNickName;


    /**
     * 消息接收者账号
     */
    private String receiverAccount;

    /**
     * 发送时间
     */
    private String messageSendTime;

    public String getMessage() {
        return message;
    }

    public String getPosterNickName() {
        return posterNickName;
    }

    public String getPosterAccount() {
        return posterAccount;
    }

    public String getReceiverNickName() {
        return receiverNickName;
    }

    public String getReceiverAccount() {
        return receiverAccount;
    }

    public String getMessageSendTime() {
        return messageSendTime;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setPosterNickName(String posterNickName) {
        this.posterNickName = posterNickName;
    }

    public void setPosterAccount(String posterAccount) {
        this.posterAccount = posterAccount;
    }

    public void setReceiverNickName(String receiverNickName) {
        this.receiverNickName = receiverNickName;
    }

    public void setReceiverAccount(String receiverAccount) {
        this.receiverAccount = receiverAccount;
    }

    public void setMessageSendTime(String messageSendTime) {
        this.messageSendTime = messageSendTime;
    }
}
