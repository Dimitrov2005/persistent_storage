package com.impl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

import java.io.File;
import java.io.IOException;
import java.util.Map;


/**
 * Uses local or remote git repository to persist and keep track of
 * database. User should clone the whole repo of the project and use
 * the DB as is.
 */
public class PersisterGitStorage extends PersisterJavaSerialization{

    private Logger logger;
    private boolean hasRemoteOrigin;
    private String gitUsername;
    private String gitPassword;

    /**
     * Construct a git storage instance
     * @param fileName - name of the DB
     * @param remoteRepositoryUrl - "https://provider/username/repo.git"
     * @param isBareRepository - true if the remote is already initialized
     */
    public PersisterGitStorage(String fileName, String remoteRepositoryUrl, boolean isBareRepository) {
        super(fileName);

        this.logger = LogManager.getLogger(PersisterGitStorage.class);

        if(remoteRepositoryUrl.isEmpty() || remoteRepositoryUrl == null){
            handleLocalGitRepositoryCreation(fileName, storageDir);
        } else {
            this.hasRemoteOrigin = true;
            handleRemoteGitRepositoryCreation(storageDir, isBareRepository, remoteRepositoryUrl);
        }
    }

    @Override
    public <K, V> void persistMap (Map < K, V > mapToPersist){
        //First auto update
        try (Git git = Git.open(new File(storagePath + ".git"))) {
            if(hasRemoteOrigin) {
                git.pull().call();
            }
            updateMap(mapToPersist);
            git.add().addFilepattern(storageFileName).call();
            git.commit().setMessage("Persistence Commit").call();
            if(hasRemoteOrigin) {
                validateUsernamePassword(gitPassword, gitUsername);
                final CredentialsProvider credentialsProvider = new UsernamePasswordCredentialsProvider(gitUsername, gitPassword);
                git.push().setRemote("origin").setCredentialsProvider(credentialsProvider).call();
            }
        } catch (GitAPIException | IOException e) {
            logger.error("",e);
            throw new RuntimeException();
        }
        super.persistMap(mapToPersist);
    }

    /**
     * Update the internal map with the map from the remote repository to
     * keep the data consistent.
     * @param internal the internal map used for storage
     * @param <K> key type
     * @param <V> value type
     */
    public <K,V> void updateMap (Map <K,V> internal){
        Map <K, V> pulledMapDeserialized = (Map<K, V>) getMapFromLocalFile();
        internal.putAll(pulledMapDeserialized);
    }

    /**
     * The password used for authentication
     * @param gitPassword
     */
    public void setGitPassword(String gitPassword) {
        this.gitPassword = gitPassword;
    }
    /**
     * The username used for authentication
     * @param gitUsername
     */
    public void setGitUsername(String gitUsername) {
        this.gitUsername = gitUsername;
    }

    /**
     * Simple validation for username and password.
     * to be updated in future
     */
    private void validateUsernamePassword(String user, String pass) {
        if(user.isEmpty() || user.equals(null)){
            logger.error("You haven't provided username");
        }
        if(pass.isEmpty() || pass.equals(null)){
            logger.error("You haven't provided password");
        }
    }

    /**
     * Create local repository
     * @param initialFileToAdd - file name for a file to add
     * @param repoDirectory - directory for repo initialization
     */
    private void handleLocalGitRepositoryCreation(String initialFileToAdd, File repoDirectory){
        try (Git git = Git.init().setDirectory(repoDirectory).call()) {
            logger.info("Created local repository: " + git.getRepository().getDirectory());

            git.add().addFilepattern(initialFileToAdd).call();
        } catch (GitAPIException e) {
            logger.error("", e);
            throw new RuntimeException();
        }
    }

    /**
     * Clone/pull remote repository from url
     * @param repoDirectory - directory for repo initialization
     * @param bare - if true, clone will be executed, else pull
     * @param repoUrl - repository url
     */
    private void handleRemoteGitRepositoryCreation(File repoDirectory, boolean bare, String repoUrl ){
        if(bare) {
            try (Git git = Git.cloneRepository().setURI(repoUrl).setGitDir(repoDirectory).call()) {
                logger.info("Cloned repository from remote url "+ repoUrl + " into: " + git.getRepository().getDirectory());
            } catch (GitAPIException e) {
                logger.error("", e);
                throw new RuntimeException();
            }
        }else {
            try (Git git = Git.open(new File(repoDirectory.getAbsolutePath()+ "\\.git"))) {
                logger.info("Pulled changes from remote url "+ repoUrl + " into: " + git.getRepository().getDirectory());
                git.pull().call();
            } catch (GitAPIException | IOException e) {
                logger.error("",e);
                throw new RuntimeException();
            }
        }
    }
}