package sonia.scm.group.update;

import com.google.common.io.Resources;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junitpioneer.jupiter.TempDirectory;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.SCMContextProvider;
import sonia.scm.group.Group;
import sonia.scm.group.xml.XmlGroupDAO;
import sonia.scm.security.AssignedPermission;
import sonia.scm.store.ConfigurationEntryStore;
import sonia.scm.store.InMemoryConfigurationEntryStore;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@ExtendWith(TempDirectory.class)
class XmlGroupV1UpdateStepTest {

  @Mock
  SCMContextProvider contextProvider;
  @Mock
  XmlGroupDAO groupDAO;

  @Captor
  ArgumentCaptor<Group> groupCaptor;

  XmlGroupV1UpdateStep updateStep;
  ConfigurationEntryStore<AssignedPermission> assignedPermissionStore;

  @BeforeEach
  void mockScmHome(@TempDirectory.TempDir Path tempDir) {
    when(contextProvider.getBaseDirectory()).thenReturn(tempDir.toFile());
    assignedPermissionStore = new InMemoryConfigurationEntryStore<>();
    updateStep = new XmlGroupV1UpdateStep(contextProvider, groupDAO);
  }

  @Nested
  class WithExistingDatabase {

    @BeforeEach
    void captureStoredRepositories() {
      doNothing().when(groupDAO).add(groupCaptor.capture());
    }

    @BeforeEach
    void createGroupV1XML(@TempDirectory.TempDir Path tempDir) throws IOException {
      Path configDir = tempDir.resolve("config");
      Files.createDirectories(configDir);
      copyTestDatabaseFile(configDir, "groups.xml");
    }

    @Test
    void shouldCreateNewGroupFromGroupsV1Xml() throws JAXBException {
      updateStep.doUpdate();
      verify(groupDAO, times(2)).add(any());
    }

    @Test
    void shouldMapAttributesFromGroupsV1Xml() throws JAXBException {
      updateStep.doUpdate();
      Optional<Group> group = groupCaptor.getAllValues().stream().filter(u -> u.getName().equals("normals")).findFirst();
      assertThat(group)
        .get()
        .hasFieldOrPropertyWithValue("name", "normals")
        .hasFieldOrPropertyWithValue("description", "Normal people")
        .hasFieldOrPropertyWithValue("type", "xml")
        .hasFieldOrPropertyWithValue("members", asList("trillian", "dent"))
        .hasFieldOrPropertyWithValue("lastModified", 1559550955883L)
        .hasFieldOrPropertyWithValue("creationDate", 1559548942457L);
    }
  }

  private void copyTestDatabaseFile(Path configDir, String groupsFileName) throws IOException {
    URL url = Resources.getResource("sonia/scm/group/update/" + groupsFileName);
    Files.copy(url.openStream(), configDir.resolve(groupsFileName));
  }

  @Test
  void shouldNotFailForMissingConfigDir() throws JAXBException {
    updateStep.doUpdate();
  }
}