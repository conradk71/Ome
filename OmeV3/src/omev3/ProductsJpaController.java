/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package omev3;

import java.io.Serializable;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import omev3.exceptions.IllegalOrphanException;
import omev3.exceptions.NonexistentEntityException;

/**
 *
 * @author Conrad
 */
public class ProductsJpaController implements Serializable {

    public ProductsJpaController(EntityManagerFactory emf) {
        this.emf = emf;
    }
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(Products products) {
        if (products.getQuantitiesCollection() == null) {
            products.setQuantitiesCollection(new ArrayList<Quantities>());
        }
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Quantities quantities = products.getQuantities();
            if (quantities != null) {
                quantities = em.getReference(quantities.getClass(), quantities.getId());
                products.setQuantities(quantities);
            }
            Collection<Quantities> attachedQuantitiesCollection = new ArrayList<Quantities>();
            for (Quantities quantitiesCollectionQuantitiesToAttach : products.getQuantitiesCollection()) {
                quantitiesCollectionQuantitiesToAttach = em.getReference(quantitiesCollectionQuantitiesToAttach.getClass(), quantitiesCollectionQuantitiesToAttach.getId());
                attachedQuantitiesCollection.add(quantitiesCollectionQuantitiesToAttach);
            }
            products.setQuantitiesCollection(attachedQuantitiesCollection);
            em.persist(products);
            if (quantities != null) {
                Products oldProductsOfQuantities = quantities.getProducts();
                if (oldProductsOfQuantities != null) {
                    oldProductsOfQuantities.setQuantities(null);
                    oldProductsOfQuantities = em.merge(oldProductsOfQuantities);
                }
                quantities.setProducts(products);
                quantities = em.merge(quantities);
            }
            for (Quantities quantitiesCollectionQuantities : products.getQuantitiesCollection()) {
                Products oldProductOfQuantitiesCollectionQuantities = quantitiesCollectionQuantities.getProduct();
                quantitiesCollectionQuantities.setProduct(products);
                quantitiesCollectionQuantities = em.merge(quantitiesCollectionQuantities);
                if (oldProductOfQuantitiesCollectionQuantities != null) {
                    oldProductOfQuantitiesCollectionQuantities.getQuantitiesCollection().remove(quantitiesCollectionQuantities);
                    oldProductOfQuantitiesCollectionQuantities = em.merge(oldProductOfQuantitiesCollectionQuantities);
                }
            }
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(Products products) throws IllegalOrphanException, NonexistentEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Products persistentProducts = em.find(Products.class, products.getId());
            Quantities quantitiesOld = persistentProducts.getQuantities();
            Quantities quantitiesNew = products.getQuantities();
            Collection<Quantities> quantitiesCollectionOld = persistentProducts.getQuantitiesCollection();
            Collection<Quantities> quantitiesCollectionNew = products.getQuantitiesCollection();
            List<String> illegalOrphanMessages = null;
            if (quantitiesOld != null && !quantitiesOld.equals(quantitiesNew)) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("You must retain Quantities " + quantitiesOld + " since its products field is not nullable.");
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            if (quantitiesNew != null) {
                quantitiesNew = em.getReference(quantitiesNew.getClass(), quantitiesNew.getId());
                products.setQuantities(quantitiesNew);
            }
            Collection<Quantities> attachedQuantitiesCollectionNew = new ArrayList<Quantities>();
            for (Quantities quantitiesCollectionNewQuantitiesToAttach : quantitiesCollectionNew) {
                quantitiesCollectionNewQuantitiesToAttach = em.getReference(quantitiesCollectionNewQuantitiesToAttach.getClass(), quantitiesCollectionNewQuantitiesToAttach.getId());
                attachedQuantitiesCollectionNew.add(quantitiesCollectionNewQuantitiesToAttach);
            }
            quantitiesCollectionNew = attachedQuantitiesCollectionNew;
            products.setQuantitiesCollection(quantitiesCollectionNew);
            products = em.merge(products);
            if (quantitiesNew != null && !quantitiesNew.equals(quantitiesOld)) {
                Products oldProductsOfQuantities = quantitiesNew.getProducts();
                if (oldProductsOfQuantities != null) {
                    oldProductsOfQuantities.setQuantities(null);
                    oldProductsOfQuantities = em.merge(oldProductsOfQuantities);
                }
                quantitiesNew.setProducts(products);
                quantitiesNew = em.merge(quantitiesNew);
            }
            for (Quantities quantitiesCollectionOldQuantities : quantitiesCollectionOld) {
                if (!quantitiesCollectionNew.contains(quantitiesCollectionOldQuantities)) {
                    quantitiesCollectionOldQuantities.setProduct(null);
                    quantitiesCollectionOldQuantities = em.merge(quantitiesCollectionOldQuantities);
                }
            }
            for (Quantities quantitiesCollectionNewQuantities : quantitiesCollectionNew) {
                if (!quantitiesCollectionOld.contains(quantitiesCollectionNewQuantities)) {
                    Products oldProductOfQuantitiesCollectionNewQuantities = quantitiesCollectionNewQuantities.getProduct();
                    quantitiesCollectionNewQuantities.setProduct(products);
                    quantitiesCollectionNewQuantities = em.merge(quantitiesCollectionNewQuantities);
                    if (oldProductOfQuantitiesCollectionNewQuantities != null && !oldProductOfQuantitiesCollectionNewQuantities.equals(products)) {
                        oldProductOfQuantitiesCollectionNewQuantities.getQuantitiesCollection().remove(quantitiesCollectionNewQuantities);
                        oldProductOfQuantitiesCollectionNewQuantities = em.merge(oldProductOfQuantitiesCollectionNewQuantities);
                    }
                }
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                Integer id = products.getId();
                if (findProducts(id) == null) {
                    throw new NonexistentEntityException("The products with id " + id + " no longer exists.");
                }
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void destroy(Integer id) throws IllegalOrphanException, NonexistentEntityException {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Products products;
            try {
                products = em.getReference(Products.class, id);
                products.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The products with id " + id + " no longer exists.", enfe);
            }
            List<String> illegalOrphanMessages = null;
            Quantities quantitiesOrphanCheck = products.getQuantities();
            if (quantitiesOrphanCheck != null) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("This Products (" + products + ") cannot be destroyed since the Quantities " + quantitiesOrphanCheck + " in its quantities field has a non-nullable products field.");
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            Collection<Quantities> quantitiesCollection = products.getQuantitiesCollection();
            for (Quantities quantitiesCollectionQuantities : quantitiesCollection) {
                quantitiesCollectionQuantities.setProduct(null);
                quantitiesCollectionQuantities = em.merge(quantitiesCollectionQuantities);
            }
            em.remove(products);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<Products> findProductsEntities() {
        return findProductsEntities(true, -1, -1);
    }

    public List<Products> findProductsEntities(int maxResults, int firstResult) {
        return findProductsEntities(false, maxResults, firstResult);
    }

    private List<Products> findProductsEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(Products.class));
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

    public Products findProducts(Integer id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Products.class, id);
        } finally {
            em.close();
        }
    }

    public int getProductsCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<Products> rt = cq.from(Products.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
