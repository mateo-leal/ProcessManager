package com.mateolegi.git.remote.gitlab;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GitLabRemoteTest {

    @Test
    void getRemoteBranches() {
        var git = new GitLabRemote();
        git.getRemoteBranches("https://git.quipux.com/api/v4/projects/168/repository/branches");
    }
}