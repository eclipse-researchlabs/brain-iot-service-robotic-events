package port;



import java.lang.reflect.Field;
import java.util.Observable;

/*
 * Baouya Abdelhakim (UGA)
 */
public class Port extends Observable{

	Object ref;
	
	boolean available;
	
	boolean trigger;
	
	public boolean isNotified() {
		return trigger;
	}

	public void atomNotify( ) {
		this.trigger = true;
	}
	public void conceal( ) {
		this.trigger = false;
	}

	public boolean isAvailable() {
		return available;
	}

	public void setAvailable(boolean available) {
		this.available = available;
	}

	public void setValue(String name, Object obj) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException{
	
		Class  c = ref.getClass();
		
		Field t = c.getDeclaredField(name);
		
		t.set(ref, obj);

	}

	@SuppressWarnings("unchecked")
	public  Object getValue(String name) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException{
		
		
		Class  c = ref.getClass();
		
		Field t1 = c.getDeclaredField(name);

		
		return  t1.get(ref);
	}

}
