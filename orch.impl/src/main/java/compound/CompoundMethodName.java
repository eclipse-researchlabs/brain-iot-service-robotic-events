package compound;

import task.*;


import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;



@Retention(RetentionPolicy.RUNTIME)
public @interface CompoundMethodName {

	String name();
}
