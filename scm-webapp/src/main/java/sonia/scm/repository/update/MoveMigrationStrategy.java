package sonia.scm.repository.update;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.SCMContextProvider;
import sonia.scm.repository.AbstractSimpleRepositoryHandler;
import sonia.scm.repository.RepositoryLocationResolver;

import javax.inject.Inject;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static java.util.Arrays.asList;

class MoveMigrationStrategy extends BaseMigrationStrategy {

  private static final Logger LOG = LoggerFactory.getLogger(MoveMigrationStrategy.class);

  private final RepositoryLocationResolver locationResolver;

  @Inject
  public MoveMigrationStrategy(SCMContextProvider contextProvider, RepositoryLocationResolver locationResolver) {
    super(contextProvider);
    this.locationResolver = locationResolver;
  }

  @Override
  public Path migrate(String id, String name, String type) {
    Path repositoryBasePath = locationResolver.forClass(Path.class).getLocation(id);
    Path targetDataPath = repositoryBasePath
      .resolve(AbstractSimpleRepositoryHandler.REPOSITORIES_NATIVE_DIRECTORY);
    Path sourceDataPath = getSourceDataPath(name, type);
    moveData(sourceDataPath, targetDataPath);
    deleteOldDataDir(getTypeDependentPath(type), name);
    return repositoryBasePath;
  }

  private void deleteOldDataDir(Path rootPath, String name) {
    delete(rootPath, asList(name.split("/")));
  }

  private void delete(Path rootPath, List<String> directories) {
    if (directories.isEmpty()) {
      return;
    }
    Path directory = rootPath.resolve(directories.get(0));
    delete(directory, directories.subList(1, directories.size()));
    try {
      Files.deleteIfExists(directory);
    } catch (IOException e) {
      LOG.warn("could not delete source repository directory {}", directory);
    }
  }

  private void moveData(Path sourceDirectory, Path targetDirectory) {
    createDataDirectory(targetDirectory);
    listSourceDirectory(sourceDirectory).forEach(
      sourceFile -> {
        Path targetFile = targetDirectory.resolve(sourceFile.getFileName());
        if (Files.isDirectory(sourceFile)) {
          moveData(sourceFile, targetFile);
        } else {
          moveFile(sourceFile, targetFile);
        }
      }
    );
    try {
      Files.delete(sourceDirectory);
    } catch (IOException e) {
      LOG.warn("could not delete source repository directory {}", sourceDirectory);
    }
  }
}
