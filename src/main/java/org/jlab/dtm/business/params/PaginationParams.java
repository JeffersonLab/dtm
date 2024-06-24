package org.jlab.dtm.business.params;

/**
 * @author ryans
 */
public class PaginationParams {
  private int offset;
  private int max;

  public int getOffset() {
    return offset;
  }

  public void setOffset(int offset) {
    this.offset = offset;
  }

  public int getMax() {
    return max;
  }

  public void setMax(int max) {
    this.max = max;
  }
}
