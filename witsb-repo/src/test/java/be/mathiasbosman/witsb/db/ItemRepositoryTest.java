package be.mathiasbosman.witsb.db;

import static org.assertj.core.api.Assertions.assertThat;

import be.mathiasbosman.witsb.entity.Item;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class ItemRepositoryTest extends AbstractRepositoryTest {

  @Autowired
  private ItemRepository repository;

  @Test
  void getById() {
    Item itemA = create(Item.builder().name("item A").build());
    Item search = WitsbRepository.getById(repository, itemA.getId());
    assertThat(search).isEqualTo(itemA);
    assertThat(search.getCreated()).isNotNull();
    assertThat(search.getUpdated()).isNotNull();
  }
}
