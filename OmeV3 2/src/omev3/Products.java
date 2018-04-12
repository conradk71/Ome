    /*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package omev3;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;
import java.util.Collection;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

/**
 *
 * @author Conrad
 */
@Entity
@Table(name = "PRODUCTS")
@NamedQueries({
    @NamedQuery(name = "Products.findAll", query = "SELECT p FROM Products p")
    , @NamedQuery(name = "Products.findById", query = "SELECT p FROM Products p WHERE p.id = :id")
    , @NamedQuery(name = "Products.findByProdname", query = "SELECT p FROM Products p WHERE p.prodname = :prodname")
    , @NamedQuery(name = "Products.findByProdprice", query = "SELECT p FROM Products p WHERE p.prodprice = :prodprice")})
public class Products implements Serializable {

    @Transient
    private PropertyChangeSupport changeSupport = new PropertyChangeSupport(this);

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "ID")
    private Integer id;
    @Column(name = "PRODNAME")
    private String prodname;
    @Column(name = "PRODPRICE")
    private String prodprice;
    @OneToMany(mappedBy = "product")
    private Collection<Quantities> quantitiesCollection;
    @OneToOne(cascade = CascadeType.ALL, mappedBy = "products")
    private Quantities quantities;

    public Products() {
    }

    public Products(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        Integer oldId = this.id;
        this.id = id;
        changeSupport.firePropertyChange("id", oldId, id);
    }

    public String getProdname() {
        return prodname;
    }

    public void setProdname(String prodname) {
        String oldProdname = this.prodname;
        this.prodname = prodname;
        changeSupport.firePropertyChange("prodname", oldProdname, prodname);
    }

    public String getProdprice() {
        return prodprice;
    }

    public void setProdprice(String prodprice) {
        String oldProdprice = this.prodprice;
        this.prodprice = prodprice;
        changeSupport.firePropertyChange("prodprice", oldProdprice, prodprice);
    }

    public Collection<Quantities> getQuantitiesCollection() {
        return quantitiesCollection;
    }

    public void setQuantitiesCollection(Collection<Quantities> quantitiesCollection) {
        this.quantitiesCollection = quantitiesCollection;
    }

    public Quantities getQuantities() {
        return quantities;
    }

    public void setQuantities(Quantities quantities) {
        this.quantities = quantities;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Products)) {
            return false;
        }
        Products other = (Products) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return prodname;
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        changeSupport.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        changeSupport.removePropertyChangeListener(listener);
    }
    
}
