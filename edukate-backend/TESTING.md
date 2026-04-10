# edukate-backend: Testing

Core unit, slice, and repository tests are implemented. Integration tests (full `@SpringBootTest` + Testcontainers) are
planned but not yet written. This document covers both implemented tests and the integration test plan.

## Infrastructure

- **Unit tests**: `StepVerifier` from `reactor-test` for reactive chains. Mock dependencies with MockK.
- **Integration tests**: `@SpringBootTest` + `WebTestClient`. Use Testcontainers for MongoDB and MinIO. Use an
  embedded/mock RabbitMQ.
- **Notifier**: Wire `NoopNotifier` (from `edukate-common`) in test profiles.
- **Storage**: Use a Testcontainers MinIO instance or an in-memory `Storage` stub.
- **Authentication**: Use `WithMockUser` or build a `TestSecurityContextHolder` with a known user JWT.

---

## Unit Tests

### `SemVerUtilsTest`

| Method                     | What it tests                            |
|----------------------------|------------------------------------------|
| `parseValidVersion`        | `"1.2.3"` → `SemVer(1, 2, 3)`            |
| `parseZeroVersion`         | `"0.0.0"` → `SemVer(0, 0, 0)`            |
| `parseLargeNumbers`        | `"100.200.300"` → correct values         |
| `parseSingleSegmentThrows` | `"1"` → `IllegalArgumentException`       |
| `parseTwoSegmentsThrows`   | `"1.2"` → `IllegalArgumentException`     |
| `parseFourSegmentsThrows`  | `"1.2.3.4"` → `IllegalArgumentException` |
| `parseNonNumericThrows`    | `"a.b.c"` → `NumberFormatException`      |

---

### `ShareCodeGeneratorTest`

| Method                        | What it tests                                                 |
|-------------------------------|---------------------------------------------------------------|
| `generatedCodeHasLength10`    | Result is exactly 10 characters                               |
| `generatedCodeIsAlphanumeric` | All characters in `[A-Za-z0-9]`                               |
| `generatedCodesAreUnique`     | Two consecutive calls produce different codes (probabilistic) |

---

### `BundlePermissionEvaluatorTest`

Uses a `Bundle` instance built with known `userIdRoleMap`.

| Method                                              | What it tests                               |
|-----------------------------------------------------|---------------------------------------------|
| `hasRoleReturnsTrueWhenUserIsAdmin`                 | ADMIN satisfies ADMIN requirement           |
| `hasRoleReturnsTrueWhenUserIsModerator`             | MODERATOR satisfies MODERATOR requirement   |
| `hasRoleReturnsTrueWhenAdminCheckedAsModerator`     | ADMIN satisfies MODERATOR requirement       |
| `hasRoleReturnsFalseWhenUserIsUser`                 | USER does not satisfy MODERATOR requirement |
| `hasRoleReturnsFalseWhenUserNotInBundle`            | Unknown userId → `false`                    |
| `hasRoleHigherThanReturnsTrueForAdmin`              | ADMIN is higher than MODERATOR              |
| `hasRoleHigherThanReturnsFalseForSameRole`          | MODERATOR is not higher than MODERATOR      |
| `hasInvitePermissionGrantedToModerator`             | MODERATOR can invite                        |
| `hasInvitePermissionDeniedToUser`                   | USER cannot invite                          |
| `hasJoinPermissionPublicBundle`                     | Public bundle → `true` for any user         |
| `hasJoinPermissionPrivateBundleInvited`             | Private bundle + invited user → `true`      |
| `hasJoinPermissionPrivateBundleNotInvited`          | Private bundle + unknown user → `false`     |
| `hasChangeRolePermissionAdminCanDemoteModerator`    | ADMIN can change MODERATOR → USER           |
| `hasChangeRolePermissionModeratorCannotChangeAdmin` | MODERATOR cannot touch ADMIN                |
| `hasChangeRolePermissionCannotSetHigherThanSelf`    | MODERATOR cannot promote another to ADMIN   |

---

### `SubmissionPermissionEvaluatorTest`

| Method                            | What it tests                   |
|-----------------------------------|---------------------------------|
| `isOwnerReturnsTrueForOwner`      | Submission owner matches userId |
| `isOwnerReturnsFalseForOtherUser` | Different userId → `false`      |

