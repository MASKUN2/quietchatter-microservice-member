package com.quietchatter.member.application

class MemberDeactivatedException(val reactivationToken: String) : 
    RuntimeException("Your account is currently deactivated. Please reactivate your account to use the service.")

class MemberNotFoundException(message: String) : RuntimeException(message)
