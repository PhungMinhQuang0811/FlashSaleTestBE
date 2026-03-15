package com.mp.flashsale.service.impl;

import com.mp.flashsale.constant.EPaymentStatus;
import com.mp.flashsale.constant.ERoleName;
import com.mp.flashsale.constant.ETransactionStatus;
import com.mp.flashsale.constant.ETransactionType;
import com.mp.flashsale.dto.response.transaction.TransactionPaymentURLResponse;
import com.mp.flashsale.dto.response.transaction.TransactionResponse;
import com.mp.flashsale.entity.Account;
import com.mp.flashsale.entity.Transaction;
import com.mp.flashsale.entity.Wallet;
import com.mp.flashsale.exception.AppException;
import com.mp.flashsale.exception.ErrorCode;
import com.mp.flashsale.mapper.TransactionMapper;
import com.mp.flashsale.payment.constant.VNPayIPNResponseConst;
import com.mp.flashsale.payment.dto.request.InitPaymentRequest;
import com.mp.flashsale.payment.dto.response.InitPaymentResponse;
import com.mp.flashsale.payment.dto.response.IpnResponse;
import com.mp.flashsale.payment.service.IpnHandler;
import com.mp.flashsale.payment.service.PaymentService;
import com.mp.flashsale.repository.AccountRepository;
import com.mp.flashsale.repository.TransactionRepository;
import com.mp.flashsale.repository.WalletRepository;
import com.mp.flashsale.service.TransactionService;
import com.mp.flashsale.util.RedisUtil;
import com.mp.flashsale.util.SecurityUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Map;
import java.util.TimeZone;
import java.util.TreeMap;

