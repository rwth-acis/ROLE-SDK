package de.imc.vocabularyTrainer.verifier;

import org.restlet.security.LocalVerifier;

import de.imc.vocabularyTrainer.database.DatabaseWrapper;

public class DatabaseVerifier extends LocalVerifier{

	private DatabaseWrapper dataBaseWrapper;
	
	public DatabaseVerifier(DatabaseWrapper dataBaseWrapper){
		this.dataBaseWrapper = dataBaseWrapper;
	}
	
	@Override
	public char[] getLocalSecret(String identifier) {
		
		if(identifier.equals("")){
			return null;
		}
		System.out.println("User tries to login: "+identifier);
		return dataBaseWrapper.getPassword(identifier);
	}





	
}
