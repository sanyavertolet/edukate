package io.github.sanyavertolet.edukate.backend

import io.github.sanyavertolet.edukate.backend.dtos.CreateSubmissionRequest
import io.github.sanyavertolet.edukate.backend.dtos.Result
import io.github.sanyavertolet.edukate.backend.entities.Bundle
import io.github.sanyavertolet.edukate.backend.entities.CheckResult
import io.github.sanyavertolet.edukate.backend.entities.Problem
import io.github.sanyavertolet.edukate.backend.entities.Submission
import io.github.sanyavertolet.edukate.backend.entities.User
import io.github.sanyavertolet.edukate.backend.entities.UserProblemStatus
import io.github.sanyavertolet.edukate.common.SubmissionStatus
import io.github.sanyavertolet.edukate.common.checks.CheckErrorType
import io.github.sanyavertolet.edukate.common.checks.CheckResultMessage
import io.github.sanyavertolet.edukate.common.checks.CheckStatus
import io.github.sanyavertolet.edukate.common.users.EdukateUserDetails
import io.github.sanyavertolet.edukate.common.users.UserRole
import io.github.sanyavertolet.edukate.common.users.UserStatus
import java.time.Instant
import org.springframework.security.core.Authentication

object BackendFixtures {
    fun bundle(
        id: String? = "bundle-1",
        name: String = "Test Bundle",
        description: String = "Test Description",
        isPublic: Boolean = false,
        problemIds: List<String> = listOf("1.0.0"),
        userIdRoleMap: Map<String, UserRole> = mapOf("admin-1" to UserRole.ADMIN),
        invitedUserIds: Set<String> = emptySet(),
        shareCode: String = "SHARE123",
    ) =
        Bundle(
            id = id,
            name = name,
            description = description,
            isPublic = isPublic,
            problemIds = problemIds,
            userIdRoleMap = userIdRoleMap,
            invitedUserIds = invitedUserIds,
            shareCode = shareCode,
        )

    fun submission(
        id: String? = "sub-1",
        problemId: String = "1.0.0",
        userId: String = "user-1",
        status: SubmissionStatus = SubmissionStatus.PENDING,
        fileObjectIds: List<String> = emptyList(),
        createdAt: Instant? = Instant.now(),
    ) =
        Submission(
            id = id,
            problemId = problemId,
            userId = userId,
            status = status,
            fileObjectIds = fileObjectIds,
            createdAt = createdAt,
        )

    fun checkResult(
        id: String? = "cr-1",
        submissionId: String = "sub-1",
        status: CheckStatus = CheckStatus.SUCCESS,
        trustLevel: Float = 0.9f,
        errorType: CheckErrorType = CheckErrorType.NONE,
        explanation: String = "Correct",
        createdAt: Instant? = Instant.now(),
    ) =
        CheckResult(
            id = id,
            submissionId = submissionId,
            status = status,
            trustLevel = trustLevel,
            errorType = errorType,
            explanation = explanation,
            createdAt = createdAt,
        )

    fun checkResultMessage(
        submissionId: String = "sub-1",
        status: CheckStatus = CheckStatus.SUCCESS,
        trustLevel: Float = 0.85f,
        errorType: CheckErrorType = CheckErrorType.NONE,
        explanation: String = "Looks good",
    ) =
        CheckResultMessage(
            submissionId = submissionId,
            status = status,
            trustLevel = trustLevel,
            errorType = errorType,
            explanation = explanation,
        )

    fun user(
        id: String? = "user-1",
        name: String = "testuser",
        email: String = "test@example.com",
        token: String = "token",
        roles: Set<UserRole> = setOf(UserRole.USER),
        status: UserStatus = UserStatus.ACTIVE,
    ) = User(id = id, name = name, email = email, token = token, roles = roles, status = status)

    fun mockAuthentication(userId: String = "user-1", username: String = "testuser"): Authentication {
        val userDetails = EdukateUserDetails(userId, username, emptySet(), UserStatus.ACTIVE, "token")
        return userDetails.toPreAuthenticatedAuthenticationToken()
    }

    fun createSubmissionRequest(problemId: String = "1.0.0", fileNames: List<String> = listOf("solution.txt")) =
        CreateSubmissionRequest(problemId = problemId, fileNames = fileNames)

    fun problem(
        id: String = "1.0.0",
        isHard: Boolean = false,
        tags: List<String> = emptyList(),
        text: String = "Test problem text",
        subtasks: List<Problem.Subtask> = emptyList(),
        images: List<String> = emptyList(),
        result: Result? = null,
    ) = Problem(id = id, isHard = isHard, tags = tags, text = text, subtasks = subtasks, images = images, result = result)

    fun userProblemStatus(
        userId: String = "user-1",
        problemId: String = "1.0.0",
        bestStatus: SubmissionStatus = SubmissionStatus.SUCCESS,
        latestStatus: SubmissionStatus = bestStatus,
    ) =
        UserProblemStatus(
            userId = userId,
            problemId = problemId,
            latestStatus = latestStatus,
            latestTime = Instant.now(),
            latestSubmissionId = "sub-1",
            bestStatus = bestStatus,
            bestTime = Instant.now(),
            bestSubmissionId = "sub-1",
        )
}
