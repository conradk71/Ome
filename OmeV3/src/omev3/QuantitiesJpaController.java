/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package omev3;

import java.io.Serializable;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import omev3.exceptions.NonexistentEntityException;

/**
 *
 * @author Conrad
 */
public class QuantitiesJpaController implements Serializable {

    public QuantitiesJpaController(EntityManagerFactory emf) {
        this.emf = emf;
    }
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(Quantities quantities) {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Products product = quantities.getProduct();
            if (product != null) {
                product = em.getReference(product.getClass(), product.getId());
                quantities.setProduct(product);
            }
            Products products = quantities.getProducts();
            if (products != null) {
                products = em.getReference(products.getClass(), products.getId());
                quantities.setProducts(products);
            }
            em.persist(quantities);
            if (product != null) {
                product.getQuantitiesCollection().add(quantities);
                product = em.merge(product);
            }
            if (products != null) {
                products.getQuantitiesCollection().add(quantities);
                products = em.merge(products);
            }
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(Quantities quantities) throws NonexistentEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Quantities persistentQuantities = em.find(Quantities.class, quantities.getId());
            Products productOld = persistentQuantities.getProduct();
            Products productNew = quantities.getProduct();
            Products productsOld = persistentQuantities.getProducts();
            Products productsNew = quantities.getProducts();
            if (productNew != null) {
                productNew = em.getReference(productNew.getClass(), productNew.getId());
                quantities.setProduct(productNew);
            }
            if (productsNew != null) {
                productsNew = em.getReference(productsNew.getClass(), productsNew.getId());
                quantities.setProducts(productsNew);
            }
            quantities = em.merge(quantities);
            if (productOld != null && !productOld.equals(productNew)) {
                productOld.getQuantitiesCollection().remove(quantities);
                productOld = em.merge(productOld);
            }
            if (productNew != null && !productNew.equals(productOld)) {
                productNew.getQuantitiesCollection().add(quantities);
                productNew = em.merge(productNew);
            }
            if (productsOld != null && !productsOld.equals(productsNew)) {
                productsOld.getQuantitiesCollection().remove(quantities);
                productsOld = em.merge(productsOld);
            }
            if (productsNew != null && !productsNew.equals(productsOld)) {
                productsNew.getQuantitiesCollection().add(quantities);
                productsNew = em.merge(productsNew);
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                Integer id = quantities.getId();
                if (findQuantities(id) == null) {
                    throw new NonexistentEntityException("The quantities with id " + id + " no longer exists.");
                }
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void destroy(Integer id) throws NonexistentEntityException {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Quantities quantities;
            try {
                quantities = em.getReference(Quantities.class, id);
                quantities.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The quantities with id " + id + " no longer exists.", enfe);
            }
            Products product = quantities.getProduct();
            if (product != null) {
                product.getQuantitiesCollection().remove(quantities);
                product = em.merge(product);
            }
            Products products = quantities.getProducts();
            if (products != null) {
                products.getQuantitiesCollection().remove(quantities);
                products = em.merge(products);
            }
            em.remove(quantities);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<Quantities> findQuantitiesEntities() {
        return findQuantitiesEntities(true, -1, -1);
    }

    public List<Quantities> findQuantitiesEntities(int maxResults, int firstResult) {
        return findQuantitiesEntities(false, maxResults, firstResult);
    }

    private List<Quantities> findQuantitiesEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(Quantities.class));
            Query q = em.createQuery(cq);
            if (!all) {
                q.setMaxResults(maxResults);
                q.setFirstResult(firstResult);
            }
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    public Quantities findQuantities(Integer id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Quantities.class, id);
        } finally {
            em.close();
        }
    }

    public int getQuantitiesCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<Quantities> rt = cq.from(Quantities.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
