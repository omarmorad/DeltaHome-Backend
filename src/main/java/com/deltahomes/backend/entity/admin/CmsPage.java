package com.deltahomes.backend.entity.admin;

import com.deltahomes.backend.entity.base.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "cms_pages")
public class CmsPage extends BaseEntity {

    @Column(name = "slug", length = 100, unique = true, nullable = false)
    private String slug;

    @Column(name = "title", length = 255, nullable = false)
    private String title;

    @Column(name = "body_html", columnDefinition = "TEXT")
    private String bodyHtml;

    @Column(name = "locale", length = 10)
    private String locale;
}