---

### `ProblemStatusDecisionManagerTest`

Mock `UserProblemStatusRepository`.

| Method                                     | What it tests                                                  |
|--------------------------------------------|----------------------------------------------------------------|
| `getStatusReturnsSolvedWhenBestIsSuccess`  | `bestStatus = SUCCESS` → `Problem.Status.SOLVED`               |
| `getStatusReturnsFailedWhenBestIsFailed`   | `bestStatus = FAILED` → `Problem.Status.FAILED`                |
| `getStatusReturnsSolvingWhenBestIsPending` | `bestStatus = PENDING` → `Problem.Status.SOLVING`              |
| `getStatusReturnsNotSolvedWhenNoRecord`    | No `UserProblemStatus` found → `NOT_SOLVED`                    |
| `getStatusWithAuthNullReturnsNotSolved`    | Unauthenticated (null auth) → `NOT_SOLVED`                     |
| `getStatusWithAuthDelegatesCorrectly`      | Authenticated call resolves userId and delegates to repository |

---

### `UserServiceTest`

Mock `UserRepository` and `Notifier`.

| Method                                            | What it tests                                           |
|---------------------------------------------------|---------------------------------------------------------|
| `findUserByAuthenticationReturnsUser`             | Valid authentication resolves user from repo            |
| `findUserByNameReturnsUser`                       | Found user returned                                     |
| `findUserByNameEmptyPropagatesEmpty`              | Repo returns empty → propagated                         |
| `findUserNameReturnsName`                         | User found → name returned                              |
| `findUserNameReturnsUnknownForMissingUser`        | User not found → `"UNKNOWN"` default                    |
| `hasUserPermissionToSubmitReturnsTrueForActive`   | `status = ACTIVE` → `true`                              |
| `hasUserPermissionToSubmitReturnsFalseForPending` | `status = PENDING` → `false`                            |
| `saveUserDelegatesToRepository`                   | Calls `userRepository.save(user)`                       |
| `deleteUserByIdDelegatesToRepository`             | Calls `userRepository.deleteById(id)`                   |
| `notifyAllUsersWithStatusPublishesToNotifier`     | Finds users by status, calls `notifier.notify` for each |

---

### `ProblemServiceTest`

Mock `ProblemRepository`, `FileManager`, `ProblemStatusDecisionManager`.

| Method                                           | What it tests                                                  |
|--------------------------------------------------|----------------------------------------------------------------|
| `getFilteredProblemsAppliesSemVerSort`           | Calls `findAll` with semver sort                               |
| `findProblemByIdReturnsProblem`                  | Found → returned                                               |
| `findProblemsByIdsReturnsAll`                    | Delegates to `findProblemsByIdIn`                              |
| `updateProblemSavesToRepo`                       | Calls `problemRepository.save(problem)`                        |
| `updateProblemBatchSavesAll`                     | Each problem saved                                             |
| `countProblemsReturnsCount`                      | Delegates to `problemRepository.count()`                       |
| `deleteProblemByIdDelegates`                     | Calls `deleteById`                                             |
| `getProblemIdsByPrefixReturnsMappedIds`          | Maps `Problem` stream to `id` strings                          |
| `getRandomUnsolvedProblemIdPrefersUnsolved`      | If authenticated and unsolved exists → returns it              |
| `getRandomUnsolvedProblemIdFallsBackToRandom`    | If no unsolved found → `findRandomProblemId`                   |
| `getRandomUnsolvedProblemIdUnauthenticated`      | Null auth → falls back to random                               |
| `problemImageDownloadUrlsGeneratesPresignedUrls` | Maps image filenames to presigned URLs via `FileManager`       |
| `prepareDtoZipsStatusAndImages`                  | Status from decision manager + URLs combined into `ProblemDto` |
| `prepareMetadataAppliesStatus`                   | Status from decision manager applied to `ProblemMetadata`      |

---

### `BundleServiceTest`

Mock all dependencies.

