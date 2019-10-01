package com.mateolegi.git.remote.gitlab;

import com.mateolegi.git.remote.GitRemote;
import com.mateolegi.net.Rest;
import com.mateolegi.net.RestException;

import java.util.List;

public class GitLabRemote implements GitRemote {

    @Override
    public List<Branch> getRemoteBranches(String endpoint) {
        try {
            var rest = new Rest();
            var response = rest.get(endpoint);
            var pages = response.getHeader("X-Total-Pages").orElse(null);
            System.out.println(pages);
        } catch (RestException e) {
            e.printStackTrace();
        }
        return null;
    }

//    private List<Branch> getBranchesListPerPage(String endpoint, int page) {
//        var rest = new Rest();
//        rest.get(endpoint + "?per_page=100&page=" + page);
//    }
}
