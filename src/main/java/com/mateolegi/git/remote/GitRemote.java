package com.mateolegi.git.remote;

import com.mateolegi.git.remote.gitlab.Branch;

import java.util.List;

public interface GitRemote {
    List<Branch> getRemoteBranches(String endpoint);
}
