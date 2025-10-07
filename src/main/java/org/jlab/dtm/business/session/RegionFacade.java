/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.dtm.business.session;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.jlab.dtm.persistence.entity.Region;

/**
 * @author ryans
 */
@Stateless
public class RegionFacade extends AbstractFacade<Region> {
  @PersistenceContext(unitName = "webappPU")
  private EntityManager em;

  @Override
  protected EntityManager getEntityManager() {
    return em;
  }

  public RegionFacade() {
    super(Region.class);
  }
}
