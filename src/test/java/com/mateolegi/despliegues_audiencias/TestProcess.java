package com.mateolegi.despliegues_audiencias;

import com.mateolegi.despliegues_audiencias.process.AsyncProcess;
import com.mateolegi.despliegues_audiencias.process.impl.AudienciasGeneration;
import com.mateolegi.despliegues_audiencias.process.impl.CompressionProcess;
import com.mateolegi.despliegues_audiencias.process.impl.FrontGeneration;
import com.mateolegi.despliegues_audiencias.process.impl.GitUploadProcess;
import com.mateolegi.despliegues_audiencias.util.Configuration;
import com.mateolegi.despliegues_audiencias.util.DeployNumbers;
import com.mateolegi.despliegues_audiencias.util.ProcessManager;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.io.File;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

class TestProcess {

    @BeforeAll
    static void prepare() {
        DeployNumbers.setAudienciasVersion("1.0.0-Test");
        DeployNumbers.setBackofficeVersion("1.0.0-Test");
        DeployNumbers.setDeploymentNumber("01-Test");
        DeployNumbers.setDeploymentVersion("Test");
    }

    @AfterAll
    static void delete() {
        var configuration = new Configuration();
        var outputDirectory = new File(configuration.getOutputDirectory());
        var testZip = new File(outputDirectory, "Test.zip");
        assert !testZip.exists() || testZip.delete();
    }

    @AfterAll
    static void deleteGitBranch() {
        File outputDir = new File(new Configuration().getOutputDirectory());
        int respGitPush = new ProcessManager(ProcessManager.SH, "-c", "git push origin :Test")
                .withDirectory(outputDir)
                .startAndWait();
        assert respGitPush == 0;
        int respGitCheckout = new ProcessManager(ProcessManager.SH, "-c", "git checkout master")
                .withDirectory(outputDir)
                .startAndWait();
        assert respGitCheckout == 0;
        int respGitBranch = new ProcessManager(ProcessManager.SH, "-c", "git branch -D Test")
                .withDirectory(outputDir)
                .startAndWait();
        assert respGitBranch == 0;
    }

    @TestFactory
    Stream<DynamicTest> dynamicTests() {
        var processes = Stream.of(
                  new AudienciasGeneration()
                , new FrontGeneration()
                , new CompressionProcess()
                , new GitUploadProcess()
        );
        return processes.map(runnableProcess -> {
            String displayName = runnableProcess.getClass().getSimpleName();
            return DynamicTest.dynamicTest(displayName, () -> test(runnableProcess));
        });
    }

    private static void test(AsyncProcess process)
            throws ExecutionException, InterruptedException {
        assertThat(process.prepare(), is(true));
        CompletableFuture<Integer> future = process.start();
        assertThat(future, is(notNullValue()));
        int resp = future.get();
        assertThat(resp, is(equalTo(0)));
        assertThat(process.validate(), is(true));
    }
}
