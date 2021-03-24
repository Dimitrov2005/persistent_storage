package com.impl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.util.Map;


/**
 * Uses already initialized LOCAL git repository to keep track of
 * database. User should clone the whole repo of the project
 * and use the DB as is.
 */
public class PersisterGitStorage extends PersisterJavaSerialization{

    private Logger logger;

    public PersisterGitStorage(String fileName) {
        super(fileName);

        this.logger = LogManager.getLogger(PersisterGitStorage.class);

        //CreateGitRepo
        try (Git git = Git.init().setDirectory(storageDir).call()) {
            logger.info("Created repository: " + git.getRepository().getDirectory());

            git.add().addFilepattern(fileName).call();
        } catch (GitAPIException e) {
            e.printStackTrace();
        }
    }

    @Override
    public <K, V> void persistMap (Map < K, V > mapToPersist){
        super.persistMap(mapToPersist);
        //After map has been persisted (call to parent to save it ot the file)
        // commit to git using jgit (git add + git commit)
        try (Git git = Git.init().setDirectory(storageDir).call()) {
            git.add().addFilepattern(storageFileName).call();
            git.commit().setMessage("Persistence Commit").call();
        } catch (GitAPIException e) {
            logger.error("",e);
        }
    }
}

    //TODO : let user set the repository location, remote or local
    // Repository initializer class/method - take two params - path/link and local/remote
    // In future, implement getting the DB from the git server