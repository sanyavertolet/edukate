package io.github.sanyavertolet.edukate.backend.storage;

import io.github.sanyavertolet.edukate.backend.entities.Problem;
import io.github.sanyavertolet.edukate.backend.entities.Submission;

public class FileKeys {
    public static String submission(Submission submission, String fileName) {
        return submissionsDir(submission) + fileName;
    }

    public static String problem(Problem problem, String fileName) {
        return problemDir(problem) + fileName;
    }

    public static String avatar(String userName) {
        return userDir(userName) + "avatar";
    }

    public static String result(Problem problem, String fileName) {
        return resultDir(problem) + fileName;
    }

    public static String temp(String userId, String fileName) {
        return tempDir(userId) + fileName;
    }

    public static String problemDir(Problem problem) {
        return "problems/" + problem.getId() + "/";
    }

    public static String resultDir(Problem problem) {
        return "results/" + problem.getId() + "/";
    }

    public static String submissionsDir(Submission submission) {
        return userDir(submission.getUserId()) + "submissions/" + submission.getProblemId() + "/";
    }

    public static String tempDir(String userName) {
        return userDir(userName) + "tmp/";
    }

    private static String userDir(String userName) {
        return "users/" + userName + "/";
    }

    public static String prefixed(String prefix, String fileName) {
        return prefix != null ? prefix + "/" + fileName : fileName;
    }

    public static String fileName(String key) {
        int index = key.lastIndexOf('/');
        return index != -1 ? key.substring(index + 1) : key;
    }
}
