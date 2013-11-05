package eu.monnetproject.clesa.core.token;


public class TokenImpl implements Token {

	private String token;
	
	public TokenImpl(String token) {
		this.token = token;
	}
	
	public String getValue() {
		return token;
	}	

}
