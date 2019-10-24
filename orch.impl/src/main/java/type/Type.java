package type;


public class Type<T> {

	T val;

	public Type(T val) {
		super();
		this.val = val;
	}
	public Type() {
	}


	public T getVal() {
		return val;
	}

	public void setVal(T val) {
		this.val = val;
	}
	
	
	
}
