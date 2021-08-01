package be.mathiasbosman.entity;

import org.springframework.data.domain.Sort;

public interface Identifiable<K> {
  K getId();

  Sort getDefaultSort();
}
