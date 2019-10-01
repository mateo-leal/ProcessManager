package com.mateolegi.despliegues_audiencias.util;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectHelper;

import java.io.File;

public class AntManager {

    private static final Configuration CONFIGURATION = new Configuration();
    private final File buildFile = new File("build.xml");
    private final Project project = new Project();

    public AntManager() {
        final var outputDirectory = new File(CONFIGURATION.getOutputDirectory());
        final var dirWorkspace = new File(CONFIGURATION.getDirectoryWorkspace());
        final var userProfile = new File(CONFIGURATION.getUserProfile());
        project.setUserProperty("dir.buildfile", outputDirectory.getAbsolutePath());
        project.setUserProperty("ant.file", buildFile.getAbsolutePath());
        project.setUserProperty("dir.workspace", dirWorkspace.getAbsolutePath());
        project.setUserProperty("user.profile", userProfile.getAbsolutePath());
        project.init();
    }

    public void build() {
        ProjectHelper helper = ProjectHelper.getProjectHelper();
        project.addReference("ant.projectHelper", helper);
        helper.parse(project, buildFile);
        project.executeTarget(project.getDefaultTarget());
    }
}
