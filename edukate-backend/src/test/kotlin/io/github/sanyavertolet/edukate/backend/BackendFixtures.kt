package io.github.sanyavertolet.edukate.backend

import io.github.sanyavertolet.edukate.backend.dtos.CreateSubmissionRequest
import io.github.sanyavertolet.edukate.backend.entities.Answer
import io.github.sanyavertolet.edukate.backend.entities.Book
import io.github.sanyavertolet.edukate.backend.entities.CheckResult
import io.github.sanyavertolet.edukate.backend.entities.Problem
import io.github.sanyavertolet.edukate.backend.entities.ProblemProgress
import io.github.sanyavertolet.edukate.backend.entities.ProblemSet
import io.github.sanyavertolet.edukate.backend.entities.Submission
import io.github.sanyavertolet.edukate.backend.entities.User
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
    fun problemSet(
        id: Long? = 1L,
        name: String = "Test ProblemSet",
        description: String = "Test Description",
        isPublic: Boolean = false,
        userIdRoleMap: Map<Long, UserRole> = mapOf(100L to UserRole.ADMIN),
        invitedUserIds: Set<Long> = emptySet(),
        shareCode: String = "SHARE123",
    ) =
        ProblemSet(
            id = id,
            name = name,
            description = description,
            isPublic = isPublic,
            userIdRoleMap = userIdRoleMap,
            invitedUserIds = invitedUserIds,
            shareCode = shareCode,
        )

    fun submission(
        id: Long? = 1L,
        problemId: Long = 1L,
        userId: Long = 1L,
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
        id: Long? = 1L,
        submissionId: Long = 1L,
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
        submissionId: Long = 1L,
        checkResultId: Long = 10L,
        status: CheckStatus = CheckStatus.SUCCESS,
        trustLevel: Float = 0.85f,
        errorType: CheckErrorType = CheckErrorType.NONE,
        explanation: String = "Looks good",
    ) =
        CheckResultMessage(
            submissionId = submissionId,
            checkResultId = checkResultId,
            status = status,
            trustLevel = trustLevel,
            errorType = errorType,
            explanation = explanation,
        )

    fun user(
        id: Long? = 1L,
        name: String = "testuser",
        email: String = "test@example.com",
        token: String = "token",
        roles: Set<UserRole> = setOf(UserRole.USER),
        status: UserStatus = UserStatus.ACTIVE,
    ) = User(id = id, name = name, email = email, token = token, roles = roles, status = status)

    fun mockAuthentication(
        userId: Long = 1L,
        username: String = "testuser",
        roles: Set<UserRole> = setOf(UserRole.USER),
    ): Authentication {
        val userDetails = EdukateUserDetails(userId, username, roles, UserStatus.ACTIVE, "token")
        return userDetails.toPreAuthenticatedAuthenticationToken()
    }

    fun createSubmissionRequest(problemKey: String = "savchenko/P1", fileNames: List<String> = listOf("solution.txt")) =
        CreateSubmissionRequest(problemKey = problemKey, fileNames = fileNames)

    fun problem(
        id: Long? = 1L,
        bookId: Long = 1L,
        code: String = "1.1.1",
        key: String = "savchenko/$code",
        isHard: Boolean = false,
        tags: List<String> = emptyList(),
        text: String = "Test problem text",
        subtasks: List<Problem.Subtask> = emptyList(),
        images: List<String> = emptyList(),
    ) =
        Problem(
            id = id,
            bookId = bookId,
            code = code,
            key = key,
            isHard = isHard,
            tags = tags,
            text = text,
            subtasks = subtasks,
            images = images,
        )

    fun problemProgress(
        id: Long? = null,
        userId: Long = 1L,
        problemId: Long = 1L,
        bestStatus: SubmissionStatus = SubmissionStatus.SUCCESS,
        latestStatus: SubmissionStatus = bestStatus,
    ) =
        ProblemProgress(
            id = id,
            userId = userId,
            problemId = problemId,
            latestStatus = latestStatus,
            latestTime = Instant.now(),
            latestSubmissionId = 1L,
            bestStatus = bestStatus,
            bestTime = Instant.now(),
            bestSubmissionId = 1L,
        )

    fun book(
        id: Long? = 1L,
        slug: String = "test-book",
        subject: String = "Physics",
        title: String = "Test Book",
        citation: String = "Test Author, Test Book, 2024",
        description: String? = "A test book",
    ) = Book(id = id, slug = slug, subject = subject, title = title, citation = citation, description = description)

    fun answer(
        id: Long? = 1L,
        problemId: Long = 1L,
        text: String = "Answer is 42",
        notes: String? = null,
        images: List<String> = emptyList(),
    ) = Answer(id = id, problemId = problemId, text = text, notes = notes, images = images)
}
