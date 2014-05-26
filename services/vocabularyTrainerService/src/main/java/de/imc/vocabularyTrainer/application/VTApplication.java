package de.imc.vocabularyTrainer.application;

import javax.sql.DataSource;

import org.restlet.Restlet;
import org.restlet.data.ChallengeScheme;
import org.restlet.routing.Router;
import org.restlet.security.ChallengeAuthenticator;

import de.imc.vocabularyTrainer.database.DatabaseWrapper;
import de.imc.vocabularyTrainer.resource.ItemResource;
import de.imc.vocabularyTrainer.resource.ListResource;
import de.imc.vocabularyTrainer.resource.LoginRessource;
import de.imc.vocabularyTrainer.resource.ScoreResource;
import de.imc.vocabularyTrainer.resource.TrainingResource;
import de.imc.vocabularyTrainer.resource.UserItemsResource;
import de.imc.vocabularyTrainer.resource.UserListsResource;
import de.imc.vocabularyTrainer.resource.UserResource;
import de.imc.vocabularyTrainer.verifier.DatabaseVerifier;


/**
 * @author Daniel Dahrendorf (daniel.dahrendorf@im-c.de)
 * @version 0.1, 21.12.2009
 *
 */
public class VTApplication extends org.restlet.Application {

    /** The list of items is persisted in memory. */
    private DatabaseWrapper dbWrapper;
    private DataSource dataSource;
    
//    private static Logger logger = Logger.getLogger(VTApplication.class);

    public VTApplication(){
    	
//    	Context initContext;
//		try {
//			initContext = new InitialContext();
//
//			Context envContext  = (Context)initContext.lookup("java:/comp/env");
//			dataSource = (DataSource)envContext.lookup("jdbc/VTDB");
//			
//			connection = ds.getConnection();
//			
//			dbWrapper = new DatabaseWrapper(dataSource);	
			dbWrapper = new DatabaseWrapper("jdbc/VTDB");	
//			
//		} catch (NamingException e) {
//			
//			logger.error(e.toString());
//		}
			 
    }
    
    /**
     * Creates a root Restlet that will receive all incoming calls.
     */
    @Override
    public synchronized Restlet createRoot() {
        // Create a router Restlet that defines routes.
        Router router = new Router(getContext());

        //Create Verifiers
        DatabaseVerifier databaseVerifier = new DatabaseVerifier(dbWrapper);  
        
        // Guard the restlet with BASIC authentication.
        ChallengeAuthenticator loginGuard = 
        	new ChallengeAuthenticator(null, ChallengeScheme.HTTP_BASIC, "testRealm");
        loginGuard.setVerifier(databaseVerifier);
        
        // Defines a route for the resource "user"
        loginGuard.setNext(LoginRessource.class); 

        // Guard the restlet with BASIC authentication.
        ChallengeAuthenticator scoreGuard = 
        	new ChallengeAuthenticator(null, ChallengeScheme.HTTP_BASIC, "testRealm");
        scoreGuard.setVerifier(databaseVerifier);
        
        scoreGuard.setNext(ScoreResource.class);        
        
        
        // Defines a route for the resource "login"
        router.attach("/login", loginGuard);             
        
        // Defines a route for the resource "user"
        router.attach("/user", UserResource.class);
        // Defines a route for the resource "user"
        router.attach("/user/{userName}", UserResource.class);
        
        // Defines a route for the resource "userLists"
        router.attach("/userLists/{userName}", UserListsResource.class);
        
        // Defines a route for the resource "userItems"
        router.attach("/userItems/{userName}/{listId}", UserItemsResource.class);
        
        // Defines a route for the resource "item"
        router.attach("/item/{listId}", ItemResource.class);
        // Defines a route for the resource "item"
        router.attach("/item/{listId}/{itemId}", ItemResource.class);
        
        // Defines a route for the resource "list"
        router.attach("/list/{listId}", ListResource.class);
        // Defines a route for the resource "list"
        router.attach("/list/{listId}/{userName}", ListResource.class);
        
        // Defines a route for the resource "train"
        router.attach("/train/{userName}/{itemId}", TrainingResource.class);
        
        // Defines a route for the resource "score"
        router.attach("/score/{userName}", scoreGuard);
        

        return router;
    }

    /**
     * Returns the list of registered items.
     * 
     * @return the list of registered items.
     */
    public DataSource getConnection() {
        return dataSource;
    }
    

    /**
     * Returns the database wrapper
     * 
     * @return
     */
    public DatabaseWrapper getDBWrapper() {
        return dbWrapper;
    }
}
