package com.mp.flashsale.payment.service;


import com.mp.flashsale.payment.dto.response.IpnResponse;

import java.util.Map;

public interface IpnHandler {
    IpnResponse process(Map<String, String> params);
}
