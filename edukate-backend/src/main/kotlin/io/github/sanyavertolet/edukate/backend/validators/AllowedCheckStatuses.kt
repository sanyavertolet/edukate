package io.github.sanyavertolet.edukate.backend.validators

import io.github.sanyavertolet.edukate.common.checks.CheckStatus
import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import jakarta.validation.Payload
import kotlin.reflect.KClass

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [AllowedCheckStatusesValidator::class])
annotation class AllowedCheckStatuses(
    val values: Array<CheckStatus>,
    val message: String = "",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = [],
)

class AllowedCheckStatusesValidator : ConstraintValidator<AllowedCheckStatuses, CheckStatus> {
    private lateinit var allowed: Set<CheckStatus>

    override fun initialize(constraint: AllowedCheckStatuses) {
        allowed = constraint.values.toSet()
    }

    override fun isValid(value: CheckStatus?, ctx: ConstraintValidatorContext): Boolean {
        if (value == null || value in allowed) return true
        ctx.disableDefaultConstraintViolation()
        ctx.buildConstraintViolationWithTemplate("must be one of: ${allowed.joinToString(", ")}").addConstraintViolation()
        return false
    }
}