| Method                                                | What it tests                                         |
|-------------------------------------------------------|-------------------------------------------------------|
| `findBundleByShareCodeReturnsBundle`                  | Bundle found → returned                               |
| `findBundleByShareCodeThrowsNotFound`                 | Repo returns empty → 404                              |
| `getOwnedBundlesFiltersAdminRole`                     | Calls `findBundlesByUserRoleIn` with `[ADMIN]`        |
| `getJoinedBundlesFiltersAnyRole`                      | Calls `findBundlesByUserRoleIn` with all roles        |
| `getPublicBundlesFiltersIsPublicTrue`                 | Calls `findBundlesByIsPublic(true, ...)`              |
| `createBundleAssignsCreatorAsAdmin`                   | Creator's userId appears as ADMIN in saved bundle     |
| `joinUserPublicBundle`                                | Public bundle → user added with USER role             |
| `joinUserInvitedPrivateBundle`                        | Private bundle + invited → user added                 |
| `joinUserNotInvitedPrivateThrowsForbidden`            | Not invited → 403                                     |
| `joinUserAlreadyInBundleThrowsBadRequest`             | Already member → 400                                  |
| `removeUserSuccess`                                   | User removed from bundle                              |
| `removeUserNotInBundleThrowsBadRequest`               | Not a member → 400                                    |
| `removeLastAdminThrowsBadRequest`                     | Single admin cannot leave → 400                       |
| `inviteUserSuccess`                                   | Invited user added to `invitedUserIds`                |
| `inviteUserAlreadyMemberThrowsBadRequest`             | Already in bundle → 400                               |
| `inviteUserByNonModeratorThrowsForbidden`             | Non-moderator → 403                                   |
| `expireInviteSuccess`                                 | Invite cancelled                                      |
| `expireInviteUserNotInvitedThrowsBadRequest`          | Not invited → 400                                     |
| `getBundleUsersRequiresModerator`                     | MODERATOR+ can fetch; USER → empty (filtered out)     |
| `changeUserRoleSuccess`                               | Role updated, new role returned                       |
| `changeUserRoleTargetNotInBundleThrowsNotFound`       | Target userId not in bundle → 404                     |
| `changeUserRoleInsufficientPermissionThrowsForbidden` | Requester cannot promote → 403                        |
| `declineInviteSuccess`                                | Invited user declines                                 |
| `declineInviteNotInvitedThrowsForbidden`              | Not invited → 403                                     |
| `changeVisibilityRequiresModerator`                   | Non-moderator → empty (filtered)                      |
| `changeVisibilityUpdatesBundle`                       | `isPublic` flipped and saved                          |
| `changeProblemsEmptyListThrowsBadRequest`             | Empty list → 400                                      |
| `changeProblemsSuccess`                               | Problem IDs updated                                   |
| `prepareDtoCollectsProblemsAndAdmins`                 | Builds `BundleDto` with metadata list and admin names |
| `prepareMetadataCollectsAdmins`                       | Builds `BundleMetadata` with admin names              |

---

### `SubmissionServiceTest`

Mock all dependencies.

| Method                                      | What it tests                                                        |
|---------------------------------------------|----------------------------------------------------------------------|
| `saveSubmissionCreatesEntityAndMovesFiles`  | `Submission.of` created, files moved, file IDs resolved              |
| `saveSubmissionWithAuthExtractionDelegates` | Overload with `Authentication` resolves userId                       |
| `updateDelegatesToRepository`               | Calls `submissionRepository.save(submission)`                        |
| `findByIdReturnsSubmission`                 | Found → returned                                                     |
| `findSubmissionsByProblemIdAndUserId`       | Delegates to repo                                                    |
| `findUserSubmissionsWithProblemId`          | Calls `findAllByProblemIdAndUserId`                                  |
| `findUserSubmissionsWithoutProblemId`       | Calls `findAllByUserId`                                              |
| `findSubmissionsByStatusIn`                 | Delegates to repo                                                    |
| `getSubmissionIfOwnsSuccess`                | Owner matches → submission returned                                  |
| `getSubmissionIfOwnsNotFound`               | Repo returns empty → 404                                             |
| `getSubmissionIfOwnsNotOwner`               | Non-owner → 403                                                      |
| `prepareDtoCollectsFileUrlsAndUserName`     | File keys → presigned URLs, userId → name                            |
| `prepareContextBuildsSubmissionContext`     | Problem text + image keys + submission file keys assembled correctly |

---

### `CheckResultServiceTest`

Mock `CheckResultRepository` and `SubmissionService`.

