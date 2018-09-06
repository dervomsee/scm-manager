package sonia.scm.api.v2.resources;

import org.junit.Test;
import org.mapstruct.factory.Mappers;
import sonia.scm.group.Group;

import java.time.Instant;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

public class GroupDtoToGroupMapperTest {

  @Test
  public void shouldMapAttributes() {
    GroupDto dto = new GroupDto();
    dto.setName("group");
    dto.setLastModified(Instant.ofEpochMilli(1234));
    Group group = Mappers.getMapper(GroupDtoToGroupMapper.class).map(dto);
    assertEquals("group", group.getName());
    assertThat(group.getLastModified()).isEqualTo(dto.getLastModified().toEpochMilli());
  }

  @Test
  public void shouldMapMembers() {
    GroupDto dto = new GroupDto();
    dto.setMembers(Arrays.asList("member1", "member2"));
    Group group = Mappers.getMapper(GroupDtoToGroupMapper.class).map(dto);

    assertEquals(2, group.getMembers().size());
    assertEquals("member1", group.getMembers().get(0));
    assertEquals("member2", group.getMembers().get(1));
  }
}
