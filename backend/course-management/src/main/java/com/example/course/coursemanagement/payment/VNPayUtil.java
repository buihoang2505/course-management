package com.example.course.coursemanagement.payment;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Tiện ích tạo và xác thực URL VNPay.
 * Tài liệu: https://sandbox.vnpayment.vn/apis/docs/thanh-toan-pay/pay.md
 */
public class VNPayUtil {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    /** Tạo URL thanh toán VNPay */
    public static String buildPaymentUrl(
            String vnpTmnCode,
            String vnpHashSecret,
            String vnpUrl,
            String returnUrl,
            String txnRef,
            long   amountVnd,
            String orderInfo,
            String ipAddr
    ) throws Exception {
        Map<String, String> params = new TreeMap<>();
        params.put("vnp_Version",    "2.1.0");
        params.put("vnp_Command",    "pay");
        params.put("vnp_TmnCode",    vnpTmnCode);
        params.put("vnp_Amount",     String.valueOf(amountVnd * 100)); // VNPay nhân 100
        params.put("vnp_CurrCode",   "VND");
        params.put("vnp_TxnRef",     txnRef);
        params.put("vnp_OrderInfo",  orderInfo);
        params.put("vnp_OrderType",  "other");
        params.put("vnp_Locale",     "vn");
        params.put("vnp_ReturnUrl",  returnUrl);
        params.put("vnp_IpAddr",     ipAddr);
        params.put("vnp_CreateDate", LocalDateTime.now().format(FMT));
        params.put("vnp_ExpireDate", LocalDateTime.now().plusMinutes(15).format(FMT));

        // Build query string (chưa có secure hash)
        StringBuilder query = new StringBuilder();
        StringBuilder hashData = new StringBuilder();
        for (Map.Entry<String, String> e : params.entrySet()) {
            String encodedKey = URLEncoder.encode(e.getKey(), StandardCharsets.UTF_8);
            String encodedVal = URLEncoder.encode(e.getValue(), StandardCharsets.UTF_8);
            query.append('&').append(encodedKey).append('=').append(encodedVal);
            hashData.append('&').append(e.getKey()).append('=').append(e.getValue());
        }

        String secureHash = hmacSHA512(vnpHashSecret, hashData.substring(1));
        return vnpUrl + "?" + query.substring(1) + "&vnp_SecureHash=" + secureHash;
    }

    /** Xác thực chữ ký IPN / Return từ VNPay */
    public static boolean verifyIpn(Map<String, String> params, String secretKey) throws Exception {
        String receivedHash = params.get("vnp_SecureHash");
        if (receivedHash == null) return false;

        Map<String, String> sorted = new TreeMap<>(params);
        sorted.remove("vnp_SecureHash");
        sorted.remove("vnp_SecureHashType");

        StringBuilder hashData = new StringBuilder();
        for (Map.Entry<String, String> e : sorted.entrySet()) {
            hashData.append('&').append(e.getKey()).append('=').append(e.getValue());
        }
        String computed = hmacSHA512(secretKey, hashData.substring(1));
        return computed.equalsIgnoreCase(receivedHash);
    }

    private static String hmacSHA512(String key, String data) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA512");
        mac.init(new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512"));
        byte[] bytes = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) sb.append(String.format("%02x", b));
        return sb.toString();
    }

    /** Tạo txnRef duy nhất = timestamp + random */
    public static String generateTxnRef() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMMddHHmmss"))
                + String.format("%04d", new Random().nextInt(10000));
    }
}