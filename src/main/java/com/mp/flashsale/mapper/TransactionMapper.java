package com.mp.flashsale.mapper;

import com.mp.flashsale.dto.request.transaction.TransactionRequest;
import com.mp.flashsale.dto.response.transaction.TransactionResponse;
import com.mp.flashsale.entity.Transaction;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TransactionMapper {
    Transaction toTransaction(TransactionRequest transactionRequest);
    TransactionResponse toTransactionResponse(Transaction transaction);



}
