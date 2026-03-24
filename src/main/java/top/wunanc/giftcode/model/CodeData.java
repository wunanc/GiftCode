package top.wunanc.giftcode.model;

public record CodeData(String uuid, String type, String content, int remaining, long expireTime) {
}