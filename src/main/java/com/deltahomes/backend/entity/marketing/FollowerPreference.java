package com.deltahomes.backend.entity.marketing;

import com.deltahomes.backend.entity.base.BaseEntity;
import com.deltahomes.backend.entity.company.Company;
import com.deltahomes.backend.entity.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "follower_preferences", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "company_id"})
})
public class FollowerPreference extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @Column(name = "wants_offers", nullable = false)
    private Boolean wantsOffers = true;

    @Column(name = "wants_videos", nullable = false)
    private Boolean wantsVideos = true;

    @Column(name = "wants_new_properties", nullable = false)
    private Boolean wantsNewProperties = true;

    @Column(name = "wants_news", nullable = false)
    private Boolean wantsNews = true;

    @Column(name = "wants_discounts", nullable = false)
    private Boolean wantsDiscounts = true;
}
