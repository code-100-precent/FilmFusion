package cn.cxdproject.coder.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BannerVO {
    private Long id;

    private String imageName;

    private String imageUrl;

    private String targetModule;

    private Boolean status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private Long sort;

    public BannerVO(Long id, String imageName, String imageUrl, String targetModule, Boolean status, Long sort) {
    this.id=id;
    this.imageName=imageName;
    this.imageUrl=imageUrl;
    this.targetModule=targetModule;
    this.status=status;
    this.sort=sort;
    }
}
