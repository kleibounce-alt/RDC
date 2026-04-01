package entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author klei
 */
@Data
public class Good {
    private Integer id;
    private String goodName;
    private String goodImage;
    private String goodPrice;
    private String description;
    private String status;
    private Integer sellerId;
    private String sellingStatus;
    private LocalDateTime createTime;
}
