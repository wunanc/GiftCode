package top.wunanc.giftcode.model;

public class CodeData {
    private String uuid;
    private String type;
    private String content;
    private int remaining;
    private long expireTime;

    public CodeData(String uuid, String type, String content, int remaining, long expireTime) {
        this.uuid = uuid;
        this.type = type;
        this.content = content;
        this.remaining = remaining;
        this.expireTime = expireTime;
    }

    public String getUuid() { return uuid; }
    public String getType() { return type; }
    public String getContent() { return content; }
    public int getRemaining() { return remaining; }
    public long getExpireTime() { return expireTime; }
}