package com.mp.flashsale.exception;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;


@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum ErrorCode {
    //range 1xxx
    SUCCESS(1000, "Success", HttpStatus.OK),

    //range 2xxx
    REQUIRED_FIELD(2000, "{fieldName} is required.", HttpStatus.BAD_REQUEST),
    INVALID_NAME(2001, "The full name can only contain alphabet characters.", HttpStatus.BAD_REQUEST),
    INVALID_EMAIL(2002, "Please enter a valid email address.", HttpStatus.BAD_REQUEST),
    NOT_UNIQUE_EMAIL(2003, "Email already existed. Please try another email.", HttpStatus.BAD_REQUEST),
    INVALID_PHONE_NUMBER(2004, "Invalid phone number.", HttpStatus.BAD_REQUEST),
    NOT_UNIQUE_PHONE_NUMBER(2005, "The phone number already existed. Please try another phone number.", HttpStatus.BAD_REQUEST),
    INVALID_PASSWORD(2006, "Password must contain at least one number, one numeral, and seven characters.", HttpStatus.BAD_REQUEST),
    INVALID_LICENSE(2007, "Invalid license plate format! Expected format: (11-99)(A-Z)-(000-999).(00-99).", HttpStatus.BAD_REQUEST),
    NOT_UNIQUE_LICENSE(2008, "License plate already existed. Please try another license plate", HttpStatus.BAD_REQUEST),
    INVALID_COLOR(2009, "Your color were not predefined. Please try another color", HttpStatus.BAD_REQUEST),
    INVALID_ADDITIONAL_FUNTION(2010, "Your additional functions were not predefined. Please try another function", HttpStatus.BAD_REQUEST),
    INVALID_PRODUCTION_YEAR(2011, "Your production year was not predefined. Please try another year", HttpStatus.BAD_REQUEST),
    INVALID_NUMBER_OF_SEAT(2012, "Invalid number of seats. Allowed values are 4, 5, or 7.", HttpStatus.BAD_REQUEST),
    INVALID_BRAND(2013, "Your brand were not predefined. Please try another brand", HttpStatus.BAD_REQUEST),
    INVALID_ITEM_TYPE(2014, "Your item type were not predefined. Please try another item type", HttpStatus.BAD_REQUEST),
    INVALID_BRAND_MODEL(2015, "Your brand-model were not matched. Please try again", HttpStatus.BAD_REQUEST),
    INVALID_DOCUMENT_FILE(2016,"Invalid file type. Accepted formats are .doc, .docx, .pdf, .jpeg, .jpg, .png", HttpStatus.BAD_REQUEST),
    INVALID_CAR_IMAGE_FILE(2017,"Invalid file type. Accepted formats are .jpg, .jpeg, .png, .gif", HttpStatus.BAD_REQUEST),
    INVALID_VALUE_MIN(2018,"This attribute must be >=0", HttpStatus.BAD_REQUEST),
    INVALID_ADDRESS(2019,"The address is invalid", HttpStatus.BAD_REQUEST),
    INVALID_DATE_OF_BIRTH(2020, "Date of birth must be at least 18 years old.", HttpStatus.BAD_REQUEST),
    INVALID_IMAGE_FILE(2021, "Invalid image file type. Accepted formats are .jpg, .jpeg, .png", HttpStatus.BAD_REQUEST),
    INVALID_NATIONAL_ID(2022, "National ID must contain exactly 12 digits.", HttpStatus.BAD_REQUEST),
    NOT_UNIQUE_NATIONAL_ID(2023, "The national id already existed. Please try another national id", HttpStatus.BAD_REQUEST),
    INVALID_CAR_STATUS_CHANGE(2024,"Allowed transitions: NOT_VERIFIED → STOPPED, STOPPED → NOT_VERIFIED, VERIFIED → STOPPED", HttpStatus.BAD_REQUEST),
    INVALID_DATE_RANGE(2025, "Invalid date range. Pick-up date must be before drop-off date.", HttpStatus.BAD_REQUEST),
    INVALID_BOOKING_TIME(2026,"Invalid booking time", HttpStatus.BAD_REQUEST),
    INVALID_PAYMENT_TYPE(2027,"Payment can only WALLET/CASH/BANK_TRANSFER", HttpStatus.BAD_REQUEST),
    INVALID_ADDRESS_COMPONENT(2028,"Invalid address component", HttpStatus.BAD_REQUEST),
    INVALID_DATE_FORMAT(2029, "Invalid date format. Please use yyyy-MM-dd'T'HH:mm:ss", HttpStatus.BAD_REQUEST),
    INVALID_TRANSACTION_TYPE(2030,"The transaction type is invalid", HttpStatus.BAD_REQUEST),
    INVALID_DRIVER_INFO(2031,"Driver's information is different from account holder, but the information is not fulfilled",HttpStatus.BAD_REQUEST),
    INVALID_COMMENT_LENGTH(2032,"The comment length is invalid", HttpStatus.BAD_REQUEST),
    INVALID_RATING_RANGE(2033, "The rating range is invalid", HttpStatus.BAD_REQUEST),
    INVALID_CAR_STATUS(2034, "The status must be NOT_VERIFIED", HttpStatus.BAD_REQUEST),

    //range 3xxx
    UPLOAD_FILE_FAIL(3001, "There was an error occurred during uploading files. Please try again.", HttpStatus.SERVICE_UNAVAILABLE),
    ROLE_NOT_FOUND_IN_DB(3002, "The entity role requested was not found in the database.", HttpStatus.BAD_REQUEST),
    ACCOUNT_NOT_FOUND_IN_DB(3003, "The account does not exist in the system.", HttpStatus.BAD_REQUEST),
    MAXIMUM_FILE_UPLOAD_EXCEED(3004, "Maximum file upload exceeded. Each file should not exceed 5MB.", HttpStatus.PAYLOAD_TOO_LARGE),
    SEND_VERIFY_EMAIL_TO_USER_FAIL(3005, "There was an error during sending verification email. Please try again.", HttpStatus.SERVICE_UNAVAILABLE),
    INCORRECT_PASSWORD(3006, "Current password is incorrect.", HttpStatus.BAD_REQUEST),

    ITEM_NOT_FOUND_IN_DB(3007, "The item does not exist in the system.", HttpStatus.BAD_REQUEST),
    ITEM_NOT_VERIFIED(3008, "This item has not been verified and cannot be viewed.", HttpStatus.FORBIDDEN),
    ITEM_STOPPED(3009, "This item is no longer for sale.", HttpStatus.FORBIDDEN),
    ITEM_NOT_AVAILABLE(3010, "The item is not available.", HttpStatus.BAD_REQUEST),

    VNPAY_SIGNING_FAILED(3011, "Payment signing failed.", HttpStatus.BAD_REQUEST),
    VNPAY_CHECKSUM_FAILED(3012, "VNPAY Checksum validation failed.", HttpStatus.BAD_REQUEST),
    VNPAY_PAYMENT_FAILED(3013, "Payment failed.", HttpStatus.BAD_REQUEST),
    PAYMENT_FAILED(3014, "Amount exceeds wallet balance.", HttpStatus.BAD_REQUEST),
    WALLET_NOT_FOUND_IN_DB(3015, "The wallet does not exist in the system.", HttpStatus.BAD_REQUEST),
    SEND_FORGOT_PASSWORD_EMAIL_TO_USER_FAIL(3016, "Error sending forgot password email. Please try again.", HttpStatus.SERVICE_UNAVAILABLE),
    TRANSACTION_NOT_FOUND_IN_DB(3017, "The transaction does not exist in the system.", HttpStatus.BAD_REQUEST),

    ORDER_NOT_FOUND_IN_DB(3018, "The order does not exist in the system.", HttpStatus.BAD_REQUEST),
    ORDER_CANNOT_BE_EDITED(3019, "The order cannot be edited in its current status.", HttpStatus.BAD_REQUEST),
    INVALID_ORDER_STATUS(3020, "The order status does not allow this action.", HttpStatus.BAD_REQUEST),
    ORDER_EXPIRED(3021, "This flash sale order has expired.", HttpStatus.BAD_REQUEST),

    EMAIL_NOT_USED_BY_ANY_ACCOUNT(3022, "The email address entered does not exist.", HttpStatus.BAD_REQUEST),
    ORDER_CANNOT_CANCEL(3023, "The order cannot be cancelled in its current state.", HttpStatus.BAD_REQUEST),

    SEND_CANCELLED_ORDER_EMAIL_FAIL(3025, "Error sending cancellation email.", HttpStatus.SERVICE_UNAVAILABLE),
    SEND_NEW_ORDER_NOTIFICATION_FAIL(3026, "Error sending order notification email.", HttpStatus.SERVICE_UNAVAILABLE),
    SEND_WALLET_UPDATE_EMAIL_FAIL(3027, "Error sending wallet update email.", HttpStatus.SERVICE_UNAVAILABLE),
    SEND_ORDER_SUCCESS_EMAIL_FAIL(3028, "Error sending order confirmation email.", HttpStatus.SERVICE_UNAVAILABLE),
    SEND_COMPLETED_ORDER_EMAIL_FAIL(3029, "Error sending order completion email.", HttpStatus.SERVICE_UNAVAILABLE),
    SEND_PENDING_PAYMENT_EMAIL_FAIL(3030, "Error sending pending payment email.", HttpStatus.SERVICE_UNAVAILABLE),

    FEEDBACK_TIME_EXPIRED(3031, "Feedback is only allowed within 30 days after order completion.", HttpStatus.BAD_REQUEST),
    FEEDBACK_TOO_LONG(3032, "Feedback content must not exceed 2000 characters.", HttpStatus.BAD_REQUEST),
    ORDER_NOT_COMPLETED(3033, "This order is not COMPLETED.", HttpStatus.BAD_REQUEST),
    FEEDBACK_NOT_FOUND(3034, "Feedback not found for the given order ID.", HttpStatus.NOT_FOUND),
    FEEDBACK_ALREADY_EXISTS(3035, "Feedback for this order already exists.", HttpStatus.BAD_REQUEST),

    OUT_OF_STOCK(3047, "Sorry, this item is currently out of stock!", HttpStatus.BAD_REQUEST),
    INSUFFICIENT_BALANCE(3045, "Your wallet does not have enough balance.", HttpStatus.BAD_REQUEST),
    FLASH_SALE_CONCURRENCY_ERROR(3048, "The system is busy due to a high volume of purchases; please try again shortly!", HttpStatus.CONFLICT),
    PURCHASE_LIMIT_EXCEEDED(3049, "You have exceeded the maximum quantity allowed for this product!", HttpStatus.BAD_REQUEST),
    STILL_IN_COMPLAINT_PERIOD(3051, "The complaint period is not over yet. Cannot settle payment.", HttpStatus.BAD_REQUEST),
    ORDER_OWNERSHIP_MISMATCH(3052, "You do not have permission to perform this action on this order.", HttpStatus.FORBIDDEN),
    //range 4xxx
    UNCATEGORIZED_EXCEPTION(4000, "There was error happen during run time", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_ERROR_KEY(4001, "The error key could be misspelled", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_LOGIN_INFORMATION(4002, "Either email address or password is incorrect. Please try again.", HttpStatus.UNAUTHORIZED),
    UNAUTHENTICATED(4003, "Unauthenticated access. The access token is invalid", HttpStatus.UNAUTHORIZED), //401
    UNAUTHORIZED(4004, "User doesn't have permission to access the endpoint.", HttpStatus.FORBIDDEN), //403
//    ACCESS_TOKEN_EXPIRED(4005, "The access token is expired. Please try again", HttpStatus.UNAUTHORIZED),
    ACCOUNT_IS_INACTIVE(4006, "Your account is inactive.", HttpStatus.FORBIDDEN),
    REFRESH_TOKEN_EXPIRED(4008, "The refresh token is expired. Please login again.", HttpStatus.UNAUTHORIZED),

    INVALID_REFRESH_TOKEN(4009, "Invalid refresh token. Please try again.", HttpStatus.UNAUTHORIZED),
    FORBIDDEN_ITEM_ACCESS(4010, "Can not view detail/edit item of another account", HttpStatus.FORBIDDEN),
    INVALID_ONETIME_TOKEN(4011, "The token is invalid or this link has expired or has been used.", HttpStatus.BAD_REQUEST),
    INVALID_FORGOT_PASSWORD_TOKEN(4012, "This link has expired. Please go back to Homepage and try again.", HttpStatus.BAD_REQUEST),
    FORBIDDEN_PROFILE_INCOMPLETE(4013, "Please complete your individual profile to booking", HttpStatus.FORBIDDEN),
    FORBIDDEN_BOOKING_ACCESS(4014, "Can not view detail/edit booking of another account", HttpStatus.FORBIDDEN),
    INVALID_CSRF_TOKEN(4015, "Invalid or missing CSRF token. Access denied.", HttpStatus.FORBIDDEN),

    ;

    /**
     * A unique numeric identifier for the error.
     */
    int code;
    /**
     * A human-readable message describing the error.
     */
    String message;
    /**
     * The corresponding HTTP status to be returned when the error occurs.
     */
    HttpStatusCode httpStatusCode;

}
