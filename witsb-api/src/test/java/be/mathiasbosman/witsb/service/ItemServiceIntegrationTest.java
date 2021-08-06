package be.mathiasbosman.witsb.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

import be.mathiasbosman.fs.service.FileService;
import be.mathiasbosman.witsb.WitsbApplicationTest;
import be.mathiasbosman.witsb.db.ItemRepository;
import be.mathiasbosman.witsb.db.WitsbRepository;
import be.mathiasbosman.witsb.entity.Item;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

class ItemServiceIntegrationTest extends WitsbApplicationTest {

  @MockBean
  private FileService fileService;
  @Autowired
  private ItemService itemService;
  @Autowired
  private ItemRepository itemRepository;

  @Test
  void create() {
    InputStream stream = IOUtils.toInputStream("test content", StandardCharsets.UTF_8);
    given(fileService.exists(any())).willReturn(true);
    Item item = itemService.create("test.txt", stream);
    assertThat(WitsbRepository.getById(itemRepository, item.getId())).isNotNull();
    given(fileService.exists(any())).willReturn(false);
    assertThrows(IllegalStateException.class, () -> itemService.create("test2.text", stream));
  }

  @Test
  void delete() {
    Item itemA = create(Item.builder().name("item A").build());
    given(fileService.exists(any())).willReturn(false);
    itemService.delete(itemA.getId());
    assertThrows(IllegalArgumentException.class,
        () -> WitsbRepository.getById(itemRepository, itemA.getId()));

    Item itemB = create(Item.builder().name("item B").build());
    given(fileService.exists(any())).willReturn(true);
    assertThrows(IllegalStateException.class, () -> itemService.delete(itemB.getId()));
    assertThat(WitsbRepository.getById(itemRepository, itemB.getId())).isNotNull();
  }

  @Test
  void load() {
    Item itemA = create(Item.builder().name("item A").build());
    given(fileService.exists(any())).willReturn(true);
    given(fileService.open(anyString()))
        .willReturn(IOUtils.toInputStream("test content", StandardCharsets.UTF_8));
    assertThat(itemService.load(itemA.getId())).isNotNull();
    given(fileService.exists(any())).willReturn(false);
    assertThrows(IllegalStateException.class, () -> itemService.load(itemA.getId()));
  }

  @Test
  void autoDelete() {
    given(fileService.exists(any())).willReturn(false);
    assertThat(itemService.autoDelete(1, ChronoUnit.DAYS)).isEmpty();
    store(Item.builder().name("item A").build());
    store(Item.builder().name("item B").build());
    assertThat(itemService.autoDelete(1, ChronoUnit.DAYS)).isEmpty();

    LocalDateTime yesterday = LocalDateTime.now().minus(1, ChronoUnit.DAYS);
    Item itemC = create(Item.builder().name("item C").uploaded(yesterday).build());
    Item itemD = create(Item.builder().name("item D").uploaded(yesterday).build());
    assertThat(itemService.autoDelete(1, ChronoUnit.DAYS))
        .hasSize(2)
        .contains(itemC, itemD);
  }
}
