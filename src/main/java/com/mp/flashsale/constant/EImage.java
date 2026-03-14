package com.mp.flashsale.constant;

import lombok.Getter;
/**
 * Represents the image type
 * @author QuangPM20
 *
 * @version 1.0
 */
@Getter
public enum EImage {
    JPG("jpg"),
    JPEG("jpeg"),
    PNG("png"),
    GIF("gif");
    private final String extension;

    EImage(String extension) {
        this.extension = extension;
    }
}
