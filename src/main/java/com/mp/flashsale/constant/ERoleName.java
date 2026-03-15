package com.mp.flashsale.constant;

import lombok.Getter;

@Getter
public enum ERoleName {
    ADMIN(1),
    CUSTOMER(2),
    SELLER(3);
    private final int id;

    // Constructor của Enum mặc định là private
    ERoleName(int id) {
        this.id = id;
    }
}
