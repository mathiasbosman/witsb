package be.mathiasbosman.witsb.entity;

import org.springframework.data.domain.Sort;

public interface WitsbEntity<K> extends Identifiable<K> {

  Sort getDefaultSort();
}
