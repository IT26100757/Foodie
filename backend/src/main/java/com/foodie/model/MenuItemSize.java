
package com.foodie.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(
        name = "menu_item_sizes",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"menu_item_id", "size_name"}
        )
)
public class MenuItemSize {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "menu_item_id", nullable = false)
    private MenuItem menuItem;

    @Column(name = "size_name", nullable = false, length = 30)
    private String sizeName;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    public MenuItemSize() {}

    public Long getId() {
        return id;
    }

    public MenuItem getMenuItem() {
        return menuItem;
    }

    public void setMenuItem(MenuItem menuItem) {
        this.menuItem = menuItem;
    }

    public String getSizeName() {
        return sizeName;
    }

    public void setSizeName(String sizeName) {
        this.sizeName = sizeName;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }
}
