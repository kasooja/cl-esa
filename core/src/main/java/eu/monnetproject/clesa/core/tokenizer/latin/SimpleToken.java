package eu.monnetproject.clesa.core.tokenizer.latin;

import java.util.List;
import eu.monnetproject.clesa.core.token.Token;;

public class SimpleToken implements Token{
    final String val;

    public SimpleToken(String val) {
        this.val = val;
    }

    public List<Token> getChildren() {
        return null;
    }

    public String getValue() {
        return val;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final SimpleToken other = (SimpleToken) obj;
        if ((this.val == null) ? (other.val != null) : !this.val.equals(other.val)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + (this.val != null ? this.val.hashCode() : 0);
        return hash;
    }
    
    public String toString() { return val; }

	public String asString() {
		return val;
	}

}
