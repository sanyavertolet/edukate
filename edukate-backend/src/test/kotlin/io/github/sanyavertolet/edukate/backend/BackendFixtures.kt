package io.github.sanyavertolet.edukate.backend

import io.github.sanyavertolet.edukate.backend.dtos.CreateSubmissionRequest
import io.github.sanyavertolet.edukate.backend.dtos.CreateSupervisorTicketRequest
import io.github.sanyavertolet.edukate.backend.dtos.SupervisorTicketDto
import io.github.sanyavertolet.edukate.backend.dtos.SupervisorVerdict
import io.github.sanyavertolet.edukate.backend.entities.Answer
import io.github.sanyavertolet.edukate.backend.entities.AnswerLocalization
import io.github.sanyavertolet.edukate.backend.entities.Book
import io.github.sanyavertolet.edukate.backend.entities.CheckResult
import io.github.sanyavertolet.edukate.backend.entities.Problem
import io.github.sanyavertolet.edukate.backend.entities.ProblemLocalization
import io.github.sanyavertolet.edukate.backend.entities.ProblemProgress
import io.github.sanyavertolet.edukate.backend.entities.ProblemSet
import io.github.sanyavertolet.edukate.backend.entities.Submission
import io.github.sanyavertolet.edukate.backend.entities.Subproblem
import io.github.sanyavertolet.edukate.backend.entities.SupervisorTicket
import io.github.sanyavertolet.edukate.backend.entities.User
import io.github.sanyavertolet.edukate.common.ContentLanguage
import io.github.sanyavertolet.edukate.common.SubmissionStatus
import io.github.sanyavertolet.edukate.common.checks.CheckErrorType
import io.github.sanyavertolet.edukate.common.checks.CheckResultMessage
import io.github.sanyavertolet.edukate.common.checks.CheckStatus
import io.github.sanyavertolet.edukate.common.checks.SupervisorTicketStatus
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
        language: ContentLanguage = ContentLanguage.RU,
        fileObjectIds: List<String> = emptyList(),
        createdAt: Instant? = Instant.now(),
        updatedAt: Instant? = Instant.now(),
    ) =
        Submission(
            id = id,
            problemId = problemId,
            userId = userId,
            status = status,
            language = language,
            fileObjectIds = fileObjectIds,
            createdAt = createdAt,
            updatedAt = updatedAt,
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
        val userDetails = EdukateUserDetails(userId, username, roles, UserStatus.ACTIVE, "token", "test@example.com")
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
        images: List<String> = emptyList(),
        createdAt: Instant = Instant.parse("2025-06-01T10:00:00Z"),
    ) = Problem(id = id, bookId = bookId, code = code, key = key, isHard = isHard, images = images, createdAt = createdAt)

    fun problemLocalization(
        problemId: Long = 1L,
        language: ContentLanguage = ContentLanguage.RU,
        text: String = "Test problem text",
        tags: List<String> = emptyList(),
        subproblems: List<Subproblem> = emptyList(),
    ) = ProblemLocalization(problemId = problemId, language = language, text = text, tags = tags, subproblems = subproblems)

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

    fun answer(id: Long? = 1L, problemId: Long = 1L, images: List<String> = emptyList()) =
        Answer(id = id, problemId = problemId, images = images)

    fun answerLocalization(
        answerId: Long = 1L,
        language: ContentLanguage = ContentLanguage.RU,
        text: String = "Answer is 42",
        notes: String? = null,
    ) = AnswerLocalization(answerId = answerId, language = language, text = text, notes = notes)

    fun supervisorTicket(
        id: Long? = 1L,
        submissionId: Long = 1L,
        problemSetId: Long = 1L,
        supervisorId: Long = 2L,
        checkResultId: Long = 10L,
        status: SupervisorTicketStatus = SupervisorTicketStatus.PENDING,
        createdAt: Instant? = Instant.now(),
    ) =
        SupervisorTicket(
            id = id,
            submissionId = submissionId,
            problemSetId = problemSetId,
            supervisorId = supervisorId,
            checkResultId = checkResultId,
            status = status,
            createdAt = createdAt,
        )

    fun createSupervisorTicketRequest(
        submissionId: Long = 1L,
        problemSetCode: String = "SHARE123",
        supervisorName: String = "moderator",
    ) =
        CreateSupervisorTicketRequest(
            submissionId = submissionId,
            problemSetCode = problemSetCode,
            supervisorName = supervisorName,
        )

    fun supervisorTicketDto(
        id: Long = 1L,
        problemKey: String = "savchenko/1.1.1",
        problemSetShareCode: String = "SHARE123",
        fileUrls: List<String> = emptyList(),
        status: SupervisorTicketStatus = SupervisorTicketStatus.PENDING,
        createdAt: Instant = Instant.now(),
    ) =
        SupervisorTicketDto(
            id = id,
            problemKey = problemKey,
            problemSetShareCode = problemSetShareCode,
            fileUrls = fileUrls,
            status = status,
            createdAt = createdAt,
        )

    fun supervisorVerdict(
        status: CheckStatus = CheckStatus.MISTAKE,
        errorType: CheckErrorType = CheckErrorType.CONCEPTUAL,
        explanation: String = "The direction of initial velocity is incorrect.",
    ) = SupervisorVerdict(status = status, errorType = errorType, explanation = explanation)
}
