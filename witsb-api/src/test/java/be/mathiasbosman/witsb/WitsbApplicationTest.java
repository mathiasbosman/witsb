package be.mathiasbosman.witsb;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureTestEntityManager;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
@AutoConfigureTestEntityManager
public abstract class WitsbApplicationTest {

  @Autowired
  private MockMvc mvc;

  @Autowired
  private TestEntityManager entityManager;

  protected <E> E create(E entity) {
    return entityManager.persist(entity);
  }

  protected <E> void store(E entity) {
    entityManager.persist(entity);
  }

  protected MockMvc mvc() {
    return this.mvc;
  }
}