| Method                                            | What it tests                                                   |
|---------------------------------------------------|-----------------------------------------------------------------|
| `saveAndUpdateSubmissionPersistsCheckResult`      | `checkResultRepository.save` called with the input              |
| `saveAndUpdateSubmissionUpdatesSubmissionStatus`  | `submissionService.update` called with best status              |
| `saveAndUpdateSubmissionBestStatusLogic`          | Existing FAILED + incoming SUCCESS → submission becomes SUCCESS |
| `saveAndUpdateSubmissionNoBestStatus`             | Existing PENDING + incoming FAILED → submission becomes FAILED  |
| `saveAndUpdateSubmissionSubmissionNotFoundThrows` | Repo returns empty → 404                                        |
| `findByIdDelegatesToRepository`                   | Returns check result by ID                                      |
| `findAllBySubmissionIdReturnsSortedDesc`          | Calls `findBySubmissionId` with DESC sort on `createdAt`        |

---

### `FileManagerTest`

Mock `FileObjectRepository` and `FileKeyStorage`.

| Method                                       | What it tests                                                       |
|----------------------------------------------|---------------------------------------------------------------------|
| `getFileObjectReturnsFromRepo`               | Finds `FileObject` by key path                                      |
| `getFileObjectTimesOut`                      | Timeout behavior when repo hangs                                    |
| `getFileContentDownloadsFromStorage`         | Delegates to `storage.getContent(key)`                              |
| `getPresignedUrlDelegatesToStorage`          | Delegates to `storage.generatePresignedUrl(key)`                    |
| `uploadFileSavesMetadataToDb`                | After upload, `saveOrUpdateByKeyPath` creates `FileObject`          |
| `uploadFileUpdatesExistingFileObject`        | If `FileObject` already exists, it is updated                       |
| `deleteFileBothStorageAndDb`                 | Both deleted → returns `true`                                       |
| `deleteFileStorageFailsContinuesToDb`        | Storage error → DB still cleaned, `false` returned for storage part |
| `deleteFileNoRecordIsIdempotent`             | Neither deleted → returns `false`                                   |
| `doesFileExistReturnsTrueWhenMetadataExists` | `storage.metadata` succeeds → `true`                                |
| `doesFileExistReturnsFalseWhenEmpty`         | `storage.metadata` returns empty → `false`                          |
| `doFilesExistAllExist`                       | All keys found → `true`                                             |
| `doFilesExistOneMissing`                     | One key missing → `false`                                           |
| `moveFileUpdatesKeyPath`                     | Old key path replaced with new key path in `FileObject`             |
| `moveFileStorageFailurePropagates`           | `storage.move` returns false → `IllegalStateException`              |
| `listFileMetadataWithPrefixMapsToDto`        | `FileObject` stream mapped to `FileMetadata` with correct fields    |

---

### `ResultServiceTest`

Mock `ProblemService` and `FileManager`.

| Method                                     | What it tests                                                                 |
|--------------------------------------------|-------------------------------------------------------------------------------|
| `updateResultSavesToProblem`               | Calls `problemService.updateProblem` with embedded result, returns problem ID |
| `updateResultBatchSavesAll`                | Batch processes each result                                                   |
| `findResultByIdReturnsResultWithImageUrls` | Problem found, result extracted, image presigned URLs added                   |
| `findResultByIdProblemNotFoundPropagates`  | Problem not found → empty/error propagated                                    |

---

### `ProblemBeforeSaveListenerTest`

| Method                                 | What it tests                                                            |
|----------------------------------------|--------------------------------------------------------------------------|
| `onBeforeSaveParsesAndPopulatesFields` | Document receives `majorId`, `minorId`, `patchId` fields from problem ID |
| `onBeforeSaveInvalidIdThrows`          | Non-semver ID → `IllegalArgumentException`                               |

---

### `SubmissionAfterSaveListenerTest`

Mock `ReactiveMongoTemplate` to capture the update document.

| Method                                       | What it tests                                            |
|----------------------------------------------|----------------------------------------------------------|
| `onAfterSaveUpsertsNewRecord`                | Correct filter `{userId, problemId}` and pipeline stages |
| `onAfterSaveSetsPendingRankZero`             | PENDING status → `newRank = 0` in pipeline               |
| `onAfterSaveSetsSucessRankTwo`               | SUCCESS status → `newRank = 2` in pipeline               |
| `onAfterSaveUsesCreatedAtFromSubmission`     | `createdAt` field from submission used when present      |
| `onAfterSaveFallsBackToNowWhenCreatedAtNull` | Null `createdAt` falls back to `Instant.now()`           |

