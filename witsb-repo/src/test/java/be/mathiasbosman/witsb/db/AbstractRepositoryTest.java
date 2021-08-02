package be.mathiasbosman.witsb.db;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureTestEntityManager;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@SpringBootTest
@AutoConfigureTestEntityManager
abstract class AbstractRepositoryTest {

  @Autowired
  private TestEntityManager entityManager;

  protected <E> E create(E entity) {
    return entityManager.persist(entity);
  }
}
