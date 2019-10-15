package com.mateolegi.git.remote.gitlab;

import com.mateolegi.git.remote.GitRemote;
import com.mateolegi.net.Rest;

import java.net.http.HttpResponse;
import java.util.List;

public class GitLabRemote implements GitRemote {

    @Override
    public List<Branch> getRemoteBranches(String endpoint) {
        var rest = new Rest();
        var pages = rest.get(endpoint)
                .thenApply(HttpResponse::headers)
                .thenApply(httpHeaders -> httpHeaders.firstValue("X-Total-Pages"));
        System.out.println(pages);
        return null;
    }

//    private List<Branch> getBranchesListPerPage(String endpoint, int page) {
//        var rest = new Rest();
//        rest.get(endpoint + "?per_page=100&page=" + page);
//    }
}
