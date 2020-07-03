package com.springone.myrestaurants.data;

import java.util.Date;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.graph.neo4j.support.node.Neo4jHelper;
import org.springframework.data.graph.neo4j.support.GraphDatabaseContext;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.transaction.AfterTransaction;
import org.springframework.test.context.transaction.BeforeTransaction;
import org.springframework.transaction.annotation.Transactional;

import com.springone.myrestaurants.domain.UserAccount;

public class AbstractTestWithUserAccount {

	protected final Log log = LogFactory.getLog(getClass());
	protected Long userId;

	@Autowired
	private GraphDatabaseContext graphDatabaseContext;

	@PersistenceContext
	protected EntityManager em;

	@PersistenceUnit
	protected EntityManagerFactory emf;

	@BeforeTransaction
	public void setUpBeforeTransaction() {
		EntityManager setUpEm = emf.createEntityManager();
		EntityTransaction setUpTx = setUpEm.getTransaction();
		setUpTx.begin();
		UserAccount u = new UserAccount();
		u.setFirstName("Bubba");
		u.setLastName("Jones");
		u.setBirthDate(new Date());
		u.setUserName("user");
		setUpEm.persist(u);
		setUpEm.flush();
        u.persist();
		this.userId = u.getId();
		setUpTx.commit();
	}

    @Before
    public void setUp() throws Exception {
    //    em = emf.createEntityManager();
    }

    @Transactional
	@BeforeTransaction
	public void cleanDb() {
	    Neo4jHelper.cleanDb(graphDatabaseContext);
	}

	@AfterTransaction
	public void tearDown() {
		EntityManager tearDownEm = emf.createEntityManager();
		EntityTransaction tearDownTx = tearDownEm.getTransaction();
		tearDownTx.begin();
		UserAccount u = tearDownEm.find(UserAccount.class, this.userId);
		tearDownEm.remove(u);
		tearDownEm.flush();
		tearDownTx.commit();    	
	}

}