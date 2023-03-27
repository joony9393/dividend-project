package com.dayone.exception.impl;
import com.dayone.exception.AbstractException;
import org.springframework.http.HttpStatus;

// 회사가 이미 존재하는 경우
public class AlreadyExistCompanyException extends AbstractException {
    @Override
    public int getStatusCode() {
        return HttpStatus.BAD_REQUEST.value();
    }
    @Override
    public String getMessage() {
        return "이미 보유하고 있는 회사명입니다.";
    }

}