@Service
@RequiredArgsConstructor
@Transactional
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class TransactionServiceImpl implements TransactionService {
    WalletRepository walletRepository;
    AccountRepository accountRepository;
    PaymentService paymentService;
    RedisUtil redisUtil;
    TransactionRepository transactionRepository;
    TransactionMapper transactionMapper;
    IpnHandler ipnHandler;
    @Value("${payment.vnpay.tmn-code}")
    @NonFinal
    String tmnCode;
    @Value("${payment.vnpay.secret-key}")
    @NonFinal
    String secretKey;
    @Value("${payment.vnpay.init-payment-url}")
    @NonFinal
    String vnpUrl;
    @Value("${payment.vnpay.return-url}")
    @NonFinal
    String returnUrl;

    @Override
    public void executeEscrowPayment(String customerId, Long amount) {
        Wallet customerWallet = walletRepository.findById(customerId)
                .orElseThrow(() -> new AppException(ErrorCode.WALLET_NOT_FOUND_IN_DB));

        if (customerWallet.getBalance() < amount) {
            throw new AppException(ErrorCode.INSUFFICIENT_BALANCE);
        }
        customerWallet.setBalance(customerWallet.getBalance() - amount);
        walletRepository.save(customerWallet);

        Account adminAccount = accountRepository.findByRoleId(ERoleName.ADMIN.getId());
        if (adminAccount == null) {
            throw new AppException(ErrorCode.ACCOUNT_NOT_FOUND_IN_DB);
        }
        Wallet adminWallet = walletRepository.findById(adminAccount.getId())
                .orElseThrow(() -> new AppException(ErrorCode.WALLET_NOT_FOUND_IN_DB));

        adminWallet.setBalance(adminWallet.getBalance() + amount);
        walletRepository.save(adminWallet);

        log.info("Escrow transfer: {} from {} to {}", amount, customerId, adminAccount.getId());
    }

    @Override
    public void executeReleaseEscrow(String sellerId, Long amount) {
        Account adminAccount = accountRepository.findByRoleId(ERoleName.ADMIN.getId());
        if (adminAccount == null) {
            throw new AppException(ErrorCode.ACCOUNT_NOT_FOUND_IN_DB);
        }

        Wallet adminWallet = walletRepository.findById(adminAccount.getId())
                .orElseThrow(() -> new AppException(ErrorCode.WALLET_NOT_FOUND_IN_DB));

        Wallet sellerWallet = walletRepository.findById(sellerId)
                .orElseThrow(() -> new AppException(ErrorCode.WALLET_NOT_FOUND_IN_DB));

        if (adminWallet.getBalance() < amount) {
            throw new AppException(ErrorCode.INSUFFICIENT_BALANCE);
        }

        adminWallet.setBalance(adminWallet.getBalance() - amount);
        sellerWallet.setBalance(sellerWallet.getBalance() + amount);

        walletRepository.save(adminWallet);
        walletRepository.save(sellerWallet);

        log.info("Release Escrow: {} from Admin {} to Seller {}", amount, adminAccount.getId(), sellerId);
    }
    @Override
    public void executeDeposit(String accountId, Long amount) {
        Wallet wallet = walletRepository.findById(accountId)
                .orElseThrow(() -> new AppException(ErrorCode.WALLET_NOT_FOUND_IN_DB));

        wallet.setBalance(wallet.getBalance() + amount);
        walletRepository.save(wallet);

        log.info("Deposit successful: +{} for account {}", amount, accountId);
    }

    @Override
    public void executeWithdraw(String accountId, Long amount) {
        Wallet wallet = walletRepository.findById(accountId)
                .orElseThrow(() -> new AppException(ErrorCode.WALLET_NOT_FOUND_IN_DB));

        if (wallet.getBalance() < amount) {
            throw new AppException(ErrorCode.INSUFFICIENT_BALANCE);
        }

        wallet.setBalance(wallet.getBalance() - amount);
        walletRepository.save(wallet);

        log.info("Withdraw successful: -{} for account {}", amount, accountId);
    }
    public TransactionPaymentURLResponse createDepositUrl(Long amount, HttpServletRequest request) {
        log.info("Khởi tạo nạp tiền: {} VND", amount);

        // 1. Lấy thông tin tài khoản hiện tại
        String accountId = SecurityUtil.getCurrentAccountId();
        Wallet wallet = walletRepository.findById(accountId)
                .orElseThrow(() -> new AppException(ErrorCode.WALLET_NOT_FOUND_IN_DB));

        // 2. Lưu giao dịch vào database với trạng thái PROCESSING
        Transaction transaction = Transaction.builder()
                .amount(amount)
                .wallet(wallet)
                .transactionType(ETransactionType.DEPOSIT)
                .paymentStatus(EPaymentStatus.PENDING)
                .build();

        transaction = transactionRepository.save(transaction);

        redisUtil.cacheProcessingTransaction(transaction.getId());

        // 4. Chuẩn bị Request cho PaymentService (VNPay)
        String ipAddress = request.getRemoteAddr();
        // Nếu chạy qua Proxy/Load Balancer thì dùng: request.getHeader("X-FORWARDED-FOR")

        InitPaymentRequest initPaymentRequest = InitPaymentRequest.builder()
                .transactionType(ETransactionType.DEPOSIT)
                .userId(accountId)
                .requestId(transaction.getId()) // ID giao dịch dùng làm vnp_TxnRef
                .amount(amount)
                .ipAddress(ipAddress)
                .txnRef(transaction.getId())
                .build();

        // 5. Gọi PaymentService để lấy Link VNPay
        InitPaymentResponse initPaymentResponse = paymentService.initPayment(initPaymentRequest);

        return TransactionPaymentURLResponse.builder()
                .transactionResponse(transactionMapper.toTransactionResponse(transaction))
                .payment(initPaymentResponse) // Chứa thuộc tính paymentUrl
                .build();
    }
    public TransactionResponse processVnpayCallback(Map<String, String> params) {
        log.info("Xử lý VNPay Callback với params: {}", params);

        // 1. Lấy mã giao dịch từ params
        String transactionId = params.get("vnp_TxnRef");

        // 2. Sử dụng IpnHandler để verify chữ ký và dữ liệu
        IpnResponse ipnResponse = ipnHandler.process(params);

        // 3. Tìm giao dịch trong database
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new AppException(ErrorCode.TRANSACTION_NOT_FOUND_IN_DB));

        // 4. Kiểm tra xem giao dịch đã được xử lý chưa (tránh cộng tiền 2 lần)
        if (transaction.getPaymentStatus() == EPaymentStatus.SUCCESS) {
            return transactionMapper.toTransactionResponse(transaction);
        }

        // 5. Nếu VNPay báo thành công (ResponseCode = 00)
        if (VNPayIPNResponseConst.SUCCESS.getResponseCode().equals(ipnResponse.getResponseCode())) {

            // Cập nhật số dư ví
            Wallet wallet = transaction.getWallet();
            wallet.setBalance(wallet.getBalance() + transaction.getAmount());
            walletRepository.save(wallet);

            // Cập nhật trạng thái thành công
            transaction.setPaymentStatus(EPaymentStatus.SUCCESS);

            // Xóa cache Redis
            redisUtil.removeCacheProcessingTransaction(transaction.getId());

            log.info("Nạp tiền thành công cho tài khoản: {}", wallet.getId());
        } else {
            // Nếu thất bại hoặc hủy giao dịch
            transaction.setPaymentStatus(EPaymentStatus.FAILED);
            log.warn("Nạp tiền thất bại cho giao dịch: {}", transactionId);
        }

        return transactionMapper.toTransactionResponse(transactionRepository.save(transaction));
    }
}
