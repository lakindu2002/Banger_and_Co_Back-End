package com.lakindu.bangerandcobackend.serviceinterface;

import com.lakindu.bangerandcobackend.auth.AuthReturnBuilder;
import com.lakindu.bangerandcobackend.dto.AuthRequest;

public interface AuthService {
    //used to define methods for implementation of AuthService
    AuthReturnBuilder performAuthentication(AuthRequest theAuthRequest) throws Exception;
}
