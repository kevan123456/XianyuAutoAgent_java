package org.automation.goofish.item;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

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
}
