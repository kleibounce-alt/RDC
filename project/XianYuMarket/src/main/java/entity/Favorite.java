package entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author klei
 */
@Data
public class Favorite {
    private Integer id;
    private Integer userId;
    private Integer goodId;
    private LocalDateTime createTime;
}
