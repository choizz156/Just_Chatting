package com.chat.api.user

import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext


class NotSpaceValidator() : ConstraintValidator<NotSpace, String> {
    override fun isValid(
        value: String?,
        context: ConstraintValidatorContext?
    ): Boolean {
        return hasWhitespace(value!!) && !value.isEmpty()
    }

    override fun initialize(constraintAnnotation: NotSpace?) {
        super.initialize(constraintAnnotation)
    }

    fun hasWhitespace(str: String): Boolean {

        str.toCharArray().forEach {
            if(Character.isWhitespace(it)) {
                return false
            }
        }
        return true
    }
}