---

## Integration Tests

Integration tests use `@SpringBootTest(webEnvironment = RANDOM_PORT)`, Testcontainers MongoDB, a mock `FileKeyStorage`,
and `NoopNotifier`.

---

### `ProblemControllerIntegrationTest`

| Method                                         | What it tests                                              |
|------------------------------------------------|------------------------------------------------------------|
| `getProblemsReturnsPagedList`                  | `GET /api/v1/problems` → 200 with list                     |
| `getProblemsReturnsSortedBySemVer`             | Seeded problems returned in semver order                   |
| `getProblemByIdReturnsDto`                     | `GET /api/v1/problems/1.0.0` → full DTO                    |
| `getProblemByIdNotFound`                       | Unknown ID → 404                                           |
| `countProblemsReturnsTotal`                    | `GET /api/v1/problems/count` → correct count               |
| `getProblemsByPrefixFiltersCorrectly`          | `GET /api/v1/problems/by-prefix?prefix=1.` → matching IDs  |
| `getRandomProblemReturnsAProblem`              | `GET /api/v1/problems/random` → valid problem ID           |
| `getRandomProblemAuthenticatedPrefersUnsolved` | Authenticated user with solved problems gets different IDs |

---

### `BundleControllerIntegrationTest`

| Method                                   | What it tests                                                      |
|------------------------------------------|--------------------------------------------------------------------|
| `createBundleSuccess`                    | `POST /api/v1/bundles` → 200, bundle created with creator as ADMIN |
| `createBundleUnauthenticated`            | `POST /api/v1/bundles` without auth → 401                          |
| `createBundleMissingNameValidation`      | Blank `name` → 400                                                 |
| `getOwnedBundlesReturnsOnlyOwned`        | Only bundles where user is ADMIN                                   |
| `getJoinedBundlesReturnsAllMemberships`  | Bundles where user has any role                                    |
| `getPublicBundlesRequiresNoAuth`         | Public bundles visible without auth                                |
| `getBundleByShareCodeRequiresMembership` | Non-member → 403 or empty                                          |
| `joinPublicBundle`                       | Join public bundle → user in `userIdRoleMap`                       |
| `joinAlreadyJoinedThrowsBadRequest`      | Duplicate join → 400                                               |
| `leaveBundle`                            | Leave removes user from bundle                                     |
| `leaveLastAdminThrowsBadRequest`         | Last admin leave → 400                                             |
| `inviteUserRequiresModerator`            | USER cannot invite → 403                                           |
| `inviteUserSuccess`                      | Invitee added to `invitedUserIds`                                  |
| `replyInviteAccept`                      | Accept adds invitee as USER                                        |
| `replyInviteDecline`                     | Decline removes from `invitedUserIds`                              |
| `changeProblemsSuccess`                  | Problem IDs updated by moderator                                   |
| `changeVisibilitySuccess`                | `isPublic` toggled by moderator                                    |
| `changeUserRoleSuccess`                  | Admin promotes/demotes member                                      |

---

### `SubmissionControllerIntegrationTest`

| Method                                            | What it tests                                                      |
|---------------------------------------------------|--------------------------------------------------------------------|
| `createSubmissionSuccess`                         | `POST /api/v1/submissions` with valid request → Submission created |
| `createSubmissionUnauthenticated`                 | No auth → 401                                                      |
| `createSubmissionInactiveUserForbidden`           | PENDING user → 403                                                 |
| `getSubmissionByIdOwnerAccess`                    | Owner can fetch submission                                         |
| `getSubmissionByIdNonOwnerForbidden`              | Non-owner → 403                                                    |
| `getMySubmissionsFiltered`                        | Paginated list for authenticated user                              |
| `getMySubmissionsFilteredByProblem`               | `?problemId=X` filters correctly                                   |
| `getAllSubmissionsRequiresModerator`              | `GET /api/v1/submissions/all` → 403 for USER role                  |
| `getSubmissionsByProblemAndUserRequiresModerator` | `GET /api/v1/submissions/{problemId}/{username}` → 403 for USER    |

