package com.chat.api.user

import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext


class NotSpaceValidator : ConstraintValidator<NotSpace, String> {
    override fun isValid(
        value: String?,
        context: ConstraintValidatorContext?
    ): Boolean {
        if (value == null) {
            return true
        }
        return hasWhitespace(value) && value.isNotEmpty()
    }

    override fun initialize(constraintAnnotation: NotSpace?) {
        super.initialize(constraintAnnotation)
    }

    fun hasWhitespace(str: String): Boolean {
        return str.none { Character.isWhitespace(it) }
    }
}