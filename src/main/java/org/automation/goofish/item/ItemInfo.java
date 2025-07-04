package org.automation.goofish.item;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ItemInfo {
    private ItemDO itemDO;

    private SellerDO sellerDO;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ItemDO {
        private String soldPrice;
        private List<ItemLabelExt> itemLabelExtList;
        private String desc;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ItemLabelExt {
        private String properties; // ex. "properties" : "122216545##是否评级卡:21959##否"
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SellerDO {
        private String city;
        private String signature;
    }

    public String promptSellerSignature() {
        return "商家自我介绍: %s".formatted(this.sellerDO.signature);
    }

    public String promptSellerLocation() {
        return "商家店铺位置: %s".formatted(this.sellerDO.city);
    }

    public String promptSellGoodPrice() {
        return "当前商品售价: %s".formatted(this.itemDO.soldPrice);
    }

    public String promptSellGoodDesc() {
        return "当前商品描述: %s".formatted(this.itemDO.desc);
    }

    public String promptSellGoodLabel() {
        String labels = itemDO.itemLabelExtList.stream()
                .map(label -> {
                    String properties = label.getProperties();
                    Pattern pattern = Pattern.compile("##([^:#]+):-?\\d+##([^#]+)");
                    Matcher matcher = pattern.matcher(properties);
                    if (matcher.find()) {
                        String key = matcher.group(1);   // 如 "分类"、"是否评级卡"
                        String value = matcher.group(2); // 如 "桌游卡牌"、"否"
                        return key + ":" + value;
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.joining(", "));
        return "当前商品标签: %s".formatted(labels);
    }
}