---

### `CheckerControllerIntegrationTest`

| Method                          | What it tests                                                                   |
|---------------------------------|---------------------------------------------------------------------------------|
| `aiCheckSchedulesCheck`         | `POST /api/v1/checker/ai?id=X` → message published to RabbitMQ (verify via spy) |
| `aiCheckNonOwnerForbidden`      | Non-owner submission → 403                                                      |
| `selfCheckCreatesCheckResult`   | `POST /api/v1/checker/self?id=X` → `CheckResult` with SELF type saved           |
| `supervisorCheckReturns501`     | `POST /api/v1/checker/supervisor` → 501                                         |
| `getCheckResultByIdOwnerAccess` | Owner can fetch result                                                          |
| `getCheckResultsBySubmissionId` | Returns list sorted by `createdAt` DESC                                         |

---

### `TempFileControllerIntegrationTest`

| Method                         | What it tests                                                  |
|--------------------------------|----------------------------------------------------------------|
| `uploadTempFileSuccess`        | `POST /api/v1/files/temp` → file stored, metadata returned     |
| `deleteTempFileSuccess`        | `DELETE /api/v1/files/temp?fileName=X` → file removed          |
| `deleteTempFileNotFound`       | Non-existent file → 404 or 200 (idempotent)                    |
| `downloadTempFile`             | `GET /api/v1/files/temp/get?fileName=X` → byte stream returned |
| `listTempFilesReturnsOwnFiles` | `GET /api/v1/files/temp` → only files owned by current user    |

---

### `UserControllerIntegrationTest`

| Method                     | What it tests                                                 |
|----------------------------|---------------------------------------------------------------|
| `whoamiReturnsCurrentUser` | `GET /api/v1/users/whoami` → `UserDto` for authenticated user |
| `whoamiUnauthenticated`    | No auth → 401                                                 |

---

### `ResultControllerIntegrationTest`

| Method                  | What it tests                                            |
|-------------------------|----------------------------------------------------------|
| `getResultByIdSuccess`  | `GET /api/v1/results/{id}` → full result with image URLs |
| `getResultByIdNotFound` | Unknown problem ID → 404                                 |

---

### Internal Controllers: `ProblemInternalControllerIntegrationTest`

| Method                    | What it tests                                        |
|---------------------------|------------------------------------------------------|
| `saveProblemSuccess`      | `POST /internal/problems` → Problem saved            |
| `saveProblemBatchSuccess` | `POST /internal/problems/batch` → all problems saved |
| `deleteProblemSuccess`    | `DELETE /internal/problems/{id}` → problem removed   |

---

### Internal Controllers: `UserInternalControllerIntegrationTest`

| Method                      | What it tests                                 |
|-----------------------------|-----------------------------------------------|
| `createOrUpdateUserSuccess` | `POST /internal/users` → user upserted        |
| `getUserByNameReturnsUser`  | `GET /internal/users/by-name/{name}` → found  |
| `getUserByIdReturnsUser`    | `GET /internal/users/by-id/{id}` → found      |
| `deleteUserByIdRemovesUser` | `DELETE /internal/users/by-id/{id}` → removed |

---

### `CheckResultMessageListenerIntegrationTest`

| Method                                                   | What it tests                                                        |
|----------------------------------------------------------|----------------------------------------------------------------------|
| `onCheckResultMessagePersistsResultAndUpdatesSubmission` | Message received → `CheckResult` saved + `Submission` status updated |
| `onCheckResultMessageSendsNotification`                  | Notification sent to `Notifier` after processing                     |
| `onCheckResultMessageSubmissionNotFoundLogsError`        | Missing submission → error logged, no exception propagated           |

---

## Test Coverage Targets

| Layer                     | Target                                                              |
|---------------------------|---------------------------------------------------------------------|
| Utils                     | 100% — pure logic, trivial to cover fully                           |
| Permissions               | 100% — critical authorization logic                                 |
| Services (unit)           | 90%+ — all happy paths + primary error paths                        |
| Controllers (integration) | All endpoints covered with at least one happy path + one error path |
| Message Listener          | At least 2 scenarios: success and missing submission                |
| Save Listeners            | Both listeners: happy path + edge cases                             |